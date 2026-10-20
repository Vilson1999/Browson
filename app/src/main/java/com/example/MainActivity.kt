package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.view.WindowCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BrowserTab
import com.example.ui.components.AdBlockerDialog
import com.example.ui.components.BookmarksDialog
import com.example.ui.components.BottomToolbar
import com.example.ui.components.BrowserWebView
import com.example.ui.components.CustomizeToolbarDialog
import com.example.ui.components.DnsDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.HistoryDialog
import com.example.ui.components.SettingsScreen
import com.example.ui.components.SpeedDialsView
import com.example.ui.components.TabSwitcherView
import com.example.ui.components.TabletTabStrip
import com.example.ui.components.ToolsBottomSheet
import com.example.ui.components.TopAddressBar
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by browserViewModel.uiState.collectAsState()
            val currentAccent = Color(uiState.accentColor)
            MyApplicationTheme(
                themeMode = uiState.themeMode,
                isAmoledDark = uiState.isAmoledDark,
                accentColor = currentAccent
            ) {
                BrowserApp(viewModel = browserViewModel)
            }
        }
    }
}

@Composable
fun BrowserApp(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val toolbarActions by viewModel.toolbarActions.collectAsState()
    val speedDials by viewModel.speedDials.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val activeTab = uiState.activeTab
    val isCurrentBookmarked = bookmarks.any { it.url == activeTab.url }
    val currentAccent = Color(uiState.accentColor)

    val systemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    val window = (context as? ComponentActivity)?.window
    LaunchedEffect(isDark) {
        window?.let { w ->
            val insetsController = WindowCompat.getInsetsController(w, w.decorView)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    val statusScrimColor = if (isDark) Color(0x80000000) else Color(0x80FFFFFF)
    val navScrimColor = if (isDark) Color(0x80000000) else Color(0x80FFFFFF)

    // Toast Feedback
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Intercept Back button intuitively
    val hasBackAction = uiState.isAddressEditing ||
            uiState.isSettingsOpen ||
            uiState.isToolsMenuOpen ||
            uiState.isFindInPageOpen ||
            uiState.isFullScreen ||
            uiState.isDnsDialogOpen ||
            uiState.isTabSwitcherOpen ||
            uiState.isHistoryOpen ||
            uiState.isBookmarksOpen ||
            uiState.isCustomizeToolbarOpen ||
            activeTab.canGoBack ||
            !activeTab.isHome

    BackHandler(enabled = hasBackAction) {
        when {
            uiState.isAddressEditing -> viewModel.setAddressEditing(false)
            uiState.isSettingsOpen -> viewModel.setSettingsVisible(false)
            uiState.isToolsMenuOpen -> viewModel.setToolsMenuVisible(false)
            uiState.isFindInPageOpen -> viewModel.closeFindInPage()
            uiState.isFullScreen -> viewModel.toggleFullScreen()
            uiState.isDnsDialogOpen -> viewModel.setDnsDialogOpen(false)
            uiState.isTabSwitcherOpen -> viewModel.setTabSwitcherVisible(false)
            uiState.isHistoryOpen -> viewModel.setHistoryVisible(false)
            uiState.isBookmarksOpen -> viewModel.setBookmarksVisible(false)
            uiState.isCustomizeToolbarOpen -> viewModel.setCustomizeToolbarVisible(false)
            activeTab.canGoBack -> viewModel.goBack()
            !activeTab.isHome -> viewModel.goHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("browser_main_scaffold"),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (!uiState.isFullScreen && !uiState.isSettingsOpen) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Tablet Tab Strip on medium / expanded screens
                        if (isTablet) {
                            TabletTabStrip(
                                tabs = uiState.tabs,
                                activeTabId = uiState.activeTabId,
                                onSelectTab = { viewModel.selectTab(it) },
                                onCloseTab = { viewModel.closeTab(it) },
                                onNewTab = { viewModel.openNewTab() }
                            )
                        }

                        // Top Smart Address Bar
                        TopAddressBar(
                            activeTab = activeTab,
                            isEditing = uiState.isAddressEditing,
                            inputText = uiState.addressInputText,
                            isBookmarked = isCurrentBookmarked,
                            tabCount = uiState.tabs.size,
                            accentColor = currentAccent,
                            onStartEditing = { viewModel.setAddressEditing(true) },
                            onInputChange = { viewModel.onAddressInputChange(it) },
                            onSubmit = { viewModel.submitSearchOrUrl(it) },
                            onCancelEditing = { viewModel.setAddressEditing(false) },
                            onToggleBookmark = { viewModel.toggleCurrentBookmark() },
                            onReloadOrStop = {
                                if (activeTab.isLoading) viewModel.stopLoading() else viewModel.reload()
                            },
                            onOpenTabs = { viewModel.setTabSwitcherVisible(true) },
                            onOpenMenu = { viewModel.setToolsMenuVisible(true) }
                        )

                    // Find In Page Bar
                    AnimatedVisibility(
                        visible = uiState.isFindInPageOpen,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        FindInPageBar(
                            query = uiState.findInPageQuery,
                            onQueryChange = { q -> viewModel.findInPage(q, forward = true) },
                            onNext = { viewModel.findInPage(uiState.findInPageQuery, forward = true) },
                            onPrevious = { viewModel.findInPage(uiState.findInPageQuery, forward = false) },
                            onClose = { viewModel.closeFindInPage() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!uiState.isFullScreen && !uiState.isSettingsOpen) {
                BottomToolbar(
                    actions = toolbarActions,
                    activeTab = activeTab,
                    tabCount = uiState.tabs.size,
                    isToolsMenuOpen = uiState.isToolsMenuOpen,
                    isTabSwitcherOpen = uiState.isTabSwitcherOpen,
                    isBookmarksOpen = uiState.isBookmarksOpen,
                    isHistoryOpen = uiState.isHistoryOpen,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onHome = { viewModel.goHome() },
                    onOpenTabs = { viewModel.setTabSwitcherVisible(true) },
                    onOpenToolsMenu = { viewModel.setToolsMenuVisible(true) },
                    onOpenBookmarks = { viewModel.setBookmarksVisible(true) },
                    onOpenHistory = { viewModel.setHistoryVisible(true) },
                    onNewTab = { viewModel.openNewTab() },
                    onReload = { viewModel.reload() },
                    onShare = {
                        if (!activeTab.isHome && activeTab.url.isNotBlank()) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share URL"))
                        }
                    },
                    onToggleDesktopMode = { viewModel.toggleDesktopMode() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Viewport: Home Speed Dials or Active Web Page
            if (activeTab.isHome) {
                SpeedDialsView(
                    speedDials = speedDials,
                    bookmarks = bookmarks,
                    recentHistory = history,
                    showSpeedDials = uiState.showSpeedDials,
                    showBookmarks = uiState.showBookmarks,
                    showRecent = uiState.showRecent,
                    wallpaper = uiState.wallpaper,
                    themeMode = uiState.themeMode,
                    accentColor = currentAccent,
                    onOpenUrl = { url ->
                        viewModel.submitSearchOrUrl(url)
                    },
                    onAddSpeedDial = { title, url, color ->
                        viewModel.addSpeedDial(title, url, color)
                    },
                    onEditSpeedDial = { id, title, url ->
                        viewModel.updateSpeedDial(id, title, url)
                    },
                    onDeleteSpeedDial = { id ->
                        viewModel.deleteSpeedDial(id)
                    },
                    onEditBookmark = { id, title, url ->
                        viewModel.updateBookmark(id, title, url)
                    },
                    onDeleteBookmark = { id ->
                        viewModel.deleteBookmark(id)
                    }
                )
            } else {
                BrowserWebView(
                    activeTab = activeTab,
                    viewModel = viewModel
                )
            }

            // Exit Fullscreen Floating Button
            if (uiState.isFullScreen) {
                FloatingActionButton(
                    onClick = { viewModel.toggleFullScreen() },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Tools Bottom Sheet (from Me icon)
            if (uiState.isToolsMenuOpen) {
                ToolsBottomSheet(
                    activeTab = activeTab,
                    isFullScreen = uiState.isFullScreen,
                    isAdBlockerEnabled = uiState.isAdBlockerEnabled,
                    isBlockImagesEnabled = uiState.isBlockImagesEnabled,
                    isNightModeEnabled = uiState.isNightModeEnabled,
                    isBookmarked = isCurrentBookmarked,
                    onFullScreen = { viewModel.toggleFullScreen() },
                    onTranslate = { viewModel.translatePage() },
                    onFindInPage = { viewModel.openFindInPage() },
                    onSavePage = { viewModel.savePage() },
                    onPrintPdf = { viewModel.printPage() },
                    onAdBlocker = { viewModel.toggleAdBlocker() },
                    onDesktop = { viewModel.toggleDesktopMode() },
                    onScreenshot = { viewModel.takeScreenshot() },
                    onBlockImages = { viewModel.toggleBlockImages() },
                    onBookmark = { viewModel.toggleCurrentBookmark() },
                    onDownloadVideo = { viewModel.downloadVideo() },
                    onToggleDns = { viewModel.toggleSecureDns() },
                    onDnsSettings = { viewModel.setDnsDialogOpen(true) },
                    onAdBlockerSettings = { viewModel.setAdBlockerDialogOpen(true) },
                    onOpenWith = {
                        if (!activeTab.isHome && activeTab.url.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeTab.url))
                                context.startActivity(Intent.createChooser(intent, "Open with"))
                            } catch (_: Exception) {
                                viewModel.showToast("No external app available")
                            }
                        } else {
                            viewModel.showToast("Open a webpage first")
                        }
                    },
                    onShareUrl = {
                        if (!activeTab.isHome && activeTab.url.isNotBlank()) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share URL"))
                        } else {
                            viewModel.showToast("Open a webpage first")
                        }
                    },
                    onCopyUrl = {
                        if (!activeTab.isHome && activeTab.url.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", activeTab.url))
                            viewModel.showToast("URL copied to clipboard")
                        } else {
                            viewModel.showToast("Open a webpage first")
                        }
                    },
                    onReload = { viewModel.reload() },
                    onAddShortcut = { viewModel.addCurrentPageToSpeedDial() },
                    onHistory = { viewModel.setHistoryVisible(true) },
                    onNightMode = { viewModel.toggleNightMode() },
                    onExit = { (context as? ComponentActivity)?.finish() },
                    onTabs = { viewModel.setTabSwitcherVisible(true) },
                    onSettings = { viewModel.setSettingsVisible(true) },
                    onOpenSetting = { section -> viewModel.openSettingsSection(section) },
                    onDismiss = { viewModel.setToolsMenuVisible(false) }
                )
            }

            // Tab Switcher Overlay
            if (uiState.isTabSwitcherOpen) {
                TabSwitcherView(
                    tabs = uiState.tabs,
                    activeTabId = uiState.activeTabId,
                    onSelectTab = { viewModel.selectTab(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onNewTab = { viewModel.openNewTab() },
                    onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                    onOpenHistory = {
                        viewModel.setTabSwitcherVisible(false)
                        viewModel.setHistoryVisible(true)
                    },
                    onCloseAllTabs = { viewModel.closeAllTabs() },
                    onDismiss = { viewModel.setTabSwitcherVisible(false) }
                )
            }

            // Bookmarks Modal Sheet
            if (uiState.isBookmarksOpen) {
                BookmarksDialog(
                    bookmarks = bookmarks,
                    onOpenBookmark = { url ->
                        viewModel.submitSearchOrUrl(url)
                    },
                    onDeleteBookmark = { id ->
                        viewModel.deleteBookmark(id)
                    },
                    onEditBookmark = { id, title, url ->
                        viewModel.updateBookmark(id, title, url)
                    },
                    onDismiss = { viewModel.setBookmarksVisible(false) }
                )
            }

            // History Modal Sheet
            if (uiState.isHistoryOpen) {
                HistoryDialog(
                    history = history,
                    onOpenUrl = { url ->
                        viewModel.submitSearchOrUrl(url)
                    },
                    onDeleteHistoryItem = { id ->
                        viewModel.deleteHistoryItem(id)
                    },
                    onClearAllHistory = {
                        viewModel.clearAllHistory()
                    },
                    onDismiss = { viewModel.setHistoryVisible(false) }
                )
            }

            // Customize Toolbar Dialog
            if (uiState.isCustomizeToolbarOpen) {
                CustomizeToolbarDialog(
                    actions = toolbarActions,
                    onSaveActions = { updated ->
                        viewModel.updateToolbarActions(updated)
                    },
                    onResetDefaults = {
                        viewModel.resetToolbarActions()
                    },
                    onDismiss = { viewModel.setCustomizeToolbarVisible(false) }
                )
            }

            // DNS Dialog
            if (uiState.isDnsDialogOpen) {
                DnsDialog(
                    isSecureDnsEnabled = uiState.isSecureDnsEnabled,
                    currentDns = uiState.selectedDns,
                    customDns = uiState.customDns,
                    onToggleSecureDns = { enabled -> viewModel.setSecureDnsEnabled(enabled) },
                    onSelectDns = { dns ->
                        viewModel.selectDns(dns)
                    },
                    onSaveCustomDns = { custom ->
                        viewModel.setCustomDns(custom)
                    },
                    onDismiss = { viewModel.setDnsDialogOpen(false) }
                )
            }

            // Ad Blocker Dialog
            if (uiState.isAdBlockerDialogOpen) {
                AdBlockerDialog(
                    isEnabled = uiState.isAdBlockerEnabled,
                    currentMode = uiState.adBlockerMode,
                    onToggleEnabled = { enabled -> viewModel.setAdBlockerEnabled(enabled) },
                    onSelectMode = { mode -> viewModel.setAdBlockerMode(mode) },
                    onDismiss = { viewModel.setAdBlockerDialogOpen(false) }
                )
            }

            // Settings Full Screen
            if (uiState.isSettingsOpen) {
                SettingsScreen(
                    currentAccentColor = uiState.accentColor,
                    currentThemeMode = uiState.themeMode,
                    isAmoledDark = uiState.isAmoledDark,
                    currentWallpaper = uiState.wallpaper,
                    toolbarActions = toolbarActions,
                    isAdBlockerEnabled = uiState.isAdBlockerEnabled,
                    adBlockerMode = uiState.adBlockerMode,
                    isSecureDnsEnabled = uiState.isSecureDnsEnabled,
                    currentDns = uiState.selectedDns,
                    customDns = uiState.customDns,
                    animationScale = uiState.animationScale,
                    showSpeedDials = uiState.showSpeedDials,
                    showBookmarks = uiState.showBookmarks,
                    showRecent = uiState.showRecent,
                    onSelectAccentColor = { color -> viewModel.setAccentColor(color) },
                    onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
                    onToggleAmoledDark = { amoled -> viewModel.setAmoledDark(amoled) },
                    onSelectWallpaper = { wp -> viewModel.setWallpaper(wp) },
                    onSaveToolbarActions = { updated -> viewModel.updateToolbarActions(updated) },
                    onResetToolbarActions = { viewModel.resetToolbarActions() },
                    onToggleAdBlocker = { enabled -> viewModel.setAdBlockerEnabled(enabled) },
                    onSelectAdBlockerMode = { mode -> viewModel.setAdBlockerMode(mode) },
                    onToggleSecureDns = { enabled -> viewModel.setSecureDnsEnabled(enabled) },
                    onSelectDns = { dns -> viewModel.selectDns(dns) },
                    onSaveCustomDns = { custom -> viewModel.setCustomDns(custom) },
                    onSetAnimationScale = { scale -> viewModel.setAnimationScale(scale) },
                    onToggleShowSpeedDials = { enabled -> viewModel.setShowSpeedDials(enabled) },
                    onToggleShowBookmarks = { enabled -> viewModel.setShowBookmarks(enabled) },
                    onToggleShowRecent = { enabled -> viewModel.setShowRecent(enabled) },
                    onOpenDnsDialog = { viewModel.setDnsDialogOpen(true) },
                    onBack = { viewModel.setSettingsVisible(false) }
                )
            }
        }
    }

    // 50% translucent scrim for notification bar (status bar)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(statusScrimColor)
            .align(Alignment.TopCenter)
    )

    // 50% translucent scrim for gesture key (navigation bar)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(navScrimColor)
            .align(Alignment.BottomCenter)
    )
}
}
