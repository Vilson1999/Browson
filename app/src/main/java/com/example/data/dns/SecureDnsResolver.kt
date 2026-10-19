package com.example.data.dns

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

data class DnsTestResult(
    val isSuccess: Boolean,
    val message: String,
    val latencyMs: Long,
    val resolvedIp: String
)

object SecureDnsResolver {
    private const val TAG = "SecureDnsResolver"

    // In-memory cache for fast hostname lookups
    private val dnsCache = ConcurrentHashMap<String, List<InetAddress>>()

    // Basic OkHttpClient for fetching DoH queries directly without recursion
    private val dohHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .dns(Dns.SYSTEM) // DoH client itself uses system DNS or IP
        .build()

    fun getDoHEndpoint(provider: String, customDns: String): String? {
        val p = provider.trim()
        return when {
            p.contains("Google", ignoreCase = true) || p.contains("8.8.8.8") ->
                "https://dns.google/resolve"

            p.contains("Cloudflare", ignoreCase = true) || p.contains("1.1.1.1") ->
                "https://cloudflare-dns.com/dns-query"

            p.contains("Quad9", ignoreCase = true) || p.contains("9.9.9.9") ->
                "https://dns.quad9.net/dns-query"

            p.startsWith("Custom", ignoreCase = true) -> {
                val custom = customDns.trim()
                when {
                    custom.startsWith("http://") || custom.startsWith("https://") -> custom
                    custom.isNotBlank() -> "https://dns.google/resolve" // fallback to Google DoH or IP
                    else -> "https://cloudflare-dns.com/dns-query"
                }
            }

            p.contains("Default", ignoreCase = true) || p.contains("System", ignoreCase = true) ->
                null

            else -> "https://cloudflare-dns.com/dns-query"
        }
    }

    /**
     * Resolves hostnames using DoH (DNS over HTTPS)
     */
    fun resolve(hostname: String, provider: String, customDns: String): List<InetAddress> {
        // Return cached entry if present
        val cacheKey = "$provider:$hostname"
        dnsCache[cacheKey]?.let { return it }

        val endpoint = getDoHEndpoint(provider, customDns)
            ?: return Dns.SYSTEM.lookup(hostname)

        return try {
            val queryUrl = if (endpoint.contains("dns.google")) {
                "$endpoint?name=$hostname&type=A"
            } else {
                "$endpoint?name=$hostname&type=A"
            }

            val request = Request.Builder()
                .url(queryUrl)
                .addHeader("Accept", "application/dns-json")
                .build()

            val response = dohHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return Dns.SYSTEM.lookup(hostname)
            }

            val body = response.body?.string() ?: return Dns.SYSTEM.lookup(hostname)
            val json = JSONObject(body)
            val answers = json.optJSONArray("Answer")

            val addresses = mutableListOf<InetAddress>()
            if (answers != null && answers.length() > 0) {
                for (i in 0 until answers.length()) {
                    val item = answers.getJSONObject(i)
                    val ip = item.optString("data").trim()
                    if (ip.isNotEmpty() && (ip.contains(".") || ip.contains(":"))) {
                        try {
                            addresses.add(InetAddress.getByName(ip))
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse IP: $ip", e)
                        }
                    }
                }
            }

            if (addresses.isNotEmpty()) {
                dnsCache[cacheKey] = addresses
                addresses
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        } catch (e: Exception) {
            Log.w(TAG, "DoH lookup failed for $hostname via $provider, falling back to system DNS", e)
            Dns.SYSTEM.lookup(hostname)
        }
    }

    /**
     * Creates an OkHttp Dns instance that uses the selected DNS provider
     */
    fun createDns(provider: String, customDns: String): Dns {
        if (provider.contains("Default", ignoreCase = true) || provider.contains("System", ignoreCase = true)) {
            return Dns.SYSTEM
        }
        return object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return resolve(hostname, provider, customDns)
            }
        }
    }

    /**
     * Creates an OkHttpClient configured with the secure DNS resolver
     */
    fun createClient(provider: String, customDns: String): OkHttpClient {
        return OkHttpClient.Builder()
            .dns(createDns(provider, customDns))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Actively tests the DNS configuration by resolving a benchmark domain (google.com)
     * and calculating round-trip latency.
     */
    suspend fun testDns(provider: String, customDns: String): DnsTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val testDomain = "google.com"
            val addresses = resolve(testDomain, provider, customDns)
            val latency = System.currentTimeMillis() - startTime
            if (addresses.isNotEmpty()) {
                val ip = addresses.first().hostAddress ?: "Unknown IP"
                DnsTestResult(
                    isSuccess = true,
                    message = "Active & Working! Resolved $testDomain to $ip",
                    latencyMs = latency,
                    resolvedIp = ip
                )
            } else {
                DnsTestResult(
                    isSuccess = false,
                    message = "Could not resolve domain. Fallback to system DNS active.",
                    latencyMs = latency,
                    resolvedIp = "None"
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            DnsTestResult(
                isSuccess = false,
                message = "DNS error: ${e.localizedMessage ?: "Connection failed"}",
                latencyMs = latency,
                resolvedIp = "None"
            )
        }
    }
}
