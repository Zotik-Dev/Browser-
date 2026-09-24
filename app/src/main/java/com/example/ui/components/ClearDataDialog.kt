package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClearDataDialog(
    onConfirm: (clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Browsing Data", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Select data you want to remove:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearHistory = !clearHistory }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = clearHistory,
                        onCheckedChange = { clearHistory = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browsing history", fontSize = 14.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearCookies = !clearCookies }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = clearCookies,
                        onCheckedChange = { clearCookies = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cookies & site data", fontSize = 14.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clearCache = !clearCache }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = clearCache,
                        onCheckedChange = { clearCache = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cached web files", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(clearHistory, clearCookies, clearCache)
                    onDismiss()
                }
            ) {
                Text("Clear Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
