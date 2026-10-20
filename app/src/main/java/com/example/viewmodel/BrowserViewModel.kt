package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HistoryEntity
import com.example.data.local.entities.SpeedDialEntity
import com.example.data.model.BrowserTab
import com.example.data.model.ToolbarActionItem
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.regex.Pattern

sealed interface WebCommand {
    data object GoBack : WebCommand
    data object GoForward : WebCommand
    data object Reload : WebCommand
    data object StopLoading : WebCommand
    data class LoadUrl(val url: String) : WebCommand
    data class FindInPage(val query: String, val forward: Boolean = true) : WebCommand
    data object ClearMatches : WebCommand
    data object PrintPage : WebCommand
    data object SaveWebArchive : WebCommand
    data object TakeScreenshot : WebCommand
    data class SetBlockImages(val block: Boolean) : WebCommand
    data class SetNightMode(val night: Boolean) : WebCommand
}

data class BrowserUiState(
    val tabs: List<BrowserTab> = listOf(BrowserTab()),
    val activeTabId: String = "",
    val isTabSwitcherOpen: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val isBookmarksOpen: Boolean = false,
    val isCustomizeToolbarOpen: Boolean = false,
    val isAddSpeedDialOpen: Boolean = false,
    val isAddressEditing: Boolean = false,
    val addressInputText: String = "",
    val historySearchQuery: String = "",
    val isToolsMenuOpen: Boolean = false,
    val isFullScreen: Boolean = false,
    val isFindInPageOpen: Boolean = false,
    val findInPageQuery: String = "",
    val findInPageMatchesCount: Int = 0,
    val isAdBlockerEnabled: Boolean = true,
    val adBlockerMode: String = "normal", // "normal", "balance", "extreme"
    val isAdBlockerDialogOpen: Boolean = false,
    val isBlockImagesEnabled: Boolean = false,
    val isNightModeEnabled: Boolean = false,
    val isDnsDialogOpen: Boolean = false,
    val isSecureDnsEnabled: Boolean = true,
    val selectedDns: String = "Cloudflare (1.1.1.1)",
    val customDns: String = "",
    val animationScale: Float = 1.0f,
    val isSettingsOpen: Boolean = false,
    val activeSettingsSection: String? = null,
    val accentColor: Long = 0xFFE5254AL,
    val themeMode: String = "system",
    val isAmoledDark: Boolean = false,
    val wallpaper: String = "wp_system",
    val showSpeedDials: Boolean = true,
    val showBookmarks: Boolean = true,
    val showRecent: Boolean = true,
    val toastMessage: String? = null
) {
    val activeTab: BrowserTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: BrowserTab()
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BrowserRepository(application)

    private val initialTab = BrowserTab()
    private val _uiState = MutableStateFlow(
        BrowserUiState(
            tabs = listOf(initialTab),
            activeTabId = initialTab.id,
            accentColor = repository.getAccentColor(),
            themeMode = repository.getThemeMode(),
            isAmoledDark = repository.isAmoledDark(),
            wallpaper = repository.getWallpaper(),
            isAdBlockerEnabled = repository.isAdBlockerEnabled(),
            adBlockerMode = repository.getAdBlockerMode(),
            isSecureDnsEnabled = repository.isSecureDnsEnabled(),
            selectedDns = repository.getSelectedDns(),
            customDns = repository.getCustomDns(),
            animationScale = repository.getAnimationScale(),
            showSpeedDials = repository.isShowSpeedDials(),
            showBookmarks = repository.isShowBookmarks(),
            showRecent = repository.isShowRecent()
        )
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val toolbarActions: StateFlow<List<ToolbarActionItem>> = repository.toolbarActionsFlow

    val speedDials: StateFlow<List<SpeedDialEntity>> = repository.getSpeedDials()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _historyQuery = MutableStateFlow("")
    val history: StateFlow<List<HistoryEntity>> = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _webCommands = MutableSharedFlow<WebCommand>(extraBufferCapacity = 1)
    val webCommands = _webCommands.asSharedFlow()

    // Tab Management
    fun openNewTab(url: String = BrowserTab.HOME_URL, isIncognito: Boolean = false) {
        val newTab = BrowserTab(url = url, isIncognito = isIncognito)
        val updatedTabs = _uiState.value.tabs + newTab
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newTab.id,
            isTabSwitcherOpen = false,
            isAddressEditing = false,
            isToolsMenuOpen = false
        )
        if (url != BrowserTab.HOME_URL) {
            _webCommands.tryEmit(WebCommand.LoadUrl(url))
        }
    }

    fun selectTab(tabId: String) {
        if (_uiState.value.activeTabId != tabId) {
            _uiState.value = _uiState.value.copy(
                activeTabId = tabId,
                isTabSwitcherOpen = false,
                isAddressEditing = false,
                isToolsMenuOpen = false
            )
        } else {
            _uiState.value = _uiState.value.copy(isTabSwitcherOpen = false)
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            val freshTab = BrowserTab()
            _uiState.value = _uiState.value.copy(
                tabs = listOf(freshTab),
                activeTabId = freshTab.id,
                isTabSwitcherOpen = false
            )
            return
        }

        val closingIndex = currentTabs.indexOfFirst { it.id == tabId }
        val updatedTabs = currentTabs.filter { it.id != tabId }
        val newActiveId = if (_uiState.value.activeTabId == tabId) {
            val nextIndex = (closingIndex.coerceAtMost(updatedTabs.size - 1)).coerceAtLeast(0)
            updatedTabs[nextIndex].id
        } else {
            _uiState.value.activeTabId
        }

        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newActiveId
        )
    }

    fun closeAllTabs() {
        val freshTab = BrowserTab()
        _uiState.value = _uiState.value.copy(
            tabs = listOf(freshTab),
            activeTabId = freshTab.id,
            isTabSwitcherOpen = false
        )
    }

    // Navigation & Web Actions
    fun submitSearchOrUrl(rawInput: String) {
        val query = rawInput.trim()
        if (query.isBlank()) return

        val targetUrl = formatUrlOrQuery(query)
        updateActiveTabUrl(targetUrl)
        _uiState.value = _uiState.value.copy(isAddressEditing = false, isToolsMenuOpen = false)
        _webCommands.tryEmit(WebCommand.LoadUrl(targetUrl))
    }

    fun goHome() {
        updateActiveTabUrl(BrowserTab.HOME_URL)
        _uiState.value = _uiState.value.copy(isAddressEditing = false, isToolsMenuOpen = false)
    }

    fun goBack() {
        val active = _uiState.value.activeTab
        if (active.canGoBack) {
            _webCommands.tryEmit(WebCommand.GoBack)
        } else if (!active.isHome) {
            goHome()
        }
    }

    fun goForward() {
        _webCommands.tryEmit(WebCommand.GoForward)
    }

    fun reload() {
        _webCommands.tryEmit(WebCommand.Reload)
    }

    fun stopLoading() {
        _webCommands.tryEmit(WebCommand.StopLoading)
    }

    fun toggleDesktopMode() {
        val active = _uiState.value.activeTab
        val updatedDesktop = !active.isDesktopMode
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) tab.copy(isDesktopMode = updatedDesktop) else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs, isToolsMenuOpen = false)
        showToast(if (updatedDesktop) "Desktop view enabled" else "Mobile view enabled")
        if (!active.isHome) {
            reload()
        }
    }

    // Tab State Updates called from WebViewClient / WebChromeClient
    fun onPageStarted(url: String) {
        val active = _uiState.value.activeTab
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) tab.copy(url = url, isLoading = true, progress = 10) else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
    }

    fun onPageFinished(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        val active = _uiState.value.activeTab
        val finalTitle = if (!title.isNullOrBlank()) title else active.title
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) {
                tab.copy(
                    url = url,
                    title = finalTitle,
                    isLoading = false,
                    progress = 100,
                    canGoBack = canBack,
                    canGoForward = canForward
                )
            } else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)

        if (url.isNotBlank() && url != BrowserTab.HOME_URL && !url.startsWith("about:") && !active.isIncognito) {
            viewModelScope.launch {
                repository.recordHistory(url, finalTitle)
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        val active = _uiState.value.activeTab
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) {
                tab.copy(
                    progress = progress,
                    isLoading = progress in 1..99
                )
            } else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
    }

    fun onReceivedTitle(title: String?) {
        if (title.isNullOrBlank()) return
        val active = _uiState.value.activeTab
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) tab.copy(title = title) else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
    }

    private fun updateActiveTabUrl(url: String) {
        val active = _uiState.value.activeTab
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) {
                tab.copy(
                    url = url,
                    title = if (url == BrowserTab.HOME_URL) "Speed Dial" else url
                )
            } else tab
        }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
    }

    // Bookmarks
    fun toggleCurrentBookmark() {
        val active = _uiState.value.activeTab
        if (active.isHome) return
        viewModelScope.launch {
            val added = repository.toggleBookmark(active.url, active.title)
            showToast(if (added) "Bookmark added" else "Bookmark removed")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
            showToast("Bookmark deleted")
        }
    }

    // History
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            showToast("History cleared")
        }
    }

    // Speed Dials
    fun addSpeedDial(title: String, url: String, color: Long) {
        viewModelScope.launch {
            repository.addSpeedDial(title, url, color)
            showToast("Shortcut added to home")
        }
    }

    fun addCurrentPageToSpeedDial() {
        val active = _uiState.value.activeTab
        if (active.isHome) return
        viewModelScope.launch {
            repository.addSpeedDial(active.title.take(20), active.url, 0xFF4285F4)
            showToast("Added \"${active.displayHost}\" to Speed Dials")
            _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        }
    }

    fun deleteSpeedDial(id: Long) {
        viewModelScope.launch {
            repository.deleteSpeedDial(id)
        }
    }

    // Toolbar Customization
    fun updateToolbarActions(actions: List<ToolbarActionItem>) {
        repository.saveToolbarActions(actions)
    }

    fun resetToolbarActions() {
        repository.resetToolbarActions()
        showToast("Toolbar reset to defaults")
    }

    fun updateSpeedDial(id: Long, title: String, url: String) {
        viewModelScope.launch {
            repository.updateSpeedDial(id, title, url)
            showToast("Speed dial updated")
        }
    }

    fun updateBookmark(id: Long, title: String, url: String) {
        viewModelScope.launch {
            repository.updateBookmark(id, title, url)
            showToast("Bookmark updated")
        }
    }

    fun setSettingsVisible(visible: Boolean, section: String? = null) {
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = visible,
            activeSettingsSection = section,
            isToolsMenuOpen = false
        )
    }

    fun setAccentColor(color: Long) {
        repository.setAccentColor(color)
        _uiState.value = _uiState.value.copy(accentColor = color)
        showToast("Color theme updated")
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
        showToast("Theme updated")
    }

    fun setAmoledDark(enabled: Boolean) {
        repository.setAmoledDark(enabled)
        _uiState.value = _uiState.value.copy(isAmoledDark = enabled)
        showToast(if (enabled) "AMOLED Dark enabled" else "AMOLED Dark disabled")
    }

    fun setWallpaper(wallpaper: String) {
        repository.setWallpaper(wallpaper)
        _uiState.value = _uiState.value.copy(wallpaper = wallpaper)
        showToast("Home wallpaper updated")
    }

    fun setShowSpeedDials(enabled: Boolean) {
        repository.setShowSpeedDials(enabled)
        _uiState.value = _uiState.value.copy(showSpeedDials = enabled)
    }

    fun setShowBookmarks(enabled: Boolean) {
        repository.setShowBookmarks(enabled)
        _uiState.value = _uiState.value.copy(showBookmarks = enabled)
    }

    fun setShowRecent(enabled: Boolean) {
        repository.setShowRecent(enabled)
        _uiState.value = _uiState.value.copy(showRecent = enabled)
    }

    // Tools Menu Actions
    fun setToolsMenuVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = visible)
    }

    fun toggleFullScreen() {
        val newFs = !_uiState.value.isFullScreen
        _uiState.value = _uiState.value.copy(
            isFullScreen = newFs,
            isToolsMenuOpen = false
        )
        showToast(if (newFs) "Full screen enabled. Tap back to exit." else "Full screen disabled")
    }

    fun openFindInPage() {
        _uiState.value = _uiState.value.copy(
            isFindInPageOpen = true,
            isToolsMenuOpen = false
        )
    }

    fun closeFindInPage() {
        _uiState.value = _uiState.value.copy(
            isFindInPageOpen = false,
            findInPageQuery = ""
        )
        _webCommands.tryEmit(WebCommand.ClearMatches)
    }

    fun findInPage(query: String, forward: Boolean = true) {
        _uiState.value = _uiState.value.copy(findInPageQuery = query)
        _webCommands.tryEmit(WebCommand.FindInPage(query, forward))
    }

    fun toggleAdBlocker() {
        setAdBlockerEnabled(!_uiState.value.isAdBlockerEnabled)
    }

    fun setAdBlockerEnabled(enabled: Boolean) {
        repository.setAdBlockerEnabled(enabled)
        _uiState.value = _uiState.value.copy(isAdBlockerEnabled = enabled)
        showToast(if (enabled) "uBlock Ad Blocker enabled" else "uBlock Ad Blocker paused")
    }

    fun toggleBlockImages() {
        val block = !_uiState.value.isBlockImagesEnabled
        _uiState.value = _uiState.value.copy(
            isBlockImagesEnabled = block,
            isToolsMenuOpen = false
        )
        _webCommands.tryEmit(WebCommand.SetBlockImages(block))
        showToast(if (block) "Images blocked (Data Saver)" else "Images allowed")
    }

    fun toggleNightMode() {
        val night = !_uiState.value.isNightModeEnabled
        _uiState.value = _uiState.value.copy(
            isNightModeEnabled = night,
            isToolsMenuOpen = false
        )
        _webCommands.tryEmit(WebCommand.SetNightMode(night))
        showToast(if (night) "Night mode on" else "Night mode off")
    }

    fun toggleSecureDns() {
        val next = !_uiState.value.isSecureDnsEnabled
        setSecureDnsEnabled(next)
    }

    fun setSecureDnsEnabled(enabled: Boolean) {
        repository.setSecureDnsEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSecureDnsEnabled = enabled)
        val providerName = _uiState.value.selectedDns.substringBefore(" (")
        showToast(if (enabled) "Secure DNS on ($providerName)" else "Secure DNS off (System default)")
    }

    fun setDnsDialogOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDnsDialogOpen = open, isToolsMenuOpen = false)
    }

    fun selectDns(dns: String) {
        repository.setSelectedDns(dns)
        _uiState.value = _uiState.value.copy(selectedDns = dns)
        showToast("DNS set to $dns")
    }

    fun setCustomDns(customDns: String) {
        repository.setCustomDns(customDns)
        _uiState.value = _uiState.value.copy(customDns = customDns)
    }

    // Ad Blocker Options
    fun setAdBlockerMode(mode: String) {
        repository.setAdBlockerMode(mode)
        _uiState.value = _uiState.value.copy(adBlockerMode = mode)
        val modeTitle = when (mode) {
            "balance" -> "Balance"
            "extreme" -> "Extreme"
            else -> "Normal"
        }
        showToast("Ad Blocker: $modeTitle")
    }

    fun setAdBlockerDialogOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isAdBlockerDialogOpen = open, isToolsMenuOpen = false)
    }

    // Animations Scale (1x / 0.5x)
    fun setAnimationScale(scale: Float) {
        val clamped = if (scale <= 0.75f) 0.5f else 1.0f
        repository.setAnimationScale(clamped)
        _uiState.value = _uiState.value.copy(animationScale = clamped)
        showToast(if (clamped == 1.0f) "Animations set to 1.0x" else "Animations set to 0.5x")
    }

    fun toggleAnimationScale() {
        val next = if (_uiState.value.animationScale == 1.0f) 0.5f else 1.0f
        setAnimationScale(next)
    }

    fun openSettingsSection(section: String) {
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = true,
            activeSettingsSection = section,
            isToolsMenuOpen = false,
            isTabSwitcherOpen = false
        )
    }

    fun translatePage() {
        val active = _uiState.value.activeTab
        if (active.isHome) {
            showToast("Open a web page to translate")
            return
        }
        val target = "https://translate.google.com/translate?sl=auto&tl=en&u=" + URLEncoder.encode(active.url, "UTF-8")
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        submitSearchOrUrl(target)
        showToast("Translating page with Google Translate...")
    }

    fun savePage() {
        val active = _uiState.value.activeTab
        if (active.isHome) {
            showToast("Open a web page to save")
            return
        }
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        _webCommands.tryEmit(WebCommand.SaveWebArchive)
        showToast("Saving offline web page archive...")
    }

    fun printPage() {
        val active = _uiState.value.activeTab
        if (active.isHome) {
            showToast("Open a web page to print")
            return
        }
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        _webCommands.tryEmit(WebCommand.PrintPage)
    }

    fun takeScreenshot() {
        val active = _uiState.value.activeTab
        if (active.isHome) {
            showToast("Open a web page to capture screenshot")
            return
        }
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        _webCommands.tryEmit(WebCommand.TakeScreenshot)
    }

    fun downloadVideo() {
        val active = _uiState.value.activeTab
        if (active.isHome) {
            showToast("Open a web page with video to detect media")
            return
        }
        _uiState.value = _uiState.value.copy(isToolsMenuOpen = false)
        showToast("Scanning page for downloadable media streams...")
    }

    fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    // Dialog / Modal Visibility Controls
    fun setTabSwitcherVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isTabSwitcherOpen = visible, isToolsMenuOpen = false)
    }

    fun setHistoryVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isHistoryOpen = visible, isToolsMenuOpen = false)
    }

    fun setBookmarksVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isBookmarksOpen = visible, isToolsMenuOpen = false)
    }

    fun setCustomizeToolbarVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isCustomizeToolbarOpen = visible, isToolsMenuOpen = false)
    }

    fun setAddSpeedDialVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isAddSpeedDialOpen = visible)
    }

    fun setAddressEditing(editing: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAddressEditing = editing,
            addressInputText = if (editing) {
                val active = _uiState.value.activeTab
                if (active.isHome) "" else active.url
            } else ""
        )
    }

    fun onAddressInputChange(text: String) {
        _uiState.value = _uiState.value.copy(addressInputText = text)
    }

    companion object {
        private val DOMAIN_PATTERN = Pattern.compile(
            "^(https?://)?(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|localhost|(\\d{1,3}\\.){3}\\d{1,3})(:\\d+)?(/.*)?$"
        )

        fun formatUrlOrQuery(input: String): String {
            val trimmed = input.trim()
            if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("about:", ignoreCase = true)
            ) {
                return trimmed
            }
            if (DOMAIN_PATTERN.matcher(trimmed).matches() && !trimmed.contains(" ")) {
                return "https://$trimmed"
            }
            return "https://www.google.com/search?q=" + URLEncoder.encode(trimmed, "UTF-8")
        }
    }
}
