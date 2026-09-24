package com.example.viewmodel

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkEntity
import com.example.data.BrowserRepository
import com.example.data.DownloadEntity
import com.example.data.HistoryEntity
import com.example.model.BrowserTab
import com.example.model.ReaderContent
import com.example.model.ReaderTheme
import com.example.model.SearchEngine
import com.example.model.SecurityState
import com.example.model.SpeedBoostState
import com.example.model.SpeedDialItem
import com.example.util.SpeedBooster
import com.example.util.UrlUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BrowserRepository(db.browserDao())
    }

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs Management
    private val initialTab = BrowserTab(url = "apex://home", title = "New Tab")
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab> = combine(_tabs, _activeTabId) { tabs, activeId ->
        tabs.firstOrNull { it.id == activeId } ?: tabs.firstOrNull() ?: BrowserTab()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialTab)

    // Bookmarked status for active tab
    val isCurrentPageBookmarked: StateFlow<Boolean> = activeTab
        .flatMapLatest { tab ->
            if (tab.isHome || tab.url.isBlank()) {
                flowOf(false)
            } else {
                repository.isBookmarked(tab.url)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Browser Settings & Toggles
    private val _searchEngine = MutableStateFlow(SearchEngine.GOOGLE)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _adBlockerEnabled = MutableStateFlow(true)
    val adBlockerEnabled: StateFlow<Boolean> = _adBlockerEnabled.asStateFlow()

    private val _javascriptEnabled = MutableStateFlow(true)
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled.asStateFlow()

    private val _trackersBlockedCount = MutableStateFlow(0)
    val trackersBlockedCount: StateFlow<Int> = _trackersBlockedCount.asStateFlow()

    // Enhanced Speed & Acceleration State
    private val _speedBoostState = MutableStateFlow(SpeedBoostState())
    val speedBoostState: StateFlow<SpeedBoostState> = _speedBoostState.asStateFlow()

    private val _showSpeedSheet = MutableStateFlow(false)
    val showSpeedSheet: StateFlow<Boolean> = _showSpeedSheet.asStateFlow()

    // Dialogs / Overlays
    private val _showTabOverview = MutableStateFlow(false)
    val showTabOverview: StateFlow<Boolean> = _showTabOverview.asStateFlow()

    private val _showBookmarksDialog = MutableStateFlow(false)
    val showBookmarksDialog: StateFlow<Boolean> = _showBookmarksDialog.asStateFlow()

    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    private val _showDownloadsDialog = MutableStateFlow(false)
    val showDownloadsDialog: StateFlow<Boolean> = _showDownloadsDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showClearDataDialog = MutableStateFlow(false)
    val showClearDataDialog: StateFlow<Boolean> = _showClearDataDialog.asStateFlow()

    private val _showAddSpeedDialDialog = MutableStateFlow(false)
    val showAddSpeedDialDialog: StateFlow<Boolean> = _showAddSpeedDialDialog.asStateFlow()

    // Reader Mode
    private val _readerContent = MutableStateFlow<ReaderContent?>(null)
    val readerContent: StateFlow<ReaderContent?> = _readerContent.asStateFlow()

    private val _readerFontSize = MutableStateFlow(18)
    val readerFontSize: StateFlow<Int> = _readerFontSize.asStateFlow()

    private val _readerTheme = MutableStateFlow(ReaderTheme.LIGHT)
    val readerTheme: StateFlow<ReaderTheme> = _readerTheme.asStateFlow()

    // Find in Page
    private val _isFindInPageActive = MutableStateFlow(false)
    val isFindInPageActive: StateFlow<Boolean> = _isFindInPageActive.asStateFlow()

    private val _findInPageQuery = MutableStateFlow("")
    val findInPageQuery: StateFlow<String> = _findInPageQuery.asStateFlow()

    private val _findInPageMatchIndex = MutableStateFlow(0)
    val findInPageMatchIndex: StateFlow<Int> = _findInPageMatchIndex.asStateFlow()

    private val _findInPageMatchCount = MutableStateFlow(0)
    val findInPageMatchCount: StateFlow<Int> = _findInPageMatchCount.asStateFlow()

    // Speed Dial Items
    private val _speedDialItems = MutableStateFlow(
        listOf(
            SpeedDialItem(title = "Google", url = "https://www.google.com", badgeLetter = "G", colorHex = 0xFF4285F4),
            SpeedDialItem(title = "Wikipedia", url = "https://en.wikipedia.org", badgeLetter = "W", colorHex = 0xFF1E293B),
            SpeedDialItem(title = "GitHub", url = "https://github.com", badgeLetter = "GH", colorHex = 0xFF24292E),
            SpeedDialItem(title = "Reddit", url = "https://www.reddit.com", badgeLetter = "R", colorHex = 0xFFFF4500),
            SpeedDialItem(title = "YouTube", url = "https://www.youtube.com", badgeLetter = "YT", colorHex = 0xFFFF0000),
            SpeedDialItem(title = "Hacker News", url = "https://news.ycombinator.com", badgeLetter = "HN", colorHex = 0xFFFF6600),
            SpeedDialItem(title = "DuckDuckGo", url = "https://duckduckgo.com", badgeLetter = "DDG", colorHex = 0xFFDE5833),
            SpeedDialItem(title = "Android Dev", url = "https://developer.android.com", badgeLetter = "AD", colorHex = 0xFF059669)
        )
    )
    val speedDialItems: StateFlow<List<SpeedDialItem>> = _speedDialItems.asStateFlow()

    // Tab Operations
    fun createNewTab(url: String = "apex://home", isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            url = url,
            title = if (url == "apex://home") "New Tab" else UrlUtils.getDisplayHost(url),
            displayUrl = if (url == "apex://home") "" else url,
            isIncognito = isIncognito,
            securityState = UrlUtils.getSecurityState(url)
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _showTabOverview.value = false
        _readerContent.value = null
        _isFindInPageActive.value = false
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            _showTabOverview.value = false
            _readerContent.value = null
            _isFindInPageActive.value = false
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index == -1) return

        val newTabs = currentTabs.filter { it.id != tabId }
        if (newTabs.isEmpty()) {
            val freshTab = BrowserTab(url = "apex://home", title = "New Tab")
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
        } else {
            _tabs.value = newTabs
            if (_activeTabId.value == tabId) {
                val nextActiveIndex = if (index >= newTabs.size) newTabs.size - 1 else index
                _activeTabId.value = newTabs[nextActiveIndex].id
            }
        }
        _readerContent.value = null
    }

    fun closeAllTabs(incognitoOnly: Boolean? = null) {
        when (incognitoOnly) {
            true -> {
                val remaining = _tabs.value.filterNot { it.isIncognito }
                if (remaining.isEmpty()) {
                    val fresh = BrowserTab(url = "apex://home", title = "New Tab")
                    _tabs.value = listOf(fresh)
                    _activeTabId.value = fresh.id
                } else {
                    _tabs.value = remaining
                    if (!_tabs.value.any { it.id == _activeTabId.value }) {
                        _activeTabId.value = remaining.first().id
                    }
                }
            }
            false -> {
                val remaining = _tabs.value.filter { it.isIncognito }
                if (remaining.isEmpty()) {
                    val fresh = BrowserTab(url = "apex://home", title = "New Tab")
                    _tabs.value = listOf(fresh)
                    _activeTabId.value = fresh.id
                } else {
                    _tabs.value = remaining
                    if (!_tabs.value.any { it.id == _activeTabId.value }) {
                        _activeTabId.value = remaining.first().id
                    }
                }
            }
            null -> {
                val fresh = BrowserTab(url = "apex://home", title = "New Tab")
                _tabs.value = listOf(fresh)
                _activeTabId.value = fresh.id
            }
        }
        _showTabOverview.value = false
        _readerContent.value = null
    }

    fun loadUrlInActiveTab(input: String) {
        val resolved = UrlUtils.resolveInput(input, _searchEngine.value)
        val activeId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        url = resolved,
                        displayUrl = if (resolved == "apex://home") "" else resolved,
                        title = UrlUtils.getDisplayHost(resolved),
                        isLoading = resolved != "apex://home",
                        progress = if (resolved != "apex://home") 10 else 0,
                        securityState = UrlUtils.getSecurityState(resolved)
                    )
                } else tab
            }
        }
        _readerContent.value = null
        _isFindInPageActive.value = false
    }

    fun updateActiveTabNavigation(
        title: String? = null,
        url: String? = null,
        progress: Int? = null,
        isLoading: Boolean? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null
    ) {
        val activeId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == activeId) {
                    val newUrl = url ?: tab.url
                    val newTitle = title ?: tab.title
                    val newDisplay = if (newUrl == "apex://home") "" else newUrl
                    tab.copy(
                        title = if (newTitle.isNotBlank()) newTitle else UrlUtils.getDisplayHost(newUrl),
                        url = newUrl,
                        displayUrl = newDisplay,
                        progress = progress ?: tab.progress,
                        isLoading = isLoading ?: tab.isLoading,
                        canGoBack = canGoBack ?: tab.canGoBack,
                        canGoForward = canGoForward ?: tab.canGoForward,
                        securityState = UrlUtils.getSecurityState(newUrl)
                    )
                } else tab
            }
        }

        // Record history if regular tab and valid URL
        if (url != null && isLoading == false) {
            val tab = _tabs.value.find { it.id == activeId }
            if (tab != null && !tab.isIncognito && !tab.isHome) {
                viewModelScope.launch {
                    repository.addHistory(tab.title, tab.url)
                }
            }
        }
    }

    fun toggleDesktopSite() {
        val activeId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(isDesktopSite = !tab.isDesktopSite)
                } else tab
            }
        }
    }

    fun toggleBookmarkCurrentPage() {
        val tab = activeTab.value
        if (tab.isHome || tab.url.isBlank()) return

        viewModelScope.launch {
            if (isCurrentPageBookmarked.value) {
                repository.removeBookmarkByUrl(tab.url)
            } else {
                repository.addBookmark(tab.title, tab.url)
            }
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch { repository.deleteBookmark(bookmark) }
    }

    fun deleteHistoryItem(historyItem: HistoryEntity) {
        viewModelScope.launch { repository.deleteHistory(historyItem) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearAllHistory() }
    }

    fun addDownloadRecord(fileName: String, url: String, fileSize: Long, mimeType: String) {
        viewModelScope.launch {
            repository.addDownload(fileName, url, fileSize, mimeType)
        }
    }

    fun deleteDownload(download: DownloadEntity) {
        viewModelScope.launch { repository.deleteDownload(download) }
    }

    fun clearAllDownloads() {
        viewModelScope.launch { repository.clearAllDownloads() }
    }

    fun incrementTrackersBlocked() {
        _trackersBlockedCount.update { it + 1 }
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
    }

    fun toggleAdBlocker() {
        _adBlockerEnabled.update { !it }
    }

    fun toggleJavascript() {
        _javascriptEnabled.update { !it }
    }

    fun addSpeedDial(title: String, url: String) {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val letter = title.take(2).uppercase()
        val randomColors = listOf(0xFF2563EB, 0xFF7C3AED, 0xFF059669, 0xFFDC2626, 0xFFD97706, 0xFF0891B2)
        val color = randomColors[(_speedDialItems.value.size) % randomColors.size]
        _speedDialItems.update {
            it + SpeedDialItem(title = title, url = normalized, badgeLetter = letter, colorHex = color)
        }
    }

    fun removeSpeedDial(id: String) {
        _speedDialItems.update { it.filterNot { item -> item.id == id } }
    }

    // Reader Mode controls
    fun setReaderContent(content: ReaderContent?) {
        _readerContent.value = content
    }

    fun closeReaderMode() {
        _readerContent.value = null
    }

    fun updateReaderFontSize(delta: Int) {
        _readerFontSize.update { (it + delta).coerceIn(12, 32) }
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    // Find In Page controls
    fun startFindInPage() {
        _isFindInPageActive.value = true
        _findInPageQuery.value = ""
        _findInPageMatchIndex.value = 0
        _findInPageMatchCount.value = 0
    }

    fun updateFindInPageQuery(query: String) {
        _findInPageQuery.value = query
    }

    fun setFindInPageResults(activeMatchIndex: Int, numberOfMatches: Int) {
        _findInPageMatchIndex.value = activeMatchIndex
        _findInPageMatchCount.value = numberOfMatches
    }

    fun closeFindInPage() {
        _isFindInPageActive.value = false
        _findInPageQuery.value = ""
        _findInPageMatchIndex.value = 0
        _findInPageMatchCount.value = 0
    }

    // Dialog Visibility controls
    fun setShowTabOverview(show: Boolean) { _showTabOverview.value = show }
    fun setShowBookmarksDialog(show: Boolean) { _showBookmarksDialog.value = show }
    fun setShowHistoryDialog(show: Boolean) { _showHistoryDialog.value = show }
    fun setShowDownloadsDialog(show: Boolean) { _showDownloadsDialog.value = show }
    fun setShowSettingsDialog(show: Boolean) { _showSettingsDialog.value = show }
    fun setShowClearDataDialog(show: Boolean) { _showClearDataDialog.value = show }
    fun setShowAddSpeedDialDialog(show: Boolean) { _showAddSpeedDialDialog.value = show }

    fun clearBrowsingData(clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean, webView: WebView?) {
        viewModelScope.launch {
            if (clearHistory) {
                repository.clearAllHistory()
            }
            if (clearCookies) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            if (clearCache) {
                webView?.clearCache(true)
                WebStorage.getInstance().deleteAllData()
            }
        }
    }

    // Enhanced Speed & Acceleration Methods
    fun setShowSpeedSheet(show: Boolean) {
        _showSpeedSheet.value = show
    }

    fun toggleEnhancedSpeed() {
        _speedBoostState.update { it.copy(isEnhancedSpeedEnabled = !it.isEnhancedSpeedEnabled) }
    }

    fun setAggressiveCache(enabled: Boolean) {
        _speedBoostState.update { it.copy(isAggressiveCacheEnabled = enabled) }
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        _speedBoostState.update { it.copy(isHardwareAccelerationEnabled = enabled) }
    }

    fun setPrefetchEnabled(enabled: Boolean) {
        _speedBoostState.update { it.copy(isPrefetchEnabled = enabled) }
    }

    fun setDataSaver(enabled: Boolean) {
        _speedBoostState.update { it.copy(isDataSaverEnabled = enabled) }
    }

    fun recordPageLoadTime(timeMs: Long) {
        if (timeMs <= 0) return
        _speedBoostState.update { current ->
            val newCount = current.totalRequestsAccelerated + 1
            val avg = if (current.lastPageLoadTimeMs == 0L) timeMs else (current.averageLoadTimeMs * 3 + timeMs) / 4
            val savedMs = if (timeMs < 1200) (1200 - timeMs).coerceAtLeast(0) else 150L
            val savedKb = (newCount * 124L)
            current.copy(
                lastPageLoadTimeMs = timeMs,
                averageLoadTimeMs = avg,
                totalRequestsAccelerated = newCount,
                estimatedDataSavedKb = savedKb,
                totalTimeSavedMs = current.totalTimeSavedMs + savedMs
            )
        }
    }

    fun clearSpeedMetrics() {
        _speedBoostState.update {
            it.copy(
                lastPageLoadTimeMs = 0L,
                averageLoadTimeMs = 260L,
                totalRequestsAccelerated = 0,
                estimatedDataSavedKb = 0L,
                totalTimeSavedMs = 0L
            )
        }
    }
}
