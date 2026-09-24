package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddSpeedDialDialog
import com.example.ui.components.BookmarksDialog
import com.example.ui.components.BottomBrowserBar
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.DownloadsDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.HistoryDialog
import com.example.ui.components.NewTabPage
import com.example.ui.components.Omnibar
import com.example.ui.components.ReaderModeView
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeedBoostSheet
import com.example.ui.components.TabOverviewSheet
import com.example.ui.components.WebContainer
import com.example.ui.components.extractReaderModeContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle external VIEW intent (e.g. clicking a link in another app)
        intent?.dataString?.let { url ->
            if (url.startsWith("http://") || url.startsWith("https://")) {
                viewModel.createNewTab(url)
            }
        }

        setContent {
            val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

            MyApplicationTheme(isIncognito = activeTab.isIncognito) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val context = LocalContext.current

    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isCurrentPageBookmarked.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val adBlockerEnabled by viewModel.adBlockerEnabled.collectAsStateWithLifecycle()
    val javascriptEnabled by viewModel.javascriptEnabled.collectAsStateWithLifecycle()
    val trackersBlocked by viewModel.trackersBlockedCount.collectAsStateWithLifecycle()
    val speedDialItems by viewModel.speedDialItems.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val speedState by viewModel.speedBoostState.collectAsStateWithLifecycle()

    // Dialog & overlay states
    val showTabOverview by viewModel.showTabOverview.collectAsStateWithLifecycle()
    val showBookmarksDialog by viewModel.showBookmarksDialog.collectAsStateWithLifecycle()
    val showHistoryDialog by viewModel.showHistoryDialog.collectAsStateWithLifecycle()
    val showDownloadsDialog by viewModel.showDownloadsDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showClearDataDialog by viewModel.showClearDataDialog.collectAsStateWithLifecycle()
    val showAddSpeedDialDialog by viewModel.showAddSpeedDialDialog.collectAsStateWithLifecycle()
    val showSpeedSheet by viewModel.showSpeedSheet.collectAsStateWithLifecycle()

    // Reader Mode & Find in page
    val readerContent by viewModel.readerContent.collectAsStateWithLifecycle()
    val readerFontSize by viewModel.readerFontSize.collectAsStateWithLifecycle()
    val readerTheme by viewModel.readerTheme.collectAsStateWithLifecycle()

    val isFindInPageActive by viewModel.isFindInPageActive.collectAsStateWithLifecycle()
    val findInPageQuery by viewModel.findInPageQuery.collectAsStateWithLifecycle()
    val findInPageMatchIndex by viewModel.findInPageMatchIndex.collectAsStateWithLifecycle()
    val findInPageMatchCount by viewModel.findInPageMatchCount.collectAsStateWithLifecycle()

    // Keep reference to the active WebView for actions
    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    // System Back Press Handling
    BackHandler(enabled = true) {
        when {
            showSpeedSheet -> viewModel.setShowSpeedSheet(false)
            isFindInPageActive -> viewModel.closeFindInPage()
            readerContent != null -> viewModel.closeReaderMode()
            showTabOverview -> viewModel.setShowTabOverview(false)
            showBookmarksDialog -> viewModel.setShowBookmarksDialog(false)
            showHistoryDialog -> viewModel.setShowHistoryDialog(false)
            showDownloadsDialog -> viewModel.setShowDownloadsDialog(false)
            showSettingsDialog -> viewModel.setShowSettingsDialog(false)
            showClearDataDialog -> viewModel.setShowClearDataDialog(false)
            showAddSpeedDialDialog -> viewModel.setShowAddSpeedDialDialog(false)
            activeWebView?.canGoBack() == true -> activeWebView?.goBack()
            !activeTab.isHome -> viewModel.loadUrlInActiveTab("apex://home")
            tabs.size > 1 -> viewModel.closeTab(activeTab.id)
            else -> {
                (context as? ComponentActivity)?.finish()
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Omnibar(
                    tab = activeTab,
                    isBookmarked = isBookmarked,
                    adBlockerEnabled = adBlockerEnabled,
                    trackersBlocked = trackersBlocked,
                    speedState = speedState,
                    onNavigate = { input -> viewModel.loadUrlInActiveTab(input) },
                    onReload = { activeWebView?.reload() },
                    onStopLoading = { activeWebView?.stopLoading() },
                    onToggleBookmark = {
                        viewModel.toggleBookmarkCurrentPage()
                        val msg = if (isBookmarked) "Bookmark removed" else "Page bookmarked"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onToggleDesktopSite = { viewModel.toggleDesktopSite() },
                    onOpenReaderMode = {
                        activeWebView?.let { wv ->
                            extractReaderModeContent(wv) { extracted ->
                                if (extracted != null && extracted.textContent.isNotBlank()) {
                                    viewModel.setReaderContent(extracted)
                                } else {
                                    Toast.makeText(context, "Could not extract article text", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onFindInPage = { viewModel.startFindInPage() },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, activeTab.title)
                            putExtra(Intent.EXTRA_TEXT, activeTab.url)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share link"))
                    },
                    onOpenBookmarks = { viewModel.setShowBookmarksDialog(true) },
                    onOpenHistory = { viewModel.setShowHistoryDialog(true) },
                    onOpenDownloads = { viewModel.setShowDownloadsDialog(true) },
                    onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                    onOpenClearData = { viewModel.setShowClearDataDialog(true) },
                    onOpenSpeedSheet = { viewModel.setShowSpeedSheet(true) }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().imePadding()) {
                if (isFindInPageActive) {
                    FindInPageBar(
                        query = findInPageQuery,
                        matchIndex = findInPageMatchIndex,
                        matchCount = findInPageMatchCount,
                        onQueryChange = { query ->
                            viewModel.updateFindInPageQuery(query)
                            activeWebView?.findAllAsync(query)
                        },
                        onNextMatch = { activeWebView?.findNext(true) },
                        onPreviousMatch = { activeWebView?.findNext(false) },
                        onClose = { viewModel.closeFindInPage() }
                    )
                }

                BottomBrowserBar(
                    tab = activeTab,
                    tabsCount = tabs.size,
                    isBookmarked = isBookmarked,
                    onBack = { activeWebView?.goBack() },
                    onForward = { activeWebView?.goForward() },
                    onHome = { viewModel.loadUrlInActiveTab("apex://home") },
                    onNewTab = { viewModel.createNewTab() },
                    onOpenTabOverview = { viewModel.setShowTabOverview(true) },
                    onToggleBookmark = {
                        viewModel.toggleBookmarkCurrentPage()
                        val msg = if (isBookmarked) "Bookmark removed" else "Page bookmarked"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area
            if (activeTab.isHome) {
                NewTabPage(
                    isIncognito = activeTab.isIncognito,
                    searchEngine = searchEngine,
                    trackersBlocked = trackersBlocked,
                    adBlockerEnabled = adBlockerEnabled,
                    speedState = speedState,
                    speedDialItems = speedDialItems,
                    bookmarks = bookmarks,
                    recentHistory = history,
                    onOpenUrl = { url -> viewModel.loadUrlInActiveTab(url) },
                    onAddSpeedDial = { viewModel.setShowAddSpeedDialDialog(true) },
                    onToggleSpeedBoost = { viewModel.toggleEnhancedSpeed() },
                    onOpenSpeedSheet = { viewModel.setShowSpeedSheet(true) }
                )
            } else {
                WebContainer(
                    tab = activeTab,
                    adBlockerEnabled = adBlockerEnabled,
                    javascriptEnabled = javascriptEnabled,
                    speedBoostState = speedState,
                    findInPageQuery = findInPageQuery,
                    isFindInPageActive = isFindInPageActive,
                    onNavigationStateChanged = { title, url, progress, isLoading, canGoBack, canGoForward ->
                        viewModel.updateActiveTabNavigation(
                            title = title,
                            url = url,
                            progress = progress,
                            isLoading = isLoading,
                            canGoBack = canGoBack,
                            canGoForward = canGoForward
                        )
                    },
                    onTrackerBlocked = { viewModel.incrementTrackersBlocked() },
                    onDownloadStarted = { fileName, url, fileSize, mimeType ->
                        viewModel.addDownloadRecord(fileName, url, fileSize, mimeType)
                    },
                    onReaderContentExtracted = { content ->
                        viewModel.setReaderContent(content)
                    },
                    onFindMatchCountUpdated = { matchIndex, count ->
                        viewModel.setFindInPageResults(matchIndex, count)
                    },
                    onWebViewCreated = { webView ->
                        activeWebView = webView
                    },
                    onPageLoadMetrics = { durationMs ->
                        viewModel.recordPageLoadTime(durationMs)
                    }
                )
            }

            // Reader Mode Overlay
            readerContent?.let { content ->
                ReaderModeView(
                    content = content,
                    fontSize = readerFontSize,
                    theme = readerTheme,
                    onFontSizeChange = { delta -> viewModel.updateReaderFontSize(delta) },
                    onThemeChange = { theme -> viewModel.setReaderTheme(theme) },
                    onClose = { viewModel.closeReaderMode() }
                )
            }

            // Tab Overview Switcher Overlay
            AnimatedVisibility(
                visible = showTabOverview,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                TabOverviewSheet(
                    tabs = tabs,
                    activeTabId = activeTabId,
                    onSelectTab = { tabId -> viewModel.selectTab(tabId) },
                    onCloseTab = { tabId -> viewModel.closeTab(tabId) },
                    onNewTab = { isIncognito -> viewModel.createNewTab(isIncognito = isIncognito) },
                    onCloseAllTabs = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
                    onDismiss = { viewModel.setShowTabOverview(false) }
                )
            }
        }
    }

    // Modal Dialogs
    if (showBookmarksDialog) {
        BookmarksDialog(
            bookmarks = bookmarks,
            onSelectBookmark = { url ->
                viewModel.setShowBookmarksDialog(false)
                viewModel.loadUrlInActiveTab(url)
            },
            onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) },
            onDismiss = { viewModel.setShowBookmarksDialog(false) }
        )
    }

    if (showHistoryDialog) {
        HistoryDialog(
            history = history,
            onSelectHistory = { url ->
                viewModel.setShowHistoryDialog(false)
                viewModel.loadUrlInActiveTab(url)
            },
            onDeleteHistoryItem = { item -> viewModel.deleteHistoryItem(item) },
            onClearAllHistory = { viewModel.clearAllHistory() },
            onDismiss = { viewModel.setShowHistoryDialog(false) }
        )
    }

    if (showDownloadsDialog) {
        DownloadsDialog(
            downloads = downloads,
            onDeleteDownload = { download -> viewModel.deleteDownload(download) },
            onClearAllDownloads = { viewModel.clearAllDownloads() },
            onDismiss = { viewModel.setShowDownloadsDialog(false) }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            searchEngine = searchEngine,
            adBlockerEnabled = adBlockerEnabled,
            javascriptEnabled = javascriptEnabled,
            speedState = speedState,
            onSearchEngineChange = { engine -> viewModel.setSearchEngine(engine) },
            onToggleAdBlocker = { viewModel.toggleAdBlocker() },
            onToggleJavascript = { viewModel.toggleJavascript() },
            onToggleSpeedBoost = { viewModel.toggleEnhancedSpeed() },
            onToggleAggressiveCache = { viewModel.setAggressiveCache(it) },
            onOpenSpeedSheet = { viewModel.setShowSpeedSheet(true) },
            onOpenClearData = {
                viewModel.setShowSettingsDialog(false)
                viewModel.setShowClearDataDialog(true)
            },
            onDismiss = { viewModel.setShowSettingsDialog(false) }
        )
    }

    if (showClearDataDialog) {
        ClearDataDialog(
            onConfirm = { clearHistory, clearCookies, clearCache ->
                viewModel.clearBrowsingData(clearHistory, clearCookies, clearCache, activeWebView)
                Toast.makeText(context, "Selected data cleared", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { viewModel.setShowClearDataDialog(false) }
        )
    }

    if (showAddSpeedDialDialog) {
        AddSpeedDialDialog(
            onAdd = { title, url ->
                viewModel.addSpeedDial(title, url)
                Toast.makeText(context, "Shortcut added", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { viewModel.setShowAddSpeedDialDialog(false) }
        )
    }

    // Apex Speed & Performance Booster Sheet
    if (showSpeedSheet) {
        SpeedBoostSheet(
            speedState = speedState,
            onToggleEnhancedSpeed = { viewModel.toggleEnhancedSpeed() },
            onToggleAggressiveCache = { viewModel.setAggressiveCache(it) },
            onToggleHardwareAcceleration = { viewModel.setHardwareAcceleration(it) },
            onTogglePrefetch = { viewModel.setPrefetchEnabled(it) },
            onToggleDataSaver = { viewModel.setDataSaver(it) },
            onClearSpeedMetrics = { viewModel.clearSpeedMetrics() },
            onDismiss = { viewModel.setShowSpeedSheet(false) }
        )
    }
}
