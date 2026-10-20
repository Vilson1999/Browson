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
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.URLDecoder
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
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.lastIndex) {
            size /= 1024
            unitIndex++
        }
        return String.format(java.util.Locale.US, "%.1f %s", size, units[unitIndex])
    }
}

/**
 * SmartDownloadEngine:
 * - Multi-threaded segmented downloading (1 to 4 threads per file)
 * - Simultaneous download concurrency limit (1 to 4 files at a time)
 * - Pause / Resume support with HTTP Range headers
 * - Automatic file name collision avoidance and MIME detection
 * - StateFlow emission for responsive Compose UI
 */
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
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val mutex = Mutex()

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private val runningJobs = ConcurrentHashMap<String, Job>()
    private val partProgressMap = ConcurrentHashMap<String, LongArray>()

    val activeDownloadsCount: StateFlow<Int> = MutableStateFlow(0).apply {
        scope.launch {
            _tasks.collect { list ->
                val active = list.count { it.status == DownloadStatus.DOWNLOADING }
                this@apply.value = active
            }
        }
    }

    /**
     * Enqueues a new download
     */
    fun enqueue(
        url: String,
        suggestedFileName: String? = null,
        mimeType: String? = null,
        contentDisposition: String? = null
    ): String {
        val fileName = sanitizeFileName(suggestedFileName, contentDisposition, url, mimeType)
        val downloadFolder = File(repository.getDownloadFolder()).apply { mkdirs() }
        val targetFile = getUniqueFile(downloadFolder, fileName)

        val task = DownloadTask(
            url = url,
            fileName = targetFile.name,
            filePath = targetFile.absolutePath,
            threads = repository.getDownloadThreads(),
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

    /**
     * Pause a running download
     */
    fun pauseDownload(id: String) {
        scope.launch {
            runningJobs[id]?.cancel()
            runningJobs.remove(id)
            updateTask(id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0L) }
            processQueue()
        }
    }

    /**
     * Resume a paused or failed download
     */
    fun resumeDownload(id: String) {
        scope.launch {
            updateTask(id) { it.copy(status = DownloadStatus.QUEUED, errorMessage = null) }
            processQueue()
        }
    }

    /**
     * Cancel and delete a download
     */
    fun cancelDownload(id: String, deleteFile: Boolean = true) {
        scope.launch {
            runningJobs[id]?.cancel()
            runningJobs.remove(id)

            val task = _tasks.value.find { it.id == id }
            if (task != null && deleteFile) {
                try {
                    val file = File(task.filePath)
                    if (file.exists()) file.delete()
                    // Delete any temp parts
                    (0..4).forEach { i ->
                        val part = File(task.filePath + ".part$i")
                        if (part.exists()) part.delete()
                    }
                } catch (_: Exception) {}
            }

            mutex.withLock {
                _tasks.value = _tasks.value.filter { it.id != id }
            }
            processQueue()
        }
    }

    /**
     * Retries a failed download
     */
    fun retryDownload(id: String) {
        resumeDownload(id)
    }

    /**
     * Process pending queue based on simultaneousDownloads limit
     */
    private suspend fun processQueue() {
        mutex.withLock {
            val maxSimultaneous = repository.getSimultaneousDownloads()
            val currentlyDownloading = _tasks.value.count { it.status == DownloadStatus.DOWNLOADING }
            val availableSlots = maxSimultaneous - currentlyDownloading

            if (availableSlots > 0) {
                val nextTasks = _tasks.value
                    .filter { it.status == DownloadStatus.QUEUED }
                    .take(availableSlots)

                for (task in nextTasks) {
                    startTaskExecution(task)
                }
            }
        }
    }

    /**
     * Executes download of a single task
     */
    private fun startTaskExecution(task: DownloadTask) {
        updateTask(task.id) { it.copy(status = DownloadStatus.DOWNLOADING) }

        val job = scope.launch {
            var lastDownloaded = task.downloadedBytes
            var lastTime = System.currentTimeMillis()

            try {
                // 1. Probe URL to check size and range support
                val probeRequest = Request.Builder()
                    .url(task.url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .build()

                var contentLength = task.totalBytes
                var supportsRange = false

                withContext(Dispatchers.IO) {
                    try {
                        val response = okHttpClient.newCall(probeRequest).execute()
                        response.use { resp ->
                            val lenHeader = resp.header("Content-Length")?.toLongOrNull()
                            if (lenHeader != null && lenHeader > 0) {
                                contentLength = lenHeader
                            }
                            val acceptRanges = resp.header("Accept-Ranges")
                            supportsRange = acceptRanges.equals("bytes", ignoreCase = true) || resp.code == 206
                        }
                    } catch (_: Exception) {
                        // Fallback will attempt standard stream
                    }
                }

                val threadCount = task.threads.coerceIn(1, 4)
                updateTask(task.id) { it.copy(totalBytes = contentLength, threads = threadCount) }

                // 2. Decide download strategy: multi-threaded segmented vs single stream
                if (threadCount > 1 && supportsRange && contentLength > 512 * 1024) {
                    downloadSegmented(task, contentLength, threadCount)
                } else {
                    downloadSingleStream(task, contentLength)
                }

                // Completed!
                updateTask(task.id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = if (contentLength > 0) contentLength else it.downloadedBytes,
                        speedBytesPerSec = 0L
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    updateTask(task.id) {
                        it.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = e.localizedMessage ?: "Download failed",
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

    /**
     * Multi-threaded segmented download:
     * Splits file into N byte ranges, downloads simultaneously, and combines parts when done.
     */
    private suspend fun downloadSegmented(task: DownloadTask, totalBytes: Long, threads: Int) = withContext(Dispatchers.IO) {
        val partFiles = Array(threads) { File(task.filePath + ".part$it") }
        val chunkSize = totalBytes / threads
        val progressArray = LongArray(threads) { 0L }
        partProgressMap[task.id] = progressArray

        // Launch monitor ticker for progress & speed
        val monitorJob = scope.launch {
            var prevDownloaded = 0L
            while (isActive) {
                delay(400)
                val currentDownloaded = progressArray.sum()
                val speed = ((currentDownloaded - prevDownloaded) * 2.5).toLong().coerceAtLeast(0L)
                prevDownloaded = currentDownloaded
                updateTask(task.id) {
                    it.copy(
                        downloadedBytes = currentDownloaded,
                        speedBytesPerSec = speed
                    )
                }
            }
        }

        val threadJobs = (0 until threads).map { index ->
            val startByte = index * chunkSize
            val endByte = if (index == threads - 1) totalBytes - 1 else (index + 1) * chunkSize - 1

            scope.launch(Dispatchers.IO) {
                val partFile = partFiles[index]
                var currentStart = startByte
                if (partFile.exists()) {
                    currentStart += partFile.length()
                }

                if (currentStart <= endByte) {
                    val request = Request.Builder()
                        .url(task.url)
                        .header("Range", "bytes=$currentStart-$endByte")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0 Mobile Safari/537.36")
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful && response.code != 206) {
                        throw Exception("HTTP Error: ${response.code}")
                    }

                    val body = response.body ?: throw Exception("Empty body")
                    val output = FileOutputStream(partFile, true)
                    val buffer = ByteArray(32 * 1024)
                    body.byteStream().use { input ->
                        output.use { out ->
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                if (!isActive) break
                                out.write(buffer, 0, bytesRead)
                                progressArray[index] = partFile.length()
                            }
                        }
                    }
                } else {
                    progressArray[index] = partFile.length()
                }
            }
        }

        // Wait for all threads to complete
        threadJobs.forEach { it.join() }
        monitorJob.cancel()

        // Combine all part files into final destination file
        val targetFile = File(task.filePath)
        if (targetFile.exists()) targetFile.delete()

        FileOutputStream(targetFile).use { finalOut ->
            partFiles.forEach { part ->
                if (part.exists()) {
                    FileInputStream(part).use { partIn ->
                        partIn.copyTo(finalOut, bufferSize = 64 * 1024)
                    }
                    part.delete()
                }
            }
        }
    }

    /**
     * Single-stream download (for servers without range support or when threads = 1)
     */
    private suspend fun downloadSingleStream(task: DownloadTask, totalBytes: Long) = withContext(Dispatchers.IO) {
        val targetFile = File(task.filePath)
        val resumeFrom = if (targetFile.exists()) targetFile.length() else 0L

        val reqBuilder = Request.Builder()
            .url(task.url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0 Mobile Safari/537.36")

        if (resumeFrom > 0 && totalBytes > resumeFrom) {
            reqBuilder.header("Range", "bytes=$resumeFrom-")
        }

        val response = okHttpClient.newCall(reqBuilder.build()).execute()
        if (!response.isSuccessful && response.code != 206) {
            throw Exception("HTTP Error: ${response.code}")
        }

        val body = response.body ?: throw Exception("Empty body")
        val isAppending = response.code == 206 && resumeFrom > 0
        val outputStream = FileOutputStream(targetFile, isAppending)

        val buffer = ByteArray(32 * 1024)
        var downloaded = if (isAppending) resumeFrom else 0L
        var lastTime = System.currentTimeMillis()
        var lastBytes = downloaded

        body.byteStream().use { input ->
            outputStream.use { out ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) break
                    out.write(buffer, 0, bytesRead)
                    downloaded += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastTime >= 400) {
                        val speed = ((downloaded - lastBytes) * 1000 / (now - lastTime)).coerceAtLeast(0L)
                        lastTime = now
                        lastBytes = downloaded
                        updateTask(task.id) {
                            it.copy(downloadedBytes = downloaded, speedBytesPerSec = speed)
                        }
                    }
                }
            }
        }
    }

    private fun updateTask(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.id == id) transform(it) else it }
    }

    /**
     * Opens a completed download with an appropriate external viewer
     */
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
            // Unable to find activity for mime type
        }
    }

    /**
     * Share a downloaded file
     */
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
        } catch (_: Exception) {}
    }

    private fun sanitizeFileName(
        suggested: String?,
        contentDisposition: String?,
        url: String,
        mimeType: String?
    ): String {
        // Try content disposition
        if (!contentDisposition.isNullOrBlank()) {
            val cdMatch = Regex("filename\\*?=['\"]?(?:UTF-8'')?([^;'\"]+)['\"]?", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
            if (cdMatch != null) {
                val name = cdMatch.groupValues[1].trim()
                if (name.isNotBlank()) return cleanFileName(URLDecoder.decode(name, "UTF-8"))
            }
        }

        // Try suggested name
        if (!suggested.isNullOrBlank()) {
            return cleanFileName(suggested)
        }

        // Try extracting from URL
        try {
            val path = Uri.parse(url).lastPathSegment
            if (!path.isNullOrBlank() && path.contains(".")) {
                return cleanFileName(URLDecoder.decode(path, "UTF-8"))
            }
        } catch (_: Exception) {}

        // Fallback with mime extension
        val ext = getExtensionFromMime(mimeType) ?: ".bin"
        return "download_${System.currentTimeMillis()}$ext"
    }

    private fun cleanFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    private fun getUniqueFile(folder: File, fileName: String): File {
        var file = File(folder, fileName)
        if (!file.exists()) return file

        val nameWithoutExt = fileName.substringBeforeLast('.', fileName)
        val ext = if (fileName.contains('.')) "." + fileName.substringAfterLast('.') else ""

        var counter = 1
        while (file.exists()) {
            file = File(folder, "$nameWithoutExt ($counter)$ext")
            counter++
        }
        return file
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }

    private fun getExtensionFromMime(mime: String?): String? {
        if (mime == null) return null
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        return if (ext != null) ".$ext" else null
    }
}
