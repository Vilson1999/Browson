package com.example.data.dns

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object DnsManager {
    private const val TAG = "DnsManager"

    // DNS Cache with TTL (hostname -> Pair(ipList, expireTimestamp))
    private val cache = ConcurrentHashMap<String, Pair<List<InetAddress>, Long>>()

    // Basic OkHttpClient for fetching DoH queries directly without recursion
    private val rawClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun getDoHEndpoint(provider: String, customDns: String): String? {
        return when {
            provider.contains("Cloudflare", ignoreCase = true) -> "https://cloudflare-dns.com/dns-query"
            provider.contains("Google", ignoreCase = true) -> "https://dns.google/resolve"
            provider.contains("Quad9", ignoreCase = true) -> "https://dns.quad9.net/dns-query"
            provider.contains("Custom", ignoreCase = true) -> {
                val trimmed = customDns.trim()
                when {
                    trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                    trimmed.isNotBlank() -> "https://$trimmed/dns-query"
                    else -> null
                }
            }
            else -> null // System default
        }
    }

    suspend fun resolve(hostname: String, provider: String, customDns: String): List<InetAddress>? {
        if (hostname.isBlank() || hostname == "localhost" || hostname == "127.0.0.1") {
            return null
        }

        // Check if hostname is already an IP address
        try {
            val parts = hostname.split(".")
            if (parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }) {
                return listOf(InetAddress.getByName(hostname))
            }
        } catch (_: Exception) {}

        // Check cache
        val cached = cache[hostname]
        if (cached != null && System.currentTimeMillis() < cached.second) {
            return cached.first
        }

        val endpoint = getDoHEndpoint(provider, customDns) ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val url = Uri.parse(endpoint).buildUpon()
                    .appendQueryParameter("name", hostname)
                    .appendQueryParameter("type", "A")
                    .build()
                    .toString()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/dns-json")
                    .build()

                rawClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val bodyString = response.body?.string() ?: return@use null
                    val json = JSONObject(bodyString)

                    if (!json.has("Answer")) return@use null
                    val answerArray = json.getJSONArray("Answer")
                    val result = mutableListOf<InetAddress>()
                    var ttl = 300L

                    for (i in 0 until answerArray.length()) {
                        val record = answerArray.getJSONObject(i)
                        val type = record.optInt("type", -1)
                        if (type == 1) { // Type 1 is A record (IPv4)
                            val ip = record.getString("data")
                            try {
                                result.add(InetAddress.getByName(ip))
                                ttl = record.optLong("TTL", 300L).coerceAtLeast(60L)
                            } catch (_: Exception) {}
                        }
                    }

                    if (result.isNotEmpty()) {
                        cache[hostname] = Pair(result, System.currentTimeMillis() + (ttl * 1000L))
                        result
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "DoH lookup failed for $hostname via $endpoint: ${e.message}")
                null
            }
        }
    }

    suspend fun testDns(provider: String, customDns: String): String {
        val testHost = "cloudflare.com"
        val endpoint = getDoHEndpoint(provider, customDns)
            ?: return "Using System DNS (Provider default)"

        return try {
            val resolved = resolve(testHost, provider, customDns)
            if (!resolved.isNullOrEmpty()) {
                "DNS Working! Resolved $testHost to ${resolved.first().hostAddress} via $provider"
            } else {
                "DNS Query failed. Check network or custom address."
            }
        } catch (e: Exception) {
            "DNS Error: ${e.message}"
        }
    }

    /**
     * Create an OkHttp Dns implementation backed by DnsManager
     */
    fun createOkHttpDns(getSelectedDns: () -> String, getCustomDns: () -> String): Dns {
        return object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val provider = getSelectedDns()
                if (provider == "Default (System DNS)") {
                    return Dns.SYSTEM.lookup(hostname)
                }

                // Check cache first
                val cached = cache[hostname]
                if (cached != null && System.currentTimeMillis() < cached.second) {
                    return cached.first
                }

                val endpoint = getDoHEndpoint(provider, getCustomDns())
                if (endpoint == null) return Dns.SYSTEM.lookup(hostname)

                try {
                    val url = Uri.parse(endpoint).buildUpon()
                        .appendQueryParameter("name", hostname)
                        .appendQueryParameter("type", "A")
                        .build()
                        .toString()

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/dns-json")
                        .build()

                    rawClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyString = response.body?.string()
                            if (bodyString != null) {
                                val json = JSONObject(bodyString)
                                if (json.has("Answer")) {
                                    val answerArray = json.getJSONArray("Answer")
                                    val result = mutableListOf<InetAddress>()
                                    var ttl = 300L
                                    for (i in 0 until answerArray.length()) {
                                        val record = answerArray.getJSONObject(i)
                                        if (record.optInt("type", -1) == 1) {
                                            val ip = record.getString("data")
                                            result.add(InetAddress.getByName(ip))
                                            ttl = record.optLong("TTL", 300L).coerceAtLeast(60L)
                                        }
                                    }
                                    if (result.isNotEmpty()) {
                                        cache[hostname] = Pair(result, System.currentTimeMillis() + (ttl * 1000L))
                                        return result
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Sync DoH lookup fallback: ${e.message}")
                }

                return Dns.SYSTEM.lookup(hostname)
            }
        }
    }
}
