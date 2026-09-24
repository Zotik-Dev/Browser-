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

data class SpeedBoostState(
    val isEnhancedSpeedEnabled: Boolean = true,
    val isAggressiveCacheEnabled: Boolean = true,
    val isHardwareAccelerationEnabled: Boolean = true,
    val isPrefetchEnabled: Boolean = true,
    val isDataSaverEnabled: Boolean = false,
    val lastPageLoadTimeMs: Long = 0L,
    val averageLoadTimeMs: Long = 260L,
    val totalRequestsAccelerated: Int = 18,
    val estimatedDataSavedKb: Long = 2240L,
    val totalTimeSavedMs: Long = 8400L
)

