package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab

@Composable
fun TopAddressBar(
    activeTab: BrowserTab,
    isEditing: Boolean,
    inputText: String,
    isBookmarked: Boolean,
    tabCount: Int = 1,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onStartEditing: () -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onCancelEditing: () -> Unit,
    onToggleBookmark: () -> Unit,
    onReloadOrStop: () -> Unit,
    onOpenTabs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star / Bookmark icon on the left (matches screenshot)
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("address_bar_star_button")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                    tint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Center Pill Search / Address Input
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(enabled = !isEditing) { onStartEditing() }
                    .testTag("address_bar_container"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEditing && !activeTab.isHome) {
                        val lockTint = if (activeTab.isSecure) Color(0xFF10B981) else Color(0xFFEF4444)
                        Icon(
                            imageVector = if (activeTab.isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (activeTab.isSecure) "Secure HTTPS" else "Not Secure",
                            tint = lockTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Text Field or Text Display
                    if (isEditing) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .testTag("address_bar_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    onSubmit(inputText)
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "Search or type URL",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { onInputChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (activeTab.isHome) "Search or type URL" else activeTab.displayHost,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (activeTab.isHome) FontWeight.Normal else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (activeTab.isHome) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (!activeTab.isHome) {
                            IconButton(
                                onClick = onReloadOrStop,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (activeTab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                    contentDescription = if (activeTab.isLoading) "Stop Loading" else "Reload",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Right Action: Tab counter icon on top right (matches screenshot)
            if (isEditing) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        onCancelEditing()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenTabs() }
                        .testTag("top_tab_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                width = 1.8.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(5.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tabCount > 99) "99+" else "$tabCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Animated Web Progress Indicator
        AnimatedVisibility(
            visible = activeTab.isLoading && !activeTab.isHome,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { activeTab.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = accentColor,
                trackColor = Color.Transparent
            )
        }
    }
}
