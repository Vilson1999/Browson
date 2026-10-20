package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsBottomSheet(
    activeTab: BrowserTab,
    isFullScreen: Boolean,
    isAdBlockerEnabled: Boolean,
    isSecureDnsEnabled: Boolean = true,
    isBlockImagesEnabled: Boolean,
    isNightModeEnabled: Boolean,
    isBookmarked: Boolean,
    onFullScreen: () -> Unit,
    onTranslate: () -> Unit,
    onFindInPage: () -> Unit,
    onSavePage: () -> Unit,
    onPrintPdf: () -> Unit,
    onAdBlocker: () -> Unit,
    onAdBlockerSettings: () -> Unit = {},
    onDesktop: () -> Unit,
    onScreenshot: () -> Unit,
    onBlockImages: () -> Unit,
    onBookmark: () -> Unit,
    onDownloadVideo: () -> Unit,
    onToggleDns: () -> Unit,
    onDnsSettings: () -> Unit = {},
    onOpenWith: () -> Unit,
    onShareUrl: () -> Unit,
    onCopyUrl: () -> Unit,
    onReload: () -> Unit,
    onAddShortcut: () -> Unit,
    onHistory: () -> Unit,
    onNightMode: () -> Unit,
    onExit: () -> Unit,
    onTabs: () -> Unit,
    onSettings: () -> Unit,
    onOpenSetting: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("tools_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Sheet Header: Mini top utility row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniToolIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        onClick = {
                            onReload()
                            onDismiss()
                        },
                        onLongClick = {
                            onOpenSetting("appearance")
                            onDismiss()
                        }
                    )

                    MiniToolIconButton(
                        icon = if (isAdBlockerEnabled) Icons.Default.VerifiedUser else Icons.Outlined.Security,
                        contentDescription = "Ad blocker",
                        isActive = isAdBlockerEnabled,
                        tint = if (isAdBlockerEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onAdBlocker,
                        onLongClick = {
                            onAdBlockerSettings()
                            onDismiss()
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniToolIconButton(
                        icon = Icons.Default.Tab,
                        contentDescription = "Tabs",
                        onClick = {
                            onTabs()
                            onDismiss()
                        },
                        onLongClick = {
                            onOpenSetting("appearance")
                            onDismiss()
                        }
                    )

                    MiniToolIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = {
                            onSettings()
                            onDismiss()
                        },
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        }
                    )
                }
            }

            HorizontalDivider(
                color = DividerDefaults.color.copy(alpha = 0.5f),
                thickness = 0.8.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 3 x 5 Tools Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolItem(
                        icon = Icons.Default.Fullscreen,
                        title = "Full screen",
                        isActive = isFullScreen,
                        onClick = onFullScreen,
                        onLongClick = {
                            onOpenSetting("appearance")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.Translate,
                        title = "Translate",
                        onClick = onTranslate,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.FindInPage,
                        title = "Find in page",
                        onClick = onFindInPage,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.SaveAlt,
                        title = "Save page",
                        onClick = onSavePage,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.Print,
                        title = "Print / PDF",
                        onClick = onPrintPdf,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolItem(
                        icon = if (isAdBlockerEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                        title = "Ad blocker",
                        isActive = isAdBlockerEnabled,
                        onClick = onAdBlocker,
                        onLongClick = {
                            onAdBlockerSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.Computer,
                        title = "Desktop",
                        isActive = activeTab.isDesktopMode,
                        onClick = onDesktop,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.CropFree,
                        title = "Screenshot",
                        onClick = onScreenshot,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.HideImage,
                        title = "Block images",
                        isActive = isBlockImagesEnabled,
                        onClick = onBlockImages,
                        onLongClick = {
                            onOpenSetting("privacy")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = if (isBookmarked) Icons.Default.Star else Icons.Default.StarOutline,
                        title = "Bookmark",
                        isActive = isBookmarked,
                        onClick = onBookmark,
                        onLongClick = {
                            onOpenSetting("start_page")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToolItem(
                        icon = Icons.Default.DownloadForOffline,
                        title = "Download\nVideo",
                        onClick = onDownloadVideo,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    // DNS Icon: Click = toggle on/off immediately (no popup!). Long press = open DNS setting!
                    ToolItem(
                        icon = Icons.Default.VpnKey,
                        title = if (isSecureDnsEnabled) "DNS: On" else "DNS: Off",
                        isActive = isSecureDnsEnabled,
                        onClick = onToggleDns,
                        onLongClick = {
                            onDnsSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        title = "Open with",
                        onClick = onOpenWith,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.Share,
                        title = "Share URL",
                        onClick = onShareUrl,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolItem(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy URL",
                        onClick = onCopyUrl,
                        onLongClick = {
                            onSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = DividerDefaults.color.copy(alpha = 0.5f),
                thickness = 0.8.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Bottom 5 Icons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniToolIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    size = 44.dp,
                    onClick = {
                        onReload()
                        onDismiss()
                    },
                    onLongClick = {
                        onOpenSetting("appearance")
                        onDismiss()
                    }
                )

                MiniToolIconButton(
                    icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
                    contentDescription = "Add to Home / Speed Dial",
                    size = 44.dp,
                    onClick = onAddShortcut,
                    onLongClick = {
                        onOpenSetting("start_page")
                        onDismiss()
                    }
                )

                MiniToolIconButton(
                    icon = Icons.Default.History,
                    contentDescription = "History",
                    size = 44.dp,
                    onClick = {
                        onHistory()
                        onDismiss()
                    },
                    onLongClick = {
                        onOpenSetting("privacy")
                        onDismiss()
                    }
                )

                MiniToolIconButton(
                    icon = Icons.Default.DarkMode,
                    contentDescription = "Night Mode",
                    size = 44.dp,
                    isActive = isNightModeEnabled,
                    tint = if (isNightModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    onClick = onNightMode,
                    onLongClick = {
                        onOpenSetting("appearance")
                        onDismiss()
                    }
                )

                MiniToolIconButton(
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = "Exit / Close",
                    size = 44.dp,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onExit,
                    onLongClick = {
                        onSettings()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiniToolIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: androidx.compose.ui.unit.Dp = 42.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bg = if (isActive) primaryColor.copy(alpha = 0.5f) else Color.Transparent
    val effectiveTint = if (isActive) primaryColor else tint

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = effectiveTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bg = if (isActive) primaryColor.copy(alpha = 0.5f) else Color.Transparent
    val contentColor = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 13.sp
            ),
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
