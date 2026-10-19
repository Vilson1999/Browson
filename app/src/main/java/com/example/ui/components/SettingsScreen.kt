package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolbarActionItem

val THEME_ACCENT_COLORS = listOf(
    0xFFE5254AL, // Opera Crimson
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

val WALLPAPER_OPTIONS = listOf(
    "Minimal" to Color(0xFF0F141C),
    "Sunset Glow" to Color(0xFF38151D),
    "Cosmic Nebula" to Color(0xFF1E1035),
    "Ocean Wave" to Color(0xFF0C2436),
    "Emerald Mist" to Color(0xFF0B2B20),
    "Dark Obsidian" to Color(0xFF050508)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentAccentColor: Long,
    currentWallpaper: String,
    toolbarActions: List<ToolbarActionItem>,
    isAdBlockerEnabled: Boolean = true,
    currentDns: String = "Cloudflare (1.1.1.1)",
    customDns: String = "",
    onSelectAccentColor: (Long) -> Unit,
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
    // Navigation hierarchy:
    // null -> Main Settings
    // "appearance" -> Settings > Appearance
    // "bottom_toolbar" -> Settings > Appearance > Bottom toolbar
    // "customize_bottom_toolbar" -> Settings > Appearance > Bottom toolbar > Customize bottom toolbar
    // "start_page" -> Settings > Start page
    // "privacy" -> Settings > Privacy & security
    var activeSubScreen by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val accentColor = Color(currentAccentColor)

    // Back handler matching the required hierarchy:
    // customize_bottom_toolbar -> bottom_toolbar -> appearance -> main settings -> exit
    BackHandler {
        when (activeSubScreen) {
            "customize_bottom_toolbar" -> activeSubScreen = "bottom_toolbar"
            "bottom_toolbar" -> activeSubScreen = "appearance"
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
            // Settings Top Bar
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

            // Content routing based on activeSubScreen
            when (activeSubScreen) {
                "appearance" -> {
                    AppearanceSettingsView(
                        currentAccentColor = currentAccentColor,
                        onSelectAccentColor = onSelectAccentColor,
                        onNavigateToBottomToolbar = { activeSubScreen = "bottom_toolbar" },
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
                        onSelectWallpaper = onSelectWallpaper,
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
    // Notice: "About Opera" and "Help and feedback" are removed as requested
    val items = listOf(
        SettingsMenuItem("Appearance", "Themes, layout, bottom toolbar, colors", Icons.Default.AutoAwesome, "appearance"),
        SettingsMenuItem("Start page", "Speed dials, wallpaper", Icons.Default.Home, "start_page"),
        SettingsMenuItem("Personal data", "Sync & backup, autofill, passwords", Icons.Default.Person, "personal"),
        SettingsMenuItem("Privacy & security", "uBlock ad blocker, secure DNS, cookies", Icons.Default.Security, "privacy"),
        SettingsMenuItem("Search", "Search engine, recent searches", Icons.Default.Search, "search"),
        SettingsMenuItem("Notifications", "News and browser alerts", Icons.Default.Notifications, "notifications"),
        SettingsMenuItem("Media", "Background play, protected content", Icons.Default.SmartDisplay, "media"),
        SettingsMenuItem("Downloads", "Concurrent downloads, storage folder", Icons.Default.Download, "downloads")
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
    }
}

/**
 * Level 1: Appearance
 * Setting > Appearance
 */
@Composable
private fun AppearanceSettingsView(
    currentAccentColor: Long,
    onSelectAccentColor: (Long) -> Unit,
    onNavigateToBottomToolbar: () -> Unit,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // 10 Color Accent Options
            Text(
                text = "Accent Color (10 Options)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Changes icon outline, download bar, and settings switch toggles.",
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

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Toolbar Menu Item
            // Clicking here goes to Settings > Appearance > Bottom toolbar
            Text(
                text = "Toolbar & Navigation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBottomToolbar() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bottom toolbar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Customize layout, icons, order and appearance",
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Level 2: Bottom Toolbar
 * Setting > Appearance > Bottom toolbar
 */
@Composable
private fun BottomToolbarSettingsView(
    toolbarActions: List<ToolbarActionItem>,
    onNavigateToCustomize: () -> Unit,
    accentColor: Color
) {
    var showTabBadge by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Breadcrumb path indicator
            Text(
                text = "Settings > Appearance > Bottom toolbar",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Main Menu Item: "Customize bottom toolbar"
            // Leads to Level 3: Customize bottom toolbar
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

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Toolbar Active Preview
            Text(
                text = "Active Toolbar Preview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Current arrangement of visible icons in the bottom bar:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val enabledItems = toolbarActions.filter { it.isEnabled }
                    if (enabledItems.isEmpty()) {
                        Text("No buttons enabled", style = MaterialTheme.typography.bodySmall)
                    } else {
                        enabledItems.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.type.icon,
                                        contentDescription = item.type.title,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.type.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Additional bottom bar behavior toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Show Tab Count Badge", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    Text(text = "Display open tab counter on the tab button", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = showTabBadge,
                    onCheckedChange = { showTabBadge = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Haptic Vibration on Tap", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    Text(text = "Subtle vibration when tapping toolbar icons", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = hapticFeedback,
                    onCheckedChange = { hapticFeedback = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                )
            }
        }
    }
}

/**
 * Level 3: Customize Bottom Toolbar
 * Setting > Appearance > Bottom toolbar > Customize bottom toolbar
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
                text = "Settings > Appearance > Bottom toolbar > Customize",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rearrange & Toggle Toolbar Buttons",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Use the arrows to reorder icons in the bottom bar, and the switch to show or hide them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                    Icon(
                        imageVector = item.type.icon,
                        contentDescription = null,
                        tint = if (item.isEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )

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
    onSelectWallpaper: (String) -> Unit,
    accentColor: Color
) {
    var showSpeedDials by remember { mutableStateOf(true) }
    var showBookmarks by remember { mutableStateOf(true) }

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
                text = "Home Screen Background Wallpaper",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick an atmosphere for your new tab / speed dial home screen:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wallpaper Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WALLPAPER_OPTIONS.forEach { (name, color) ->
                val isSelected = (name == currentWallpaper)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectWallpaper(name) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = color),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor), width = 2.dp) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Speed Dials on Home",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showSpeedDials,
                onCheckedChange = { showSpeedDials = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Bookmarks on Home",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showBookmarks,
                onCheckedChange = { showBookmarks = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                )
            )
        }
    }
}

/**
 * Privacy & Security Settings
 * Features:
 * - Ads Blocker by uBlock
 * - Secure DNS (Google, Cloudflare, 9.9.9.9, and Custom add option)
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

    var blockCookies by remember { mutableStateOf(false) }
    var doNotTrack by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Ads Blocker by uBlock
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

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Blocks intrusive video ads, telemetry, trackers, and cosmetics ad slots powered by uBlock filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "Network filter: ON",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isAdBlockerEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "Cosmetic hiding: ON",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isAdBlockerEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Secure DNS Options (Google, Cloudflare, 9.9.9.9, and Custom add option)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Secure DNS Provider",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Encrypts domain queries to protect privacy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    dnsProviders.forEach { (providerName, description) ->
                        val isSelected = if (providerName == "Custom DNS") {
                            isCustomMode
                        } else {
                            !isCustomMode && (providerName == currentDns)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (providerName == "Custom DNS") {
                                        isCustomMode = true
                                    } else {
                                        isCustomMode = false
                                        onSelectDns(providerName)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (providerName == "Custom DNS") {
                                        isCustomMode = true
                                    } else {
                                        isCustomMode = false
                                        onSelectDns(providerName)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = providerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Custom DNS Input Field
                    if (isCustomMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Custom DNS Server Address",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customAddressInput,
                                    onValueChange = { customAddressInput = it },
                                    placeholder = { Text("e.g. 94.140.14.14 or https://dns.adguard.com/dns-query") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val trimmed = customAddressInput.trim()
                                        if (trimmed.isNotBlank()) {
                                            onSaveCustomDns(trimmed)
                                            onSelectDns("Custom: $trimmed")
                                        }
                                    },
                                    enabled = customAddressInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Save Custom DNS")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Extra Privacy settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Browser Tracking & Cookies",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Send 'Do Not Track' (DNT)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Request websites not to track your browsing session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = doNotTrack,
                            onCheckedChange = { doNotTrack = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Block Third-Party Cookies", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Prevent cross-site tracking across ad networks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = blockCookies,
                            onCheckedChange = { blockCookies = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class SettingsMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val id: String
)
