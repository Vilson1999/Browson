package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tab
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolbarActionType(
    val title: String,
    val icon: ImageVector,
    val defaultEnabled: Boolean = true
) {
    BACK("Back", Icons.AutoMirrored.Filled.ArrowBack, defaultEnabled = true),
    FORWARD("Forward", Icons.AutoMirrored.Filled.ArrowForward, defaultEnabled = true),
    HOME("Home", Icons.Default.Home, defaultEnabled = true),
    TABS("Tabs", Icons.Default.Tab, defaultEnabled = true),
    ME("Me", Icons.Default.AccountCircle, defaultEnabled = true),
    BOOKMARKS("Bookmarks", Icons.Default.Bookmark, defaultEnabled = false),
    HISTORY("History", Icons.Default.History, defaultEnabled = false),
    NEW_TAB("New Tab", Icons.Default.Add, defaultEnabled = false),
    RELOAD("Reload", Icons.Default.Refresh, defaultEnabled = false),
    SHARE("Share", Icons.Default.Share, defaultEnabled = false),
    DESKTOP_MODE("Desktop", Icons.Default.Computer, defaultEnabled = false)
}

data class ToolbarActionItem(
    val type: ToolbarActionType,
    val isEnabled: Boolean = true
)
