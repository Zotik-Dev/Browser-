package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DEFAULT_DNS_PROVIDERS
import com.example.model.DEFAULT_VPN_SERVERS
import com.example.model.SecureDnsProvider
import com.example.model.VpnServer
import com.example.model.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnSheet(
    vpnState: VpnState,
    onToggleVpn: () -> Unit,
    onSelectServer: (VpnServer) -> Unit,
    onSetUnblockAllSites: (Boolean) -> Unit,
    onSelectDns: (SecureDnsProvider) -> Unit,
    onUpdateCustomProxy: (enabled: Boolean, host: String, port: Int, type: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("vpn_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (vpnState.isConnected) Color(0xFF10B981).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vpnState.isConnected) Icons.Default.Lock else Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = if (vpnState.isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Apex VPN & Site Unblocker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (vpnState.isConnected) "Encrypted Tunnel Active • Sites Unblocked" else "Private Browsing & Anti-Censorship",
                            fontSize = 12.sp,
                            color = if (vpnState.isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_vpn_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tabs: Connect & Locations / DNS & Advanced
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("VPN & Locations") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Unblock & DNS") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Custom Proxy") }
                )
            }

            when (selectedTab) {
                0 -> VpnMainTab(
                    vpnState = vpnState,
                    onToggleVpn = onToggleVpn,
                    onSelectServer = onSelectServer
                )
                1 -> UnblockAndDnsTab(
                    vpnState = vpnState,
                    onSetUnblockAllSites = onSetUnblockAllSites,
                    onSelectDns = onSelectDns
                )
                2 -> CustomProxyTab(
                    vpnState = vpnState,
                    onUpdateCustomProxy = onUpdateCustomProxy
                )
            }
        }
    }
}

@Composable
private fun VpnMainTab(
    vpnState: VpnState,
    onToggleVpn: () -> Unit,
    onSelectServer: (VpnServer) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Power Connect Hub
        item {
            VpnPowerCard(vpnState = vpnState, onToggle = onToggleVpn)
        }

        // Server Locations Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Virtual Server Locations",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${DEFAULT_VPN_SERVERS.size} Available",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Server list
        items(DEFAULT_VPN_SERVERS) { server ->
            val isSelected = vpnState.selectedServer.id == server.id
            ServerItemCard(
                server = server,
                isSelected = isSelected,
                onClick = { onSelectServer(server) }
            )
        }
    }
}

@Composable
private fun VpnPowerCard(
    vpnState: VpnState,
    onToggle: () -> Unit
) {
    val isConnected = vpnState.isConnected
    val isConnecting = vpnState.isConnecting

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val haloColor by animateColorAsState(
        targetValue = when {
            isConnected -> Color(0xFF10B981)
            isConnecting -> Color(0xFFF59E0B)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        },
        label = "haloColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isConnected) Color(0xFF10B981).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Power Button with glowing rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                if (isConnected || isConnecting) {
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(haloColor.copy(alpha = 0.15f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isConnected -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                isConnecting -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                else -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                            }
                        )
                        .clickable(onClick = onToggle)
                        .testTag("vpn_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle VPN",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isConnected -> Color(0xFF10B981).copy(alpha = 0.15f)
                    isConnecting -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        isConnected -> Color(0xFF10B981)
                        isConnecting -> Color(0xFFF59E0B)
                        else -> Color.Transparent
                    }
                )
            ) {
                Text(
                    text = when {
                        isConnected -> "PROTECTED & ENCRYPTED"
                        isConnecting -> "CONNECTING SECURE TUNNEL..."
                        else -> "DISCONNECTED (TAP TO CONNECT)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isConnected -> Color(0xFF10B981)
                        isConnecting -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Virtual IP & Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Virtual IP",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isConnected) vpnState.currentVirtualIp else "Direct",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Location",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${vpnState.selectedServer.flagEmoji} ${vpnState.selectedServer.city}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Unblocked",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${vpnState.sitesUnblockedCount} sites",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerItemCard(
    server: VpnServer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("vpn_server_${server.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = server.flagEmoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (server.isRecommended) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "FASTEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${server.city} • Ping ${server.pingMs}ms",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (server.pingMs < 40) Color(0xFF10B981)
                        else if (server.pingMs < 60) Color(0xFFF59E0B)
                        else Color(0xFFEF4444)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UnblockAndDnsTab(
    vpnState: VpnState,
    onSetUnblockAllSites: (Boolean) -> Unit,
    onSelectDns: (SecureDnsProvider) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unblock All Sites Switch Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unblock All Sites",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bypasses ISP filtering, institutional firewalls, and geo-restrictions automatically.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = vpnState.unblockAllSites,
                        onCheckedChange = onSetUnblockAllSites,
                        modifier = Modifier.testTag("unblock_all_sites_switch")
                    )
                }
            }
        }

        // DNS Providers Header
        item {
            Text(
                text = "Secure DNS Resolvers (Anti-Censorship DoH)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // DNS Provider Items
        items(DEFAULT_DNS_PROVIDERS) { dns ->
            val isSelected = vpnState.selectedDns.id == dns.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDns(dns) }
                    .testTag("dns_${dns.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dns.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dns.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectDns(dns) },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomProxyTab(
    vpnState: VpnState,
    onUpdateCustomProxy: (enabled: Boolean, host: String, port: Int, type: String) -> Unit
) {
    var enabled by remember { mutableStateOf(vpnState.customProxyEnabled) }
    var host by remember { mutableStateOf(vpnState.customProxyHost) }
    var port by remember { mutableStateOf(vpnState.customProxyPort.toString()) }
    var proxyType by remember { mutableStateOf(vpnState.customProxyType) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Use Custom Proxy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Connect via your own HTTP or SOCKS5 proxy server.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 8080, proxyType)
                    },
                    modifier = Modifier.testTag("custom_proxy_switch")
                )
            }
        }

        if (enabled) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = {
                        proxyType = "HTTP"
                        onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 8080, "HTTP")
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (proxyType == "HTTP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "HTTP / HTTPS",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (proxyType == "HTTP") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Surface(
                    onClick = {
                        proxyType = "SOCKS5"
                        onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 1080, "SOCKS5")
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (proxyType == "SOCKS5") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "SOCKS5",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (proxyType == "SOCKS5") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            OutlinedTextField(
                value = host,
                onValueChange = {
                    host = it
                    onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 8080, proxyType)
                },
                label = { Text("Proxy Host / IP") },
                placeholder = { Text("e.g. 192.168.1.100 or proxy.mycompany.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_proxy_host_input")
            )

            OutlinedTextField(
                value = port,
                onValueChange = {
                    port = it
                    onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 8080, proxyType)
                },
                label = { Text("Port") },
                placeholder = { Text("e.g. 8080 or 1080") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_proxy_port_input")
            )

            Button(
                onClick = {
                    onUpdateCustomProxy(enabled, host, port.toIntOrNull() ?: 8080, proxyType)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_custom_proxy_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Proxy Configuration")
            }
        }
    }
}
