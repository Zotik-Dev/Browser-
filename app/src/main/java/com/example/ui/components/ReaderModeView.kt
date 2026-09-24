package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReaderContent
import com.example.model.ReaderTheme
import com.example.util.UrlUtils

@Composable
fun ReaderModeView(
    content: ReaderContent,
    fontSize: Int,
    theme: ReaderTheme,
    onFontSizeChange: (Int) -> Unit,
    onThemeChange: (ReaderTheme) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, headerColor) = when (theme) {
        ReaderTheme.LIGHT -> Triple(Color(0xFFFFFFFF), Color(0xFF1E293B), Color(0xFF0F172A))
        ReaderTheme.SEPIA -> Triple(Color(0xFFFBF0D9), Color(0xFF433422), Color(0xFF2C2216))
        ReaderTheme.DARK -> Triple(Color(0xFF18181B), Color(0xFFE4E4E7), Color(0xFFF4F4F5))
    }

    Surface(
        color = backgroundColor,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Reader Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("reader_close_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit reader mode",
                        tint = textColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font Size -
                    IconButton(
                        onClick = { onFontSizeChange(-2) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "A-",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Font Size +
                    IconButton(
                        onClick = { onFontSizeChange(2) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "A+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Theme selector buttons
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(textColor.copy(alpha = 0.1f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Light
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onThemeChange(ReaderTheme.LIGHT) }
                        )
                        // Sepia
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFBF0D9))
                                .clickable { onThemeChange(ReaderTheme.SEPIA) }
                        )
                        // Dark
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF18181B))
                                .clickable { onThemeChange(ReaderTheme.DARK) }
                        )
                    }
                }
            }

            HorizontalDivider(color = textColor.copy(alpha = 0.12f))

            // Article Content Scroll Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .testTag("reader_content_scroll")
            ) {
                Text(
                    text = content.title,
                    fontSize = (fontSize + 8).sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor,
                    lineHeight = (fontSize + 14).sp,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = UrlUtils.getDisplayHost(content.url),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                val paragraphs = content.textContent.split("\n\n")
                paragraphs.forEach { paragraph ->
                    val trimmed = paragraph.trim()
                    if (trimmed.isNotEmpty()) {
                        Text(
                            text = trimmed,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.6).sp,
                            color = textColor,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
