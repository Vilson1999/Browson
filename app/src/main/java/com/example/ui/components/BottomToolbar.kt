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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_toolbar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            enabledActions.forEach { action ->
                when (action.type) {
                    ToolbarActionType.BACK -> {
                        val canBack = activeTab.canGoBack || !activeTab.isHome
                        IconButton(
                            onClick = onBack,
                            enabled = canBack,
                            modifier = Modifier.testTag("toolbar_action_back")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Back",
                                tint = if (canBack) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }

                    ToolbarActionType.FORWARD -> {
                        val canForward = activeTab.canGoForward
                        IconButton(
                            onClick = onForward,
                            enabled = canForward,
                            modifier = Modifier.testTag("toolbar_action_forward")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Forward",
                                tint = if (canForward) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }

                    ToolbarActionType.HOME -> {
                        IconButton(
                            onClick = onHome,
                            modifier = Modifier.testTag("toolbar_action_home")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Home",
                                tint = if (activeTab.isHome) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ToolbarActionType.TABS -> {
                        // Tab Counter Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenTabs() }
                                .testTag("toolbar_action_tabs"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 1.8.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tabCount > 99) "99+" else "$tabCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    ToolbarActionType.ME -> {
                        // "Me" icon in circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onOpenToolsMenu() }
                                .testTag("toolbar_action_me"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Me Tools Menu",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    ToolbarActionType.BOOKMARKS -> {
                        IconButton(
                            onClick = onOpenBookmarks,
                            modifier = Modifier.testTag("toolbar_action_bookmarks")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Bookmarks",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ToolbarActionType.HISTORY -> {
                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.testTag("toolbar_action_history")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ToolbarActionType.NEW_TAB -> {
                        IconButton(
                            onClick = onNewTab,
                            modifier = Modifier.testTag("toolbar_action_new_tab")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "New Tab",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ToolbarActionType.RELOAD -> {
                        IconButton(
                            onClick = onReload,
                            modifier = Modifier.testTag("toolbar_action_reload")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Reload",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    ToolbarActionType.SHARE -> {
                        IconButton(
                            onClick = onShare,
                            enabled = !activeTab.isHome,
                            modifier = Modifier.testTag("toolbar_action_share")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Share",
                                tint = if (!activeTab.isHome) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }

                    ToolbarActionType.DESKTOP_MODE -> {
                        IconButton(
                            onClick = onToggleDesktopMode,
                            modifier = Modifier.testTag("toolbar_action_desktop")
                        ) {
                            Icon(
                                imageVector = action.type.icon,
                                contentDescription = "Desktop Site",
                                tint = if (activeTab.isDesktopMode) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
