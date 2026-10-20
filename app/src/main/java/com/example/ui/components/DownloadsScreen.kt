package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DownloadsBackground = Color(0xFF0A1513)
private val DownloadsCard = Color(0xFF0F1918)
private val DownloadsSoft = Color(0xFF1A2725)
private val DownloadsBorder = Color(0xFF2B3E3A)
private val DownloadsText = Color(0xFFE9F2EF)
private val DownloadsSecondary = Color(0xFFB7C7C3)
private val DownloadsAccent = Color(0xFF8BD7BC)
private val DownloadsMuted = Color(0xFF7C8F8A)
private val DownloadMenuBg = Color(0xFF0F1717)

private enum class DownloadFilter(val label: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    APPS("Apps"),
    DOCUM("Docum")
}

data class DownloadItem(
    val id: String,
    val title: String,
    val size: String,
    val timeAgo: String,
    val type: DownloadType,
    val iconBackground: Color,
    val isDownloading: Boolean = false,
    val downloadedSize: String = "",
    val totalSize: String = "",
    val progress: Float = 0f,
    val speed: String = "",
    val timeLeft: String = ""
)

enum class DownloadType {
    IMAGE,
    VIDEO,
    APP,
    DOCUMENT
}

data class DownloadAction(
    val label: String,
    val icon: ImageVector
)

@Composable
fun DownloadsScreen(
    items: List<DownloadItem> = demoDownloadItems(),
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(DownloadFilter.IMAGES) }
    var openMenuFor by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DownloadsBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            DownloadHeader()

            Spacer(modifier = Modifier.height(18.dp))

            FilterRow(
                selectedFilter = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { item ->
                    DownloadRow(
                        item = item,
                        isMenuOpen = openMenuFor == item.id,
                        onMenuToggle = {
                            openMenuFor = if (openMenuFor == item.id) null else item.id
                        },
                        onDismissMenu = { openMenuFor = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {},
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DownloadsText,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Downloads",
            color = DownloadsText,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        IconButton(
            onClick = {},
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DownloadsText,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Filter menu",
                tint = DownloadsText,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = DownloadsText,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: DownloadFilter,
    onSelect: (DownloadFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(DownloadFilter.values()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onSelect(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (filter) {
                            DownloadFilter.IMAGES -> Icons.Default.Image
                            DownloadFilter.VIDEOS -> Icons.Default.Movie
                            DownloadFilter.APPS -> Icons.Default.Android
                            DownloadFilter.DOCUM -> Icons.Default.Description
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = filter.label,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(filter.label)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFF202A2D),
                    labelColor = DownloadsText,
                    selectedLabelColor = DownloadsText,
                    iconColor = DownloadsText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = DownloadsBorder,
                    selectedBorderColor = DownloadsBorder
                ),
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItem,
    isMenuOpen: Boolean,
    onMenuToggle: () -> Unit,
    onDismissMenu: () -> Unit
) {
    val actions = listOf(
        DownloadAction("Open with...", Icons.Default.OpenInNew),
        DownloadAction("Copy link", Icons.Default.Tune),
        DownloadAction("Share", Icons.Default.Share),
        DownloadAction("Hide", Icons.Default.VisibilityOff),
        DownloadAction("Rename", Icons.Default.Edit),
        DownloadAction("Delete", Icons.Default.Delete),
        DownloadAction("Send to Flow", Icons.Default.Send)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(item.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            DownloadType.IMAGE -> Icons.Default.Image
                            DownloadType.VIDEO -> Icons.Default.Movie
                            DownloadType.APP -> Icons.Default.Android
                            DownloadType.DOCUMENT -> Icons.Default.Description
                        },
                        contentDescription = item.title,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        color = DownloadsText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.size,
                            color = DownloadsSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "• ${item.timeAgo}",
                            color = DownloadsSecondary,
                            fontSize = 15.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onMenuToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = DownloadsText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (item.isDownloading) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = DownloadsAccent,
                        trackColor = Color(0xFF253836)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (item.speed.isNotBlank()) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (item.speed.isNotBlank()) "Pause download" else "Resume download",
                            tint = DownloadsText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.downloadedSize} / ${item.totalSize}",
                        color = DownloadsSecondary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "${item.speed} • ${item.timeLeft}",
                        color = DownloadsSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = onDismissMenu,
            modifier = Modifier
                .background(DownloadMenuBg)
                .border(1.dp, DownloadsBorder, RoundedCornerShape(14.dp))
                .width(240.dp)
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = DownloadsText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = action.label,
                                color = DownloadsText,
                                fontSize = 18.sp
                            )
                        }
                    },
                    onClick = onDismissMenu,
                    leadingIcon = null
                )
            }
        }
    }
}

private fun demoDownloadItems(): List<DownloadItem> = listOf(
    DownloadItem(
        id = "apk-1",
        title = "mpvRx-arm64-v8a-v3.0.0.apk",
        size = "74.15 MB",
        timeAgo = "9 hr ago",
        type = DownloadType.APP,
        iconBackground = Color(0xFF6F8F2F),
        isDownloading = true,
        downloadedSize = "70.5 MB",
        totalSize = "74.15 MB",
        progress = 0.94f,
        speed = "1.2 MB/s",
        timeLeft = "11 sec left"
    ),
    DownloadItem(
        id = "img-1",
        title = "ed419159-b777-4...2b4314cf03.jpeg",
        size = "32.05 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0D7072)
    ),
    DownloadItem(
        id = "img-2",
        title = "Wallpaper Cute.jpeg",
        size = "85.74 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0C6D69)
    ),
    DownloadItem(
        id = "img-3",
        title = "ba1fc54f-aeea-4d...ca1c9c25bd.jpeg",
        size = "38.13 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0B6B63)
    ),
    DownloadItem(
        id = "img-4",
        title = "654561ff-0bae-46...7e08945477.jpeg",
        size = "185 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0D7173)
    ),
    DownloadItem(
        id = "img-5",
        title = "Dreamy Night Sky... Wallpaper.jpeg",
        size = "11.08 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0B6D62)
    ),
    DownloadItem(
        id = "img-6",
        title = "New Samsung Gal...allaper HD.jpeg",
        size = "20.32 kB",
        timeAgo = "9 hr ago",
        type = DownloadType.IMAGE,
        iconBackground = Color(0xFF0E6C7B)
    ),
    DownloadItem(
        id = "txt-1",
        title = "build-apk.yml.txt",
        size = "0.93 kB",
        timeAgo = "10 hr ago",
        type = DownloadType.DOCUMENT,
        iconBackground = Color(0xFF0F7B76)
    ),
    DownloadItem(
        id = "apk-2",
        title = "Solipsism-Browser-v9.3.1.apk",
        size = "45.56 MB",
        timeAgo = "20 hr ago",
        type = DownloadType.APP,
        iconBackground = Color(0xFF7E9D39)
    )
)

@Preview(showBackground = true, widthDp = 430, heightDp = 930)
@Composable
private fun DownloadsScreenPreview() {
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
        DownloadsScreen(items = demoDownloadItems())
    }
}

@Preview(showBackground = true, widthDp = 430, heightDp = 930)
@Composable
private fun DownloadsScreenMenuPreview() {
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
        DownloadsScreen(
            items = demoDownloadItems().mapIndexed { index, item ->
                if (index == 0) item.copy() else item
            },
        )
    }
}
