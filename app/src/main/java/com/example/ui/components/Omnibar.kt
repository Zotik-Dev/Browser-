package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.model.BrowserTab
import com.example.model.SecurityState
import com.example.model.SpeedBoostState
import com.example.util.SpeedBooster

@Composable
fun Omnibar(
    tab: BrowserTab,
    isBookmarked: Boolean,
    adBlockerEnabled: Boolean,
    trackersBlocked: Int,
    speedState: SpeedBoostState,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onStopLoading: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onOpenReaderMode: () -> Unit,
    onFindInPage: () -> Unit,
    onShare: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenClearData: () -> Unit,
    onOpenSpeedSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember(tab.id, tab.url) {
        mutableStateOf(if (tab.isHome) "" else tab.url)
    }
    var showMenu by remember { mutableStateOf(false) }
    var showShieldDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shield / Security Button
                IconButton(
                    onClick = { showShieldDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("omnibar_shield_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (adBlockerEnabled && trackersBlocked > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = if (trackersBlocked > 99) "99+" else trackersBlocked.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        when (tab.securityState) {
                            SecurityState.SECURE_HTTPS -> {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure HTTPS",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            SecurityState.INSECURE_HTTP -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Not secure HTTP",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            SecurityState.INTERNAL_HOME -> {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "Apex Shield",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Address & Search Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isEditing && tab.isHome) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (!isEditing) {
                                Text(
                                    text = if (tab.isHome) "Search or enter address" else tab.displayUrl.ifBlank { tab.title },
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = if (tab.isHome) FontWeight.Normal else FontWeight.Medium,
                                        color = if (tab.isHome) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isEditing = true
                                            textInput = if (tab.isHome) "" else tab.url
                                        }
                                        .testTag("omnibar_display_text")
                                )
                            } else {
                                BasicTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        imeAction = ImeAction.Go
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            isEditing = false
                                            focusManager.clearFocus()
                                            onNavigate(textInput)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .onFocusChanged { state ->
                                            if (!state.isFocused) {
                                                isEditing = false
                                            }
                                        }
                                        .testTag("omnibar_input_field")
                                )
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                            }
                        }

                        if (isEditing && textInput.isNotEmpty()) {
                            IconButton(
                                onClick = { textInput = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Speed Booster / Turbo Quick Status Button
                Surface(
                    onClick = onOpenSpeedSheet,
                    shape = RoundedCornerShape(12.dp),
                    color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .padding(horizontal = 2.dp)
                        .testTag("omnibar_speed_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Speed Booster & Turbo",
                            tint = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (speedState.isEnhancedSpeedEnabled) {
                                if (speedState.lastPageLoadTimeMs > 0) SpeedBooster.formatLoadTime(speedState.lastPageLoadTimeMs) else "TURBO"
                            } else "FAST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Reload or Stop Loading
                if (tab.isLoading) {
                    IconButton(
                        onClick = onStopLoading,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("omnibar_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (!tab.isHome) {
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("omnibar_reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("omnibar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Enhanced Speed Booster
                        DropdownMenuItem(
                            text = { Text("Speed Booster & Turbo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (speedState.isEnhancedSpeedEnabled) "TURBO" else "OFF",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onOpenSpeedSheet()
                            },
                            modifier = Modifier.testTag("menu_speed_item")
                        )

                        HorizontalDivider()

                        // Desktop Site Toggle
                        DropdownMenuItem(
                            text = { Text("Desktop Site") },
                            leadingIcon = {
                                Icon(Icons.Default.DesktopWindows, contentDescription = null)
                            },
                            trailingIcon = {
                                Switch(
                                    checked = tab.isDesktopSite,
                                    onCheckedChange = {
                                        showMenu = false
                                        onToggleDesktopSite()
                                    }
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleDesktopSite()
                            }
                        )

                        if (!tab.isHome) {
                            // Reader Mode
                            DropdownMenuItem(
                                text = { Text("Reader View") },
                                leadingIcon = {
                                    Icon(Icons.Default.ChromeReaderMode, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onOpenReaderMode()
                                }
                            )

                            // Find in Page
                            DropdownMenuItem(
                                text = { Text("Find in Page") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.FindInPage, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onFindInPage()
                                }
                            )

                            // Share Link
                            DropdownMenuItem(
                                text = { Text("Share Link") },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onShare()
                                }
                            )

                            // Add / Remove Bookmark
                            DropdownMenuItem(
                                text = { Text(if (isBookmarked) "Remove Bookmark" else "Add Bookmark") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                        tint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleBookmark()
                                }
                            )
                            HorizontalDivider()
                        }

                        // Bookmarks
                        DropdownMenuItem(
                            text = { Text("Bookmarks") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenBookmarks()
                            }
                        )

                        // History
                        DropdownMenuItem(
                            text = { Text("History") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenHistory()
                            }
                        )

                        // Downloads
                        DropdownMenuItem(
                            text = { Text("Downloads") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenDownloads()
                            }
                        )

                        HorizontalDivider()

                        // Settings
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )

                        // Clear Data
                        DropdownMenuItem(
                            text = { Text("Clear Browsing Data") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenClearData()
                            }
                        )
                    }
                }
            }

            // Progress bar
            AnimatedVisibility(visible = tab.isLoading && tab.progress in 1..99) {
                LinearProgressIndicator(
                    progress = { tab.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .testTag("omnibar_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }

    // Shield / Privacy Quick Info Dialog
    if (showShieldDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showShieldDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apex Shield Protection")
                }
            },
            text = {
                Column {
                    Text(
                        text = if (adBlockerEnabled) "Ad & Tracker Shield is ACTIVE." else "Shield is disabled.",
                        fontWeight = FontWeight.SemiBold,
                        color = if (adBlockerEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trackers and ad scripts blocked: $trackersBlocked")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (speedState.isEnhancedSpeedEnabled)
                            "Speed Booster: Turbo Active (${SpeedBooster.formatLoadTime(speedState.averageLoadTimeMs)} avg load time)"
                        else
                            "Speed Booster: Standard",
                        fontWeight = FontWeight.Medium,
                        color = if (speedState.isEnhancedSpeedEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (tab.securityState) {
                            SecurityState.SECURE_HTTPS -> "Connection is secure (HTTPS)."
                            SecurityState.INSECURE_HTTP -> "Connection is not encrypted (HTTP)."
                            SecurityState.INTERNAL_HOME -> "Internal Apex Start Page."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showShieldDialog = false
                        onOpenSpeedSheet()
                    }
                ) {
                    Text("Speed Settings")
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showShieldDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
