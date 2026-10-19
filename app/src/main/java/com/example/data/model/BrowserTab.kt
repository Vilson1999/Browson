package com.example.data.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = HOME_URL,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isHome: Boolean get() = url.isBlank() || url == HOME_URL

    val isSecure: Boolean get() = url.startsWith("https://", ignoreCase = true)

    val displayHost: String get() {
        if (isHome) return if (isIncognito) "Incognito Home" else "Search or enter address"
        return try {
            val uri = java.net.URI(url)
            uri.host?.removePrefix("www.") ?: run {
                val clean = url.substringAfter("://").substringBefore("/").substringBefore(":")
                clean.removePrefix("www.")
            }
        } catch (_: Exception) {
            try {
                val clean = url.substringAfter("://").substringBefore("/").substringBefore(":")
                clean.removePrefix("www.")
            } catch (_: Exception) {
                url
            }
        }
    }

    companion object {
        const val HOME_URL = "about:home"
    }
}
