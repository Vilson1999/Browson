package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.data.model.ToolbarActionItem
import com.example.data.model.ToolbarActionType

@Composable
fun BottomToolbar(
    actions: List<ToolbarActionItem>,
    activeTab: BrowserTab,
    tabCount: Int,
    isToolsMenuOpen: Boolean = false,
    isTabSwitcherOpen: Boolean = false,
    isBookmarksOpen: Boolean = false,
    isHistoryOpen: Boolean = false,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenToolsMenu: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewTab: () -> Unit,
    onReload: () -> Unit,
    onShare: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabledActions = actions.filter { it.isEnabled }
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_toolbar"),
        color = if (activeTab.isHome) Color.Transparent else MaterialTheme.colorScheme.surface,
        tonalElevation = if (activeTab.isHome) 0.dp else 4.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            enabledActions.forEach { action ->
                when (action.type) {
                    ToolbarActionType.BACK -> {
                        val canBack = activeTab.canGoBack || !activeTab.isHome
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Back",
                            isActive = false,
                            enabled = canBack,
                            onClick = onBack,
                            testTag = "toolbar_action_back"
                        )
                    }

                    ToolbarActionType.FORWARD -> {
                        val canForward = activeTab.canGoForward
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Forward",
                            isActive = false,
                            enabled = canForward,
                            onClick = onForward,
                            testTag = "toolbar_action_forward"
                        )
                    }

                    ToolbarActionType.HOME -> {
                        val isActive = activeTab.isHome
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Home",
                            isActive = isActive,
                            onClick = onHome,
                            testTag = "toolbar_action_home"
                        )
                    }

                    ToolbarActionType.TABS -> {
                        val isActive = isTabSwitcherOpen
                        val bg = Color.Transparent
                        val contentColor = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(bg)
                                .clickable { onOpenTabs() }
                                .testTag("toolbar_action_tabs"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(
                                        width = 1.6.dp,
                                        color = contentColor,
                                        shape = RoundedCornerShape(5.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tabCount > 99) "99+" else "$tabCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = contentColor
                                )
                            }
                        }
                    }

                    ToolbarActionType.ME -> {
                        val isActive = isToolsMenuOpen
                        ToolbarToolCircle(
                            icon = Icons.Default.Person,
                            contentDescription = "Me Tools Menu",
                            isActive = isActive,
                            onClick = onOpenToolsMenu,
                            testTag = "toolbar_action_me"
                        )
                    }

                    ToolbarActionType.BOOKMARKS -> {
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Bookmarks",
                            isActive = isBookmarksOpen,
                            onClick = onOpenBookmarks,
                            testTag = "toolbar_action_bookmarks"
                        )
                    }

                    ToolbarActionType.HISTORY -> {
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "History",
                            isActive = isHistoryOpen,
                            onClick = onOpenHistory,
                            testTag = "toolbar_action_history"
                        )
                    }

                    ToolbarActionType.NEW_TAB -> {
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "New Tab",
                            isActive = false,
                            onClick = onNewTab,
                            testTag = "toolbar_action_new_tab"
                        )
                    }

                    ToolbarActionType.RELOAD -> {
                        val isLoading = activeTab.isLoading
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Reload",
                            isActive = isLoading,
                            onClick = onReload,
                            testTag = "toolbar_action_reload"
                        )
                    }

                    ToolbarActionType.SHARE -> {
                        val canShare = !activeTab.isHome && activeTab.url.isNotBlank()
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Share",
                            isActive = false,
                            enabled = canShare,
                            onClick = onShare,
                            testTag = "toolbar_action_share"
                        )
                    }

                    ToolbarActionType.DESKTOP_MODE -> {
                        val isActive = activeTab.isDesktopMode
                        ToolbarToolCircle(
                            icon = action.type.icon,
                            contentDescription = "Desktop Site",
                            isActive = isActive,
                            onClick = onToggleDesktopMode,
                            testTag = "toolbar_action_desktop"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circle background for every tool in the toolbar.
 * Colored with the app's current primary color only when active.
 */
@Composable
private fun ToolbarToolCircle(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    isHome: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bg = Color.Transparent
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isActive -> primaryColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
