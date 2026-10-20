package com.example.data.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadTask(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val filePath: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val threads: Int = 1,
    val mimeType: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f

    val isRunning: Boolean
        get() = status == DownloadStatus.DOWNLOADING

    val isFinished: Boolean
        get() = status == DownloadStatus.COMPLETED

    val formattedDownloadedSize: String
        get() = formatSize(downloadedBytes)

    val formattedTotalSize: String
        get() = if (totalBytes > 0) formatSize(totalBytes) else "Unknown"

    val formattedSpeed: String
        get() = "${formatSize(speedBytesPerSec)}/s"

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.lastIndex) {
            size /= 1024.0
            unitIndex++
        }
        return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
    }
}

class SmartDownloadEngine(
    private val context: Context,
    private val repository: BrowserRepository,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private val runningJobs = ConcurrentHashMap<String, Job>()
    private val partProgressMap = ConcurrentHashMap<String, LongArray>()

    val activeDownloadsCount: StateFlow<Int> = MutableStateFlow(0)

    init {
        scope.launch {
            _tasks.collect { taskList ->
                (activeDownloadsCount as MutableStateFlow<Int>).value = taskList.count { it.status == DownloadStatus.DOWNLOADING }
            }
        }
    }

    fun enqueue(
        url: String,
        suggestedFileName: String? = null,
        mimeType: String? = null,
        contentDisposition: String? = null
    ): String {
        val downloadFolder = File(repository.getDownloadFolder()).apply { mkdirs() }
        val fileName = sanitizeFileName(suggestedFileName, contentDisposition, url, mimeType)
        val targetFile = getUniqueFile(downloadFolder, fileName)
        val task = DownloadTask(
            url = url,
            fileName = targetFile.name,
            filePath = targetFile.absolutePath,
            threads = repository.getDownloadThreads().coerceIn(1, 4),
            mimeType = mimeType
        )

        scope.launch {
            mutex.withLock {
                _tasks.value = listOf(task) + _tasks.value
            }
            processQueue()
        }

        return task.id
    }

    fun pauseDownload(id: String) {
        scope.launch {
            runningJobs[id]?.cancel()
            runningJobs.remove(id)
            updateTask(id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L) }
            processQueue()
        }
    }

    fun resumeDownload(id: String) {
        scope.launch {
            updateTask(id) { it.copy(status = DownloadStatus.QUEUED, errorMessage = null) }
            processQueue()
        }
    }

    fun cancelDownload(id: String, deleteFile: Boolean = true) {
        scope.launch {
            runningJobs[id]?.cancel()
            runningJobs.remove(id)

            val task = _tasks.value.find { it.id == id }
            if (task != null && deleteFile) {
                try {
                    val file = File(task.filePath)
                    if (file.exists()) file.delete()
                    for (index in 0..4) {
                        val part = File(task.filePath + ".part$index")
                        if (part.exists()) part.delete()
                    }
                    val segmentDir = File(task.filePath + ".hls")
                    if (segmentDir.exists()) segmentDir.deleteRecursively()
                } catch (_: Exception) {
                }
            }

            mutex.withLock {
                _tasks.value = _tasks.value.filter { it.id != id }
            }
            processQueue()
        }
    }

    fun retryDownload(id: String) {
        resumeDownload(id)
    }

    private suspend fun processQueue() {
        mutex.withLock {
            val maxSimultaneous = repository.getSimultaneousDownloads().coerceIn(1, 4)
            val downloading = _tasks.value.count { it.status == DownloadStatus.DOWNLOADING }
            val slots = maxSimultaneous - downloading
            if (slots <= 0) return

            val next = _tasks.value
                .filter { it.status == DownloadStatus.QUEUED }
                .take(slots)

            for (task in next) {
                startTaskExecution(task)
            }
        }
    }

    private fun startTaskExecution(task: DownloadTask) {
        updateTask(task.id) { it.copy(status = DownloadStatus.DOWNLOADING) }

        val job = scope.launch {
            try {
                val metadata = probeTask(task)
                val finalThreadCount = metadata.threadCount.coerceIn(1, 4)
                updateTask(task.id) {
                    it.copy(
                        totalBytes = metadata.totalBytes,
                        threads = finalThreadCount,
                        status = DownloadStatus.DOWNLOADING
                    )
                }

                if (metadata.isHlsManifest) {
                    downloadHlsManifest(task)
                } else if (finalThreadCount > 1 && metadata.supportsRange && metadata.totalBytes > 512 * 1024) {
                    downloadSegmented(task, metadata.totalBytes, finalThreadCount)
                } else {
                    downloadSingleStream(task, metadata.totalBytes)
                }

                updateTask(task.id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = if (metadata.totalBytes > 0) metadata.totalBytes else it.downloadedBytes,
                        speedBytesPerSec = 0L,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    updateTask(task.id) {
                        it.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = e.message ?: "Download failed",
                            speedBytesPerSec = 0L
                        )
                    }
                }
            } finally {
                runningJobs.remove(task.id)
                partProgressMap.remove(task.id)
                processQueue()
            }
        }

        runningJobs[task.id] = job
    }

    data class ProbeResult(
        val totalBytes: Long,
        val supportsRange: Boolean,
        val isHlsManifest: Boolean,
        val threadCount: Int
    )

    private suspend fun probeTask(task: DownloadTask): ProbeResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(task.url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36"
            )
            .build()

        var totalBytes = task.totalBytes
        var supportsRange = false
        var isHlsManifest = false
        var contentType: String? = null

        try {
            val response = okHttpClient.newCall(request).execute()
            response.use { resp ->
                contentType = resp.header("Content-Type")
                isHlsManifest = resp.header("Content-Type")?.contains("application/vnd.apple.mpegurl", ignoreCase = true) == true ||
                    task.url.lowercase(Locale.US).endsWith(".m3u8") ||
                    task.url.contains("m3u8", ignoreCase = true)

                val lenHeader = resp.header("Content-Length")?.toLongOrNull()
                if (lenHeader != null && lenHeader > 0) totalBytes = lenHeader
                supportsRange = resp.header("Accept-Ranges")?.equals("bytes", ignoreCase = true) == true || resp.code == 206
            }
        } catch (_: Exception) {
            isHlsManifest = task.url.lowercase(Locale.US).endsWith(".m3u8") || task.url.contains("m3u8", ignoreCase = true)
        }

        val threadCount = if (task.threads > 0) task.threads else repository.getDownloadThreads().coerceIn(1, 4)
        ProbeResult(
            totalBytes = totalBytes,
            supportsRange = supportsRange,
            isHlsManifest = isHlsManifest,
            threadCount = threadCount
        )
    }

    private suspend fun downloadSingleStream(task: DownloadTask, totalBytes: Long) = withContext(Dispatchers.IO) {
        val targetFile = File(task.filePath)
        val existingBytes = if (targetFile.exists()) targetFile.length() else 0L
        val reqBuilder = Request.Builder()
            .url(task.url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36"
            )

        if (existingBytes > 0L && totalBytes > existingBytes) {
            reqBuilder.header("Range", "bytes=$existingBytes-")
        }

        val response = okHttpClient.newCall(reqBuilder.build()).execute()
        if (!response.isSuccessful && response.code != 206) {
            throw IllegalStateException("HTTP ${response.code} while downloading ${task.url}")
        }

        val body = response.body ?: throw IllegalStateException("Empty response body")
        val appendMode = response.code == 206 && existingBytes > 0L
        val outputStream = FileOutputStream(targetFile, appendMode)
        val buffer = ByteArray(32 * 1024)
        var downloaded = existingBytes
        var lastReportAt = System.currentTimeMillis()
        var lastDownloaded = existingBytes

        body.byteStream().use { input ->
            outputStream.use { out ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) break
                    out.write(buffer, 0, bytesRead)
                    downloaded += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastReportAt >= 400) {
                        val speed = ((downloaded - lastDownloaded) * 1000L / (now - lastReportAt)).coerceAtLeast(0L)
                        lastReportAt = now
                        lastDownloaded = downloaded
                        updateTask(task.id) {
                            it.copy(downloadedBytes = downloaded, speedBytesPerSec = speed)
                        }
                    }
                }
            }
        }

        if (!isActive) {
            updateTask(task.id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L) }
            return@withContext
        }
    }

    private suspend fun downloadSegmented(task: DownloadTask, totalBytes: Long, threads: Int) = withContext(Dispatchers.IO) {
        val partFiles = Array(threads) { File(task.filePath + ".part$it") }
        val chunkSize = if (totalBytes > 0) totalBytes / threads else 0L
        val progressArray = LongArray(threads) { 0L }
        partProgressMap[task.id] = progressArray

        val monitorJob = scope.launch {
            var previous = 0L
            while (isActive) {
                delay(400)
                val current = progressArray.sum()
                val speed = ((current - previous) * 2L).coerceAtLeast(0L)
                previous = current
                updateTask(task.id) {
                    it.copy(downloadedBytes = current, speedBytesPerSec = speed)
                }
            }
        }

        val workerJobs = (0 until threads).map { index ->
            scope.launch(Dispatchers.IO) {
                val partFile = partFiles[index]
                val start = if (totalBytes > 0) index * chunkSize else 0L
                val end = if (totalBytes > 0) {
                    if (index == threads - 1) totalBytes - 1 else (index + 1) * chunkSize - 1
                } else {
                    -1L
                }

                var resumeFrom = 0L
                if (partFile.exists()) {
                    resumeFrom = partFile.length()
                }

                val currentStart = if (end >= 0) start + resumeFrom else 0L
                if (end >= 0 && currentStart <= end) {
                    val request = Request.Builder()
                        .url(task.url)
                        .header("Range", "bytes=$currentStart-$end")
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36"
                        )
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful && response.code != 206) {
                        throw IllegalStateException("HTTP ${response.code} for part $index")
                    }

                    val body = response.body ?: throw IllegalStateException("Empty part body")
                    val out = FileOutputStream(partFile, true)
                    val buffer = ByteArray(32 * 1024)

                    body.byteStream().use { input ->
                        out.use { output ->
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                if (!isActive) break
                                output.write(buffer, 0, bytesRead)
                                progressArray[index] = partFile.length()
                            }
                        }
                    }
                }
            }
        }

        workerJobs.forEach { it.join() }
        monitorJob.cancel()

        val finalFile = File(task.filePath)
        if (finalFile.exists()) finalFile.delete()

        FileOutputStream(finalFile).use { output ->
            partFiles.forEach { partFile ->
                if (partFile.exists()) {
                    FileInputStream(partFile).use { input ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                    }
                    partFile.delete()
                }
            }
        }

        if (!isActive) {
            updateTask(task.id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L) }
        }
    }

    private suspend fun downloadHlsManifest(task: DownloadTask) = withContext(Dispatchers.IO) {
        val manifestText = fetchText(task.url)
        val baseUrl = task.url.substringBeforeLast('/') + "/"
        val variantUrl = selectBestVariant(manifestText, baseUrl) ?: manifestText
        val segmentUrls = parseSegments(variantUrl, baseUrl)

        val outputFile = File(task.filePath)
        val segmentFolder = File(outputFile.parentFile, outputFile.nameWithoutExtension + "_segments")
        segmentFolder.mkdirs()

        var segmentIndex = 0
        var downloadedTotal = 0L
        for (segmentUrl in segmentUrls) {
            if (!isActive) {
                updateTask(task.id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L) }
                return@withContext
            }

            val segmentFile = File(segmentFolder, "segment_${segmentIndex}.ts")
            if (!segmentFile.exists()) {
                val response = okHttpClient.newCall(Request.Builder().url(segmentUrl).build()).execute()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Failed to fetch HLS segment: ${response.code}")
                }
                val body = response.body ?: throw IllegalStateException("Empty HLS segment body")
                val output = FileOutputStream(segmentFile)
                body.byteStream().use { input ->
                    output.use { out ->
                        input.copyTo(out)
                    }
                }
                downloadedTotal += segmentFile.length()
                updateTask(task.id) {
                    it.copy(downloadedBytes = downloadedTotal, totalBytes = downloadedTotal, speedBytesPerSec = segmentFile.length())
                }
            }
            segmentIndex++
        }

        if (!outputFile.exists()) {
            FileOutputStream(outputFile).use { output ->
                val segmentFiles = segmentFolder.listFiles()?.sortedBy { it.name }
                segmentFiles?.forEach { segmentFile ->
                    FileInputStream(segmentFile).use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private suspend fun fetchText(url: String): String = withContext(Dispatchers.IO) {
        val response = okHttpClient.newCall(Request.Builder().url(url).build()).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to fetch manifest: ${response.code}")
        }
        val body = response.body ?: throw IllegalStateException("Missing manifest body")
        body.string()
    }

    private fun selectBestVariant(manifest: String, baseUrl: String): String? {
        val streamRegex = Regex("#EXT-X-STREAM-INF[^
]*?(?:BANDWIDTH=(\\d+))?[^
]*\\s+([^\\s]+)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val variants = mutableListOf<Pair<Long, String>>()

        val matches = streamRegex.findAll(manifest)
        for (match in matches) {
            val bandwidth = match.groupValues.getOrNull(1)?.toLongOrNull() ?: 0L
            val ref = match.groupValues.getOrNull(2)?.trim() ?: continue
            if (ref.isNotEmpty()) {
                variants.add(bandwidth to resolveAbsoluteUrl(baseUrl, ref))
            }
        }

        return if (variants.isNotEmpty()) {
            variants.maxByOrNull { it.first }?.second
        } else {
            null
        }
    }

    private fun parseSegments(variantUrl: String, baseUrl: String): List<String> {
        val text = if (variantUrl == baseUrl || variantUrl.isBlank()) "" else try {
            fetchText(variantUrl)
        } catch (_: Exception) {
            ""
        }

        if (text.isBlank()) return emptyList()

        val segments = mutableListOf<String>()
        for (line in text.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            if (!trimmed.startsWith("http")) {
                segments.add(resolveAbsoluteUrl(baseUrl, trimmed))
            } else {
                segments.add(trimmed)
            }
        }
        return segments.distinct()
    }

    private fun resolveAbsoluteUrl(baseUrl: String, relative: String): String {
        return try {
            if (relative.startsWith("http://", ignoreCase = true) || relative.startsWith("https://", ignoreCase = true)) {
                relative
            } else {
                val base = Uri.parse(baseUrl)
                val resolved = Uri.withAppendedPath(base, relative)
                resolved.toString()
            }
        } catch (_: Exception) {
            relative
        }
    }

    private fun updateTask(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value.map { current -> if (current.id == id) transform(current) else current }
    }

    fun openFile(task: DownloadTask) {
        try {
            val file = File(task.filePath)
            if (!file.exists()) return

            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (_: Exception) {
                Uri.fromFile(file)
            }

            val mime = task.mimeType ?: getMimeTypeFromExtension(file.extension)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun shareFile(task: DownloadTask) {
        try {
            val file = File(task.filePath)
            if (!file.exists()) return

            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (_: Exception) {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = task.mimeType ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${task.fileName}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }

    private fun sanitizeFileName(
        suggested: String?,
        contentDisposition: String?,
        url: String,
        mimeType: String?
    ): String {
        if (!contentDisposition.isNullOrBlank()) {
            val cdMatch = Regex("filename\\*?=['\"]?(?:UTF-8'')?([^;\"']+)[\"']?", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
            if (cdMatch != null) {
                val name = cdMatch.groupValues[1].trim()
                if (name.isNotBlank()) return cleanFileName(URLDecoder.decode(name, "UTF-8"))
            }
        }

        if (!suggested.isNullOrBlank()) {
            return cleanFileName(suggested)
        }

        try {
            val path = Uri.parse(url).lastPathSegment
            if (!path.isNullOrBlank() && path.contains(".")) {
                return cleanFileName(URLDecoder.decode(path, "UTF-8"))
            }
        } catch (_: Exception) {
        }

        val extension = if (url.lowercase(Locale.US).contains("m3u8")) ".ts" else getExtensionFromMime(mimeType) ?: ".bin"
        return "download_${System.currentTimeMillis()}$extension"
    }

    private fun cleanFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    private fun getUniqueFile(folder: File, fileName: String): File {
        var file = File(folder, fileName)
        if (!file.exists()) return file

        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = if (fileName.contains('.')) ".${fileName.substringAfterLast('.')}" else ""
        var counter = 1
        while (file.exists()) {
            file = File(folder, "$baseName ($counter)$extension")
            counter++
        }
        return file
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase(Locale.US))
    }

    private fun getExtensionFromMime(mime: String?): String? {
        if (mime == null) return null
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        return if (ext != null) ".$ext" else null
    }
}
