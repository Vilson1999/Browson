package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.BrowserDatabase
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HistoryEntity
import com.example.data.local.entities.SpeedDialEntity
import com.example.data.model.ToolbarActionItem
import com.example.data.model.ToolbarActionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BrowserRepository(private val context: Context) {
    private val database = BrowserDatabase.getInstance(context)
    private val historyDao = database.historyDao()
    private val bookmarkDao = database.bookmarkDao()
    private val speedDialDao = database.speedDialDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("operawave_prefs", Context.MODE_PRIVATE)

    private val _toolbarActionsFlow = MutableStateFlow(loadToolbarActions())
    val toolbarActionsFlow = _toolbarActionsFlow.asStateFlow()

    // History
    fun getHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    fun searchHistory(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)

    suspend fun recordHistory(url: String, title: String) = withContext(Dispatchers.IO) {
        if (url.isBlank() || url == "about:home" || url == "about:blank") return@withContext
        historyDao.insertHistory(
            HistoryEntity(
                url = url,
                title = if (title.isBlank()) url else title,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }

    // Bookmarks
    fun getBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun isBookmarked(url: String): Flow<Boolean> = bookmarkDao.isBookmarked(url)

    suspend fun toggleBookmark(url: String, title: String): Boolean = withContext(Dispatchers.IO) {
        val existing = bookmarkDao.getBookmarkByUrl(url)
        if (existing != null) {
            bookmarkDao.deleteBookmark(existing)
            false
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    url = url,
                    title = if (title.isBlank()) url else title,
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun updateBookmark(id: Long, title: String, url: String) = withContext(Dispatchers.IO) {
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        bookmarkDao.updateBookmark(id, title, finalUrl)
    }

    // Speed Dials
    fun getSpeedDials(): Flow<List<SpeedDialEntity>> = speedDialDao.getAllSpeedDials()

    suspend fun addSpeedDial(title: String, url: String, color: Long) = withContext(Dispatchers.IO) {
        val count = speedDialDao.getCount()
        speedDialDao.insertSpeedDial(
            SpeedDialEntity(
                title = title,
                url = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url",
                iconColor = color,
                sortOrder = count,
                isCustom = true
            )
        )
    }

    suspend fun updateSpeedDial(id: Long, title: String, url: String) = withContext(Dispatchers.IO) {
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        speedDialDao.updateSpeedDial(id, title, finalUrl)
    }

    suspend fun deleteSpeedDial(id: Long) = withContext(Dispatchers.IO) {
        speedDialDao.deleteSpeedDialById(id)
    }

    // Appearance, Theme & Wallpaper Preferences
    fun getAccentColor(): Long = prefs.getLong("theme_accent_color", 0xFFE5254AL)
    fun setAccentColor(color: Long) = prefs.edit().putLong("theme_accent_color", color).apply()

    fun getThemeMode(): String = prefs.getString("theme_mode", "system") ?: "system"
    fun setThemeMode(mode: String) = prefs.edit().putString("theme_mode", mode).apply()

    fun isAmoledDark(): Boolean = prefs.getBoolean("amoled_dark_enabled", false)
    fun setAmoledDark(enabled: Boolean) = prefs.edit().putBoolean("amoled_dark_enabled", enabled).apply()

    fun getWallpaper(): String = prefs.getString("home_wallpaper", "wp_system") ?: "wp_system"
    fun setWallpaper(wallpaper: String) = prefs.edit().putString("home_wallpaper", wallpaper).apply()

    // Privacy & Security Preferences
    fun isAdBlockerEnabled(): Boolean = prefs.getBoolean("ublock_ad_blocker_enabled", true)
    fun setAdBlockerEnabled(enabled: Boolean) = prefs.edit().putBoolean("ublock_ad_blocker_enabled", enabled).apply()

    fun getAdBlockerMode(): String = prefs.getString("ad_blocker_mode", "normal") ?: "normal"
    fun setAdBlockerMode(mode: String) {
        prefs.edit().putString("ad_blocker_mode", mode).putString("prev_ad_blocker_mode", mode).apply()
    }
    fun getPreviousAdBlockerMode(): String = prefs.getString("prev_ad_blocker_mode", "normal") ?: "normal"

    fun isSecureDnsEnabled(): Boolean = prefs.getBoolean("secure_dns_enabled", true)
    fun setSecureDnsEnabled(enabled: Boolean) = prefs.edit().putBoolean("secure_dns_enabled", enabled).apply()

    fun getSelectedDns(): String = prefs.getString("selected_dns_provider", "Cloudflare (1.1.1.1)") ?: "Cloudflare (1.1.1.1)"
    fun setSelectedDns(dns: String) {
        prefs.edit().putString("selected_dns_provider", dns).putString("prev_dns_provider", dns).apply()
    }
    fun getPreviousDnsProvider(): String = prefs.getString("prev_dns_provider", "Cloudflare (1.1.1.1)") ?: "Cloudflare (1.1.1.1)"

    fun getCustomDns(): String = prefs.getString("custom_dns_address", "") ?: ""
    fun setCustomDns(customDns: String) = prefs.edit().putString("custom_dns_address", customDns).apply()

    // Appearance Preferences
    fun getAnimationScale(): Float = prefs.getFloat("animation_scale", 1.0f)
    fun setAnimationScale(scale: Float) = prefs.edit().putFloat("animation_scale", scale).apply()

    // Start Page Preferences (Speed Dial, Bookmark, Recent)
    fun isShowSpeedDials(): Boolean = prefs.getBoolean("start_page_show_speed_dials", true)
    fun setShowSpeedDials(enabled: Boolean) = prefs.edit().putBoolean("start_page_show_speed_dials", enabled).apply()

    fun isShowBookmarks(): Boolean = prefs.getBoolean("start_page_show_bookmarks", true)
    fun setShowBookmarks(enabled: Boolean) = prefs.edit().putBoolean("start_page_show_bookmarks", enabled).apply()

    fun isShowRecent(): Boolean = prefs.getBoolean("start_page_show_recent", true)
    fun setShowRecent(enabled: Boolean) = prefs.edit().putBoolean("start_page_show_recent", enabled).apply()

    // Download Preferences (Simultaneous downloads 1-4, Threads 1-4, Folder)
    fun getSimultaneousDownloads(): Int = prefs.getInt("simultaneous_downloads", 2).coerceIn(1, 4)
    fun setSimultaneousDownloads(count: Int) = prefs.edit().putInt("simultaneous_downloads", count.coerceIn(1, 4)).apply()

    fun getDownloadThreads(): Int = prefs.getInt("download_threads", 2).coerceIn(1, 4)
    fun setDownloadThreads(threads: Int) = prefs.edit().putInt("download_threads", threads.coerceIn(1, 4)).apply()

    fun getDefaultDownloadFolder(): String {
        return try {
            val pub = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            pub?.absolutePath ?: context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: "/storage/emulated/0/Download"
        } catch (_: Exception) {
            "/storage/emulated/0/Download"
        }
    }

    fun getDownloadFolder(): String = prefs.getString("download_folder", null) ?: getDefaultDownloadFolder()
    fun setDownloadFolder(path: String) = prefs.edit().putString("download_folder", path).apply()

    // Toolbar Customization Persistence
    private fun loadToolbarActions(): List<ToolbarActionItem> {
        val saved = prefs.getString("toolbar_actions_order", null)
        if (saved.isNullOrBlank()) {
            return defaultToolbarActions()
        }
        val items = mutableListOf<ToolbarActionItem>()
        val entries = saved.split(";")
        for (entry in entries) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val actionType = try {
                    ToolbarActionType.valueOf(parts[0])
                } catch (_: Exception) {
                    null
                }
                val enabled = parts[1].toBooleanStrictOrNull() ?: true
                if (actionType != null) {
                    items.add(ToolbarActionItem(actionType, enabled))
                }
            }
        }
        // Ensure all types are represented
        ToolbarActionType.entries.forEach { type ->
            if (items.none { it.type == type }) {
                items.add(ToolbarActionItem(type, type.defaultEnabled))
            }
        }
        return items
    }

    fun saveToolbarActions(actions: List<ToolbarActionItem>) {
        val serialized = actions.joinToString(";") { "${it.type.name}:${it.isEnabled}" }
        prefs.edit().putString("toolbar_actions_order", serialized).apply()
        _toolbarActionsFlow.value = actions
    }

    fun resetToolbarActions() {
        val defaults = defaultToolbarActions()
        saveToolbarActions(defaults)
    }

    private fun defaultToolbarActions(): List<ToolbarActionItem> {
        return listOf(
            ToolbarActionItem(ToolbarActionType.BACK, true),
            ToolbarActionItem(ToolbarActionType.FORWARD, true),
            ToolbarActionItem(ToolbarActionType.HOME, true),
            ToolbarActionItem(ToolbarActionType.TABS, true),
            ToolbarActionItem(ToolbarActionType.ME, true),
            ToolbarActionItem(ToolbarActionType.BOOKMARKS, false),
            ToolbarActionItem(ToolbarActionType.HISTORY, false),
            ToolbarActionItem(ToolbarActionType.NEW_TAB, false),
            ToolbarActionItem(ToolbarActionType.RELOAD, false),
            ToolbarActionItem(ToolbarActionType.SHARE, false),
            ToolbarActionItem(ToolbarActionType.DESKTOP_MODE, false)
        )
    }
}
