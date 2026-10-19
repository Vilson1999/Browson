package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HistoryEntity
import com.example.data.local.entities.SpeedDialEntity

sealed interface SelectedItemForAction {
    data class SpeedDial(val item: SpeedDialEntity) : SelectedItemForAction
    data class Bookmark(val item: BookmarkEntity) : SelectedItemForAction
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedDialsView(
    speedDials: List<SpeedDialEntity>,
    bookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity> = emptyList(),
    wallpaper: String,
    accentColor: Color,
    onOpenUrl: (String) -> Unit,
    onAddSpeedDial: (title: String, url: String, color: Long) -> Unit,
    onEditSpeedDial: (id: Long, title: String, url: String) -> Unit,
    onDeleteSpeedDial: (Long) -> Unit,
    onEditBookmark: (id: Long, title: String, url: String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Long press selection (shows center popup)
    var selectedItemForMenu by remember { mutableStateOf<SelectedItemForAction?>(null) }
    var itemToEdit by remember { mutableStateOf<SelectedItemForAction?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("speed_dials_view")
    ) {
        // Wallpaper background rendering
        WallpaperBackground(wallpaper = wallpaper)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Speed Dials Section Title
            item {
                Text(
                    text = "Speed Dials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Speed Dials Grid (4 per row, ending with '+' Add tile)
            item {
                val chunked = speedDials.chunked(4)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    chunked.forEach { rowDials ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowDials.forEach { dial ->
                                SpeedDialTile(
                                    title = dial.title,
                                    url = dial.url,
                                    bgColor = Color(dial.iconColor),
                                    accentBorder = accentColor,
                                    onClick = { onOpenUrl(dial.url) },
                                    onLongClick = { selectedItemForMenu = SelectedItemForAction.SpeedDial(dial) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining slots in row if less than 4
                            for (i in 0 until (4 - rowDials.size)) {
                                if (i == 0 && rowDials.size < 4) {
                                    AddSpeedDialTile(
                                        accentColor = accentColor,
                                        onClick = { showAddDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // If all rows were full of 4 items, put the Add button in its own row
                    if (speedDials.size % 4 == 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AddSpeedDialTile(
                                accentColor = accentColor,
                                onClick = { showAddDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Bookmarks Section (Rendered with website logo and letter fallback!)
            if (bookmarks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                item {
                    val chunkedBookmarks = bookmarks.chunked(4)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        chunkedBookmarks.forEach { rowBookmarks ->
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowBookmarks.forEach { bookmark ->
                                    SpeedDialTile(
                                        title = bookmark.title,
                                        url = bookmark.url,
                                        bgColor = Color(0xFF2C3E50),
                                        accentBorder = accentColor,
                                        onClick = { onOpenUrl(bookmark.url) },
                                        onLongClick = { selectedItemForMenu = SelectedItemForAction.Bookmark(bookmark) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                for (i in 0 until (4 - rowBookmarks.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Long Press Context Popup (Centered on Screen with 2 options: Edit and Delete)
    selectedItemForMenu?.let { selected ->
        val title = when (selected) {
            is SelectedItemForAction.SpeedDial -> selected.item.title
            is SelectedItemForAction.Bookmark -> selected.item.title
        }
        val url = when (selected) {
            is SelectedItemForAction.SpeedDial -> selected.item.url
            is SelectedItemForAction.Bookmark -> selected.item.url
        }

        AlertDialog(
            onDismissRequest = { selectedItemForMenu = null },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 1: Edit
                    OutlinedButton(
                        onClick = {
                            itemToEdit = selected
                            selectedItemForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Option 2: Delete
                    Button(
                        onClick = {
                            when (selected) {
                                is SelectedItemForAction.SpeedDial -> onDeleteSpeedDial(selected.item.id)
                                is SelectedItemForAction.Bookmark -> onDeleteBookmark(selected.item.id)
                            }
                            selectedItemForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedItemForMenu = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Dialog (Rename Title and Change Website URL)
    itemToEdit?.let { selected ->
        var editTitle by remember {
            mutableStateOf(
                when (selected) {
                    is SelectedItemForAction.SpeedDial -> selected.item.title
                    is SelectedItemForAction.Bookmark -> selected.item.title
                }
            )
        }
        var editUrl by remember {
            mutableStateOf(
                when (selected) {
                    is SelectedItemForAction.SpeedDial -> selected.item.url
                    is SelectedItemForAction.Bookmark -> selected.item.url
                }
            )
        }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Shortcut") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Rename Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("Change Website URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank() && editUrl.isNotBlank()) {
                            when (selected) {
                                is SelectedItemForAction.SpeedDial -> onEditSpeedDial(selected.item.id, editTitle.trim(), editUrl.trim())
                                is SelectedItemForAction.Bookmark -> onEditBookmark(selected.item.id, editTitle.trim(), editUrl.trim())
                            }
                            itemToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Speed Dial Dialog
    if (showAddDialog) {
        var inputTitle by remember { mutableStateOf("") }
        var inputUrl by remember { mutableStateOf("") }
        val colors = listOf(0xFF4285F4, 0xFFE5254A, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFF333333)
        var selectedColor by remember { mutableStateOf(colors[0]) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Speed Dial Shortcut") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Title (e.g. My Site)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("URL (e.g. example.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Tile Color", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { selectedColor = c }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotBlank() && inputUrl.isNotBlank()) {
                            onAddSpeedDial(inputTitle.trim(), inputUrl.trim(), selectedColor)
                            showAddDialog = false
                        }
                    },
                    enabled = inputTitle.isNotBlank() && inputUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WallpaperBackground(wallpaper: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            wallpaper.startsWith("custom:") -> {
                val uriString = wallpaper.removePrefix("custom:")
                AsyncImage(
                    model = Uri.parse(uriString),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                )
            }
            wallpaper == "wp_black_waves" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_black_waves),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }
            wallpaper == "wp_purple_waves" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_purple_waves),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }
            wallpaper == "wp_pink_peach" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_pink_peach),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.12f))
                )
            }
            wallpaper == "wp_cyan_blue" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_cyan_blue),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.12f))
                )
            }
            wallpaper == "wp_clean_dark" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_clean_dark),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            wallpaper == "wp_clean_light" -> {
                Image(
                    painter = painterResource(id = R.drawable.wp_clean_light),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
            }
            else -> {
                val fallbackBrush = Brush.verticalGradient(
                    listOf(Color(0xFF131A26), Color(0xFF0E131C), Color(0xFF090D14))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fallbackBrush)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeedDialTile(
    title: String,
    url: String,
    bgColor: Color,
    accentBorder: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = accentBorder.copy(alpha = 0.35f), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            WebsiteLogoIcon(
                url = url,
                title = title,
                bgColor = bgColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WebsiteLogoIcon(
    url: String,
    title: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    val domain = remember(url) {
        try {
            val uri = Uri.parse(if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url")
            val host = uri.host?.removePrefix("www.") ?: url
            host.ifBlank { url }
        } catch (_: Exception) {
            url
        }
    }

    val faviconUrl = remember(domain) {
        if (domain.isNotBlank() && !domain.startsWith("about:")) {
            "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        } else null
    }

    val initial = remember(title, domain) {
        (title.ifBlank { domain }).trim().firstOrNull()?.uppercaseChar()?.toString() ?: "W"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (faviconUrl != null) {
            SubcomposeAsyncImage(
                model = faviconUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
        }
    }
}

@Composable
private fun AddSpeedDialTile(
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(width = 1.dp, color = accentColor.copy(alpha = 0.35f), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Speed Dial",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Add",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
