package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ToolbarActionItem

val THEME_ACCENT_COLORS = listOf(
    0xFFE5254AL, // Browson Crimson
    0xFFEF4444L, // Vibrant Red
    0xFF3B82F6L, // Electric Blue
    0xFF06B6D4L, // Cyber Cyan
    0xFF10B981L, // Emerald Green
    0xFF8B5CF6L, // Royal Purple
    0xFFEC4899L, // Hot Pink
    0xFFF59E0BL, // Amber Gold
    0xFFF97316L, // Deep Orange
    0xFF64748BL  // Slate Obsidian
)

data class DefaultWallpaperItem(
    val id: String,
    val title: String,
    val drawableResId: Int,
    val isDark: Boolean
)

val DEFAULT_WALLPAPERS = listOf(
    DefaultWallpaperItem("wp_black_waves", "Black Waves", R.drawable.wp_black_waves, true),
    DefaultWallpaperItem("wp_purple_waves", "Purple Waves", R.drawable.wp_purple_waves, true),
    DefaultWallpaperItem("wp_pink_peach", "Pink Peach", R.drawable.wp_pink_peach, false),
    DefaultWallpaperItem("wp_cyan_blue", "Cyan Blue", R.drawable.wp_cyan_blue, false),
    DefaultWallpaperItem("wp_clean_dark", "Clean Dark", R.drawable.wp_clean_dark, true),
    DefaultWallpaperItem("wp_clean_light", "Clean Light", R.drawable.wp_clean_light, false)
)

