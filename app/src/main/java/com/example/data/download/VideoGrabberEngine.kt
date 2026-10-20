package com.example.data.download

import java.net.URI

private const val HLS_MIME = "application/vnd.apple.mpegurl"
private const val DASH_MIME = "application/dash+xml"

data class DetectedVideoStream(
    val sourceUrl: String,
    val title: String = "Detected stream",
    val quality: String = "Auto",
    val mimeType: String? = null,
    val sourceType: String = "HLS"
) {
    val displayLabel: String
        get() = if (quality.isBlank() || quality == "Auto") title else "$title • $quality"
}

object VideoGrabberEngine {
    fun isHlsLike(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()
        return lower.contains(".m3u8") ||
            lower.contains(".m3u") ||
            lower.contains(".mpd") ||
            lower.contains("mpegurl") ||
            lower.contains("x-mpegurl") ||
            lower.contains("playlist") && lower.contains(".m3u")
    }

    fun detectFromUrl(url: String, playlistContent: String? = null): List<DetectedVideoStream> {
        if (url.isBlank()) return emptyList()

        val candidates = linkedSetOf<String>()
        if (isHlsLike(url)) {
            candidates += normalizeUrl(url)
        }

        playlistContent?.let { content ->
            extractPlaylistUrls(content, url).forEach { candidates += it }
        }

        return candidates.map { streamUrl ->
            val mimeType = when {
                streamUrl.lowercase().contains(".mpd") -> DASH_MIME
                else -> HLS_MIME
            }

            DetectedVideoStream(
                sourceUrl = streamUrl,
                title = deriveTitle(streamUrl),
                quality = inferQuality(streamUrl),
                mimeType = mimeType,
                sourceType = if (streamUrl.lowercase().contains(".mpd")) "DASH" else "HLS"
            )
        }.distinctBy { it.sourceUrl }
    }

    fun extractPlaylistUrls(playlistContent: String, baseUrl: String): List<String> {
        if (playlistContent.isBlank()) return emptyList()

        val matches = mutableSetOf<String>()
        val lines = playlistContent.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) continue

            val candidate = when {
                trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                trimmed.startsWith("/") -> resolve(baseUrl, trimmed)
                trimmed.startsWith("./") || trimmed.startsWith("../") -> resolve(baseUrl, trimmed)
                trimmed.contains(".m3u8") || trimmed.contains(".mpd") || trimmed.contains(".m3u") -> resolve(baseUrl, trimmed)
                else -> null
            }

            if (candidate != null && isHlsLike(candidate)) {
                matches += normalizeUrl(candidate)
            }
        }

        return matches.toList()
    }

    private fun normalizeUrl(rawUrl: String): String {
        return rawUrl.trim().replace("\\", "")
    }

    private fun resolve(baseUrl: String, relative: String): String {
        return try {
            URI(baseUrl).resolve(relative).toString()
        } catch (_: Exception) {
            relative
        }
    }

    private fun deriveTitle(url: String): String {
        return try {
            val path = URI(url).path ?: url
            val fileName = path.substringAfterLast('/').substringBefore('?')
            if (fileName.isBlank()) "Detected stream" else fileName
        } catch (_: Exception) {
            "Detected stream"
        }
    }

    private fun inferQuality(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("720") || lower.contains("720p") -> "720p"
            lower.contains("1080") || lower.contains("1080p") -> "1080p"
            lower.contains("480") || lower.contains("480p") -> "480p"
            lower.contains("360") || lower.contains("360p") -> "360p"
            lower.contains("240") || lower.contains("240p") -> "240p"
            lower.contains("2160") || lower.contains("2160p") -> "2160p"
            lower.contains("sd") -> "SD"
            lower.contains("hd") -> "HD"
            lower.contains("uhd") || lower.contains("4k") -> "4K"
            else -> "Auto"
        }
    }
}
