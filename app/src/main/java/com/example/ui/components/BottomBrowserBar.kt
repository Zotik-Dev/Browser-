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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab

@Composable
fun BottomBrowserBar(
    tab: BrowserTab,
    tabsCount: Int,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onNewTab: () -> Unit,
    onOpenTabOverview: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                enabled = tab.canGoBack,
                modifier = Modifier.testTag("bottom_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (tab.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // Forward button
            IconButton(
                onClick = onForward,
                enabled = tab.canGoForward,
                modifier = Modifier.testTag("bottom_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (tab.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // Home button
            IconButton(
                onClick = onHome,
                modifier = Modifier.testTag("bottom_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (tab.isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Quick Star Bookmark
            IconButton(
                onClick = onToggleBookmark,
                enabled = !tab.isHome,
                modifier = Modifier.testTag("bottom_bookmark_button")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Bookmark",
                    tint = when {
                        tab.isHome -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        isBookmarked -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            // Tab Switcher Button (Box with counter or incognito mask)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.8.dp,
                        color = if (tab.isIncognito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onOpenTabOverview() }
                    .testTag("bottom_tabs_button"),
                contentAlignment = Alignment.Center
            ) {
                if (tab.isIncognito) {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = "Incognito Tabs",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = if (tabsCount > 99) ":)" else tabsCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // New Tab button
            IconButton(
                onClick = onNewTab,
                modifier = Modifier.testTag("bottom_new_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Tab",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