data class SettingsMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val id: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentAccentColor: Long,
    currentThemeMode: String = "system",
    isAmoledDark: Boolean = false,
    currentWallpaper: String,
    toolbarActions: List<ToolbarActionItem>,
    isAdBlockerEnabled: Boolean = true,
    currentDns: String = "Cloudflare (1.1.1.1)",
    customDns: String = "",
    onSelectAccentColor: (Long) -> Unit,
    onSelectThemeMode: (String) -> Unit = {},
    onToggleAmoledDark: (Boolean) -> Unit = {},
    onSelectWallpaper: (String) -> Unit,
    onSaveToolbarActions: (List<ToolbarActionItem>) -> Unit,
    onResetToolbarActions: () -> Unit,
    onToggleAdBlocker: (Boolean) -> Unit = {},
    onSelectDns: (String) -> Unit = {},
    onSaveCustomDns: (String) -> Unit = {},
    onOpenDnsDialog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val accentColor = Color(currentAccentColor)

    BackHandler {
        when (activeSubScreen) {
            "customize_bottom_toolbar" -> activeSubScreen = "bottom_toolbar"
            "bottom_toolbar" -> activeSubScreen = "appearance"
            "theme" -> activeSubScreen = "appearance"
            "wallpaper" -> activeSubScreen = "appearance"
            null -> onBack()
            else -> activeSubScreen = null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopAppBar(
                title = {
                    if (isSearching && activeSubScreen == null) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search settings") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = when (activeSubScreen) {
                                "appearance" -> "Appearance"
                                "theme" -> "Theme"
                                "wallpaper" -> "Wallpaper"
                                "bottom_toolbar" -> "Bottom toolbar"
                                "customize_bottom_toolbar" -> "Customize bottom toolbar"
                                "start_page" -> "Start page"
                                "privacy" -> "Privacy & security"
                                else -> "Settings"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (activeSubScreen) {
                            "customize_bottom_toolbar" -> activeSubScreen = "bottom_toolbar"
                            "bottom_toolbar" -> activeSubScreen = "appearance"
                            "theme" -> activeSubScreen = "appearance"
                            "wallpaper" -> activeSubScreen = "appearance"
                            null -> onBack()
                            else -> activeSubScreen = null
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (activeSubScreen == null) {
                        IconButton(onClick = { isSearching = !isSearching }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            when (activeSubScreen) {
                "appearance" -> {
                    AppearanceSettingsView(
                        currentAccentColor = currentAccentColor,
                        currentThemeMode = currentThemeMode,
                        isAmoledDark = isAmoledDark,
                        currentWallpaper = currentWallpaper,
                        onNavigateToTheme = { activeSubScreen = "theme" },
                        onNavigateToWallpaper = { activeSubScreen = "wallpaper" },
                        onNavigateToBottomToolbar = { activeSubScreen = "bottom_toolbar" },
                        onNavigateToCustomizeToolbar = { activeSubScreen = "customize_bottom_toolbar" },
                        onSelectAccentColor = onSelectAccentColor,
                        accentColor = accentColor
                    )
                }

                "theme" -> {
                    ThemeSettingsView(
                        currentThemeMode = currentThemeMode,
                        isAmoledDark = isAmoledDark,
                        onSelectThemeMode = onSelectThemeMode,
                        onToggleAmoledDark = onToggleAmoledDark,
                        accentColor = accentColor
                    )
                }

                "wallpaper" -> {
                    WallpaperSettingsView(
                        currentWallpaper = currentWallpaper,
                        onSelectWallpaper = onSelectWallpaper,
                        accentColor = accentColor
                    )
                }

                "bottom_toolbar" -> {
                    BottomToolbarSettingsView(
                        toolbarActions = toolbarActions,
                        onNavigateToCustomize = { activeSubScreen = "customize_bottom_toolbar" },
                        accentColor = accentColor
                    )
                }

                "customize_bottom_toolbar" -> {
                    CustomizeBottomToolbarView(
                        toolbarActions = toolbarActions,
                        onSaveToolbarActions = onSaveToolbarActions,
                        onResetToolbarActions = onResetToolbarActions,
                        accentColor = accentColor
                    )
                }

                "start_page" -> {
                    StartPageSettingsView(
                        currentWallpaper = currentWallpaper,
                        onNavigateToWallpaper = { activeSubScreen = "wallpaper" },
                        accentColor = accentColor
                    )
                }

                "privacy" -> {
                    PrivacySettingsView(
                        isAdBlockerEnabled = isAdBlockerEnabled,
                        currentDns = currentDns,
                        customDns = customDns,
                        onToggleAdBlocker = onToggleAdBlocker,
                        onSelectDns = onSelectDns,
                        onSaveCustomDns = onSaveCustomDns,
                        accentColor = accentColor
                    )
                }

                else -> {
                    MainSettingsListView(
                        searchQuery = searchQuery,
                        accentColor = accentColor,
                        onNavigate = { activeSubScreen = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainSettingsListView(
    searchQuery: String,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        SettingsMenuItem("Appearance", "Theme, wallpaper, colors, bottom toolbar", Icons.Default.Palette, "appearance"),
        SettingsMenuItem("Start page", "Speed dials, wallpaper", Icons.Default.Home, "start_page"),
        SettingsMenuItem("Privacy & security", "uBlock ad blocker, secure DNS", Icons.Default.Security, "privacy"),
        SettingsMenuItem("Downloads", "Storage and concurrent downloads", Icons.Default.Download, "downloads"),
        SettingsMenuItem("Search", "Default search engine", Icons.Default.Search, "search")
    )

    val filtered = if (searchQuery.isBlank()) items else items.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(filtered) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(item.id) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }

        // About / Developer Info Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Browson",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Developer: son skr",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = accentColor
                        )
                        Text(
                            text = "Version 1.0 • Built with Jetpack Compose",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Appearance Settings View
 */
@Composable
private fun AppearanceSettingsView(
    currentAccentColor: Long,
    currentThemeMode: String,
    isAmoledDark: Boolean,
    currentWallpaper: String,
    onNavigateToTheme: () -> Unit,
    onNavigateToWallpaper: () -> Unit,
    onNavigateToBottomToolbar: () -> Unit,
    onNavigateToCustomizeToolbar: () -> Unit,
    onSelectAccentColor: (Long) -> Unit,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Theme Option Card
            Text(
                text = "Theme & Display",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTheme() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBrightness,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val themeDescription = when (currentThemeMode.lowercase()) {
                            "light" -> "Light"
                            "dark" -> if (isAmoledDark) "Dark (AMOLED Active)" else "Dark"
                            else -> if (isAmoledDark) "System default (AMOLED on dark)" else "System default"
                        }
                        Text(
                            text = themeDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Theme",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Wallpaper Option Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWallpaper() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wallpaper",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val wpLabel = when {
                            currentWallpaper.startsWith("custom:") -> "Custom Device Image"
                            currentWallpaper == "wp_black_waves" -> "Black Waves"
                            currentWallpaper == "wp_purple_waves" -> "Purple Waves"
                            currentWallpaper == "wp_pink_peach" -> "Pink Peach"
                            currentWallpaper == "wp_cyan_blue" -> "Cyan Blue"
                            currentWallpaper == "wp_clean_dark" -> "Clean Dark"
                            currentWallpaper == "wp_clean_light" -> "Clean Light"
                            else -> currentWallpaper
                        }
                        Text(
                            text = wpLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Wallpaper",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Customize Bottom Toolbar Option Card
            Text(
                text = "Toolbar & Navigation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCustomizeToolbar() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize bottom toolbar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reorder buttons and live preview status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Accent Color Options (10 colors)
            Text(
                text = "Accent Color (10 Options)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Changes icons, active tool circles, buttons and highlighted items.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(THEME_ACCENT_COLORS) { colorLong ->
                    val color = Color(colorLong)
                    val isSelected = (colorLong == currentAccentColor)

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelectAccentColor(colorLong) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Theme Settings View:
 * - System default (change by system light or dark)
 * - Dark
 * - Light
 * - AMOLED Dark option with on/off toggle
 */
@Composable
private fun ThemeSettingsView(
    currentThemeMode: String,
    isAmoledDark: Boolean,
    onSelectThemeMode: (String) -> Unit,
    onToggleAmoledDark: (Boolean) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Choose Theme",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select how Browson looks on your device:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option 1: System default
        ThemeOptionCard(
            title = "System default",
            subtitle = "Change automatically with system light or dark mode",
            icon = Icons.Default.SettingsBrightness,
            isSelected = currentThemeMode.equals("system", ignoreCase = true),
            accentColor = accentColor,
            onClick = { onSelectThemeMode("system") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 2: Light
        ThemeOptionCard(
            title = "Light",
            subtitle = "Classic bright interface",
            icon = Icons.Default.LightMode,
            isSelected = currentThemeMode.equals("light", ignoreCase = true),
            accentColor = accentColor,
            onClick = { onSelectThemeMode("light") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 3: Dark
        ThemeOptionCard(
            title = "Dark",
            subtitle = "Comfortable dark interface for low-light environments",
            icon = Icons.Default.DarkMode,
            isSelected = currentThemeMode.equals("dark", ignoreCase = true),
            accentColor = accentColor,
            onClick = { onSelectThemeMode("dark") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // AMOLED Dark option with on/off toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isAmoledDark) Color.Black else accentColor.copy(alpha = 0.15f))
                        .border(1.dp, if (isAmoledDark) accentColor else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = if (isAmoledDark) accentColor else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AMOLED Dark Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pure pitch-black #000000 background to save battery on OLED screens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isAmoledDark,
                    onCheckedChange = { onToggleAmoledDark(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(accentColor),
            width = 2.dp
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
        }
    }
}

/**
 * Wallpaper Settings View:
 * Inside Wallpaper option:
 * - Default tab: Grid view with Black Waves, Purple Waves, Pink Peach, Cyan Blue, Clean Dark, Clean Light.
 *   Touch on image sets as home wallpaper!
 * - Choose tab: Opens system file picker to select an image from files!
 */
@Composable
private fun WallpaperSettingsView(
    currentWallpaper: String,
    onSelectWallpaper: (String) -> Unit,
    accentColor: Color
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Default, 1 = Choose

    // System file picker for selecting device image
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onSelectWallpaper("custom:$it")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = accentColor
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Default", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Choose", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Default Grid View: User images + dark and light image
            Text(
                text = "Tap any wallpaper to set as home screen background:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(DEFAULT_WALLPAPERS, key = { it.id }) { item ->
                    val isSelected = (currentWallpaper == item.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectWallpaper(item.id) },
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(accentColor),
                            width = 3.dp
                        ) else null
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f)
                            ) {
                                Image(
                                    painter = painterResource(id = item.drawableResId),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(accentColor.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(accentColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (item.isDark) "Dark" else "Light",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Choose Option: Open system file picker to select image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Choose Image From Device",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select any picture or photo from your files or gallery to use as your browser home wallpaper.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                filePickerLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open File Picker")
                        }
                    }
                }

                if (currentWallpaper.startsWith("custom:")) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Current Custom Wallpaper Active",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val customUri = Uri.parse(currentWallpaper.removePrefix("custom:"))
                    Card(
                        modifier = Modifier
                            .size(160.dp, 240.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(accentColor),
                            width = 2.dp
                        )
                    ) {
                        AsyncImage(
                            model = customUri,
                            contentDescription = "Active custom wallpaper",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom Toolbar Menu
 */
@Composable
private fun BottomToolbarSettingsView(
    toolbarActions: List<ToolbarActionItem>,
    onNavigateToCustomize: () -> Unit,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Settings > Appearance > Bottom toolbar",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCustomize() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.4f)),
                    width = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize bottom toolbar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reorder buttons, enable or disable toolbar actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Customize Bottom Toolbar View
 * "move active toolbar option to customize bottom toolbar option put it at top when change toolbar option it show live toolbar icon status."
 */
@Composable
private fun CustomizeBottomToolbarView(
    toolbarActions: List<ToolbarActionItem>,
    onSaveToolbarActions: (List<ToolbarActionItem>) -> Unit,
    onResetToolbarActions: () -> Unit,
    accentColor: Color
) {
    var actionsList by remember(toolbarActions) { mutableStateOf(toolbarActions) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Settings > Appearance > Customize Toolbar",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Customize Bottom Toolbar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Use the arrows to reorder icons, and the switch to toggle visibility.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            // AT TOP: Live Active Toolbar Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.35f)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "LIVE TOOLBAR STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val enabledItems = actionsList.filter { it.isEnabled }
                    if (enabledItems.isEmpty()) {
                        Text(
                            text = "No tools active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            enabledItems.forEach { item ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.type.icon,
                                            contentDescription = item.type.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.type.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Toolbar Items Reordering List
        itemsIndexed(actionsList) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isEnabled) accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.type.icon,
                            contentDescription = null,
                            tint = if (item.isEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = item.type.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (index > 0) {
                                val mutable = actionsList.toMutableList()
                                val prev = mutable[index - 1]
                                mutable[index - 1] = item
                                mutable[index] = prev
                                actionsList = mutable
                                onSaveToolbarActions(mutable)
                            }
                        },
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            if (index < actionsList.size - 1) {
                                val mutable = actionsList.toMutableList()
                                val next = mutable[index + 1]
                                mutable[index + 1] = item
                                mutable[index] = next
                                actionsList = mutable
                                onSaveToolbarActions(mutable)
                            }
                        },
                        enabled = index < actionsList.size - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Switch(
                        checked = item.isEnabled,
                        onCheckedChange = { checked ->
                            val mutable = actionsList.toMutableList()
                            mutable[index] = item.copy(isEnabled = checked)
                            actionsList = mutable
                            onSaveToolbarActions(mutable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = {
                    onResetToolbarActions()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Toolbar to Defaults")
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Start Page Settings
 */
@Composable
private fun StartPageSettingsView(
    currentWallpaper: String,
    onNavigateToWallpaper: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, tint = accentColor)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Home Screen Wallpaper",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Customize your new tab / speed dial home background wallpaper:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToWallpaper() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, tint = accentColor)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Wallpaper",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Choose from Default gallery or files from your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accentColor)
            }
        }
    }
}

/**
 * Privacy & Security Settings
 */
@Composable
private fun PrivacySettingsView(
    isAdBlockerEnabled: Boolean,
    currentDns: String,
    customDns: String,
    onToggleAdBlocker: (Boolean) -> Unit,
    onSelectDns: (String) -> Unit,
    onSaveCustomDns: (String) -> Unit,
    accentColor: Color
) {
    val dnsProviders = listOf(
        "Google (8.8.8.8)" to "8.8.8.8 / 8.8.4.4",
        "Cloudflare (1.1.1.1)" to "1.1.1.1 / 1.0.0.1",
        "Quad9 (9.9.9.9)" to "9.9.9.9 / 149.112.112.112",
        "Custom DNS" to "Custom IP or DNS over HTTPS",
        "Default (System DNS)" to "Network provider default"
    )

    var isCustomMode by remember(currentDns) {
        mutableStateOf(currentDns.startsWith("Custom") || !dnsProviders.any { it.first == currentDns })
    }
    var customAddressInput by remember(customDns, currentDns) {
        mutableStateOf(
            if (currentDns.startsWith("Custom: ")) currentDns.removePrefix("Custom: ") else customDns
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isAdBlockerEnabled) accentColor.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    width = 1.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isAdBlockerEnabled) accentColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAdBlockerEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isAdBlockerEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ads Blocker by uBlock",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAdBlockerEnabled) "Active • Blocking ads & trackers" else "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAdBlockerEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isAdBlockerEnabled,
                            onCheckedChange = { onToggleAdBlocker(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Secure DNS Provider",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Current: $currentDns",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    dnsProviders.forEach { (name, desc) ->
                        val isSelected = if (name == "Custom DNS") isCustomMode else (currentDns == name && !isCustomMode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (name == "Custom DNS") {
                                        isCustomMode = true
                                    } else {
                                        isCustomMode = false
                                        onSelectDns(name)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (name == "Custom DNS") {
                                        isCustomMode = true
                                    } else {
                                        isCustomMode = false
                                        onSelectDns(name)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (isCustomMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customAddressInput,
                            onValueChange = {
                                customAddressInput = it
                                onSaveCustomDns(it)
                                if (it.isNotBlank()) onSelectDns("Custom: $it")
                            },
                            label = { Text("Enter Custom DNS (e.g., 94.140.14.14)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
