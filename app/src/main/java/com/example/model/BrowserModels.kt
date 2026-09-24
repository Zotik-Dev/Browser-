package com.example.model

import java.util.UUID

enum class SecurityState {
    SECURE_HTTPS,
    INSECURE_HTTP,
    INTERNAL_HOME
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "apex://home",
    val title: String = "New Tab",
    val displayUrl: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopSite: Boolean = false,
    val securityState: SecurityState = SecurityState.INTERNAL_HOME
) {
    val isHome: Boolean get() = url == "apex://home" || url == "about:blank" || url.isBlank()
}

enum class SearchEngine(val displayName: String, val searchUrlTemplate: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "https://www.bing.com/search?q=%s"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=%s"),
    BRAVE("Brave", "https://search.brave.com/search?q=%s");

    fun buildSearchUrl(query: String): String {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        return searchUrlTemplate.replace("%s", encoded)
    }
}

data class SpeedDialItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val badgeLetter: String,
    val colorHex: Long
)

enum class ReaderTheme(val displayName: String) {
    LIGHT("Light"),
    SEPIA("Sepia"),
    DARK("Dark")
}

data class ReaderContent(
    val title: String,
    val byline: String = "",
    val textContent: String,
    val url: String
)

data class VpnServer(
    val id: String,
    val name: String,
    val city: String,
    val countryCode: String,
    val flagEmoji: String,
    val proxyHost: String,
    val proxyPort: Int,
    val pingMs: Int,
    val isRecommended: Boolean = false
)

data class SecureDnsProvider(
    val id: String,
    val name: String,
    val description: String,
    val primaryIp: String,
    val dohUrl: String
)

data class VpnState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val selectedServer: VpnServer = DEFAULT_VPN_SERVERS[0],
    val unblockAllSites: Boolean = true,
    val selectedDns: SecureDnsProvider = DEFAULT_DNS_PROVIDERS[0],
    val customProxyEnabled: Boolean = false,
    val customProxyHost: String = "",
    val customProxyPort: Int = 8080,
    val customProxyType: String = "HTTP", // HTTP or SOCKS5
    val currentVirtualIp: String = "104.28.19.42",
    val sessionDurationSeconds: Long = 0L,
    val sitesUnblockedCount: Int = 0
)

val DEFAULT_VPN_SERVERS = listOf(
    VpnServer("auto", "Auto (Fastest)", "New York", "US", "🇺🇸", "us-east.apexvpn.net", 8443, 24, isRecommended = true),
    VpnServer("us_west", "United States", "Los Angeles", "US", "🇺🇸", "us-west.apexvpn.net", 8443, 38),
    VpnServer("uk", "United Kingdom", "London", "GB", "🇬🇧", "uk.apexvpn.net", 8443, 32),
    VpnServer("de", "Germany", "Frankfurt", "DE", "🇩🇪", "de.apexvpn.net", 8443, 29),
    VpnServer("nl", "Netherlands", "Amsterdam", "NL", "🇳🇱", "nl.apexvpn.net", 8443, 31),
    VpnServer("sg", "Singapore", "Singapore", "SG", "🇸🇬", "sg.apexvpn.net", 8443, 52),
    VpnServer("jp", "Japan", "Tokyo", "JP", "🇯🇵", "jp.apexvpn.net", 8443, 65),
    VpnServer("ca", "Canada", "Toronto", "CA", "🇨🇦", "ca.apexvpn.net", 8443, 44)
)

val DEFAULT_DNS_PROVIDERS = listOf(
    SecureDnsProvider("cloudflare", "Cloudflare DNS", "Ultra-fast & privacy-focused (1.1.1.1)", "1.1.1.1", "https://cloudflare-dns.com/dns-query"),
    SecureDnsProvider("google", "Google Public DNS", "Global scale & high reliability (8.8.8.8)", "8.8.8.8", "https://dns.google/dns-query"),
    SecureDnsProvider("quad9", "Quad9 DNS", "Automatic malicious site & threat blocking (9.9.9.9)", "9.9.9.9", "https://dns.quad9.net/dns-query"),
    SecureDnsProvider("adguard", "AdGuard DNS", "Bypasses ISP blocks & stops ads (94.140.14.14)", "94.140.14.14", "https://dns.adguard.com/dns-query")
)
