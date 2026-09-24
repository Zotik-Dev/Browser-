package com.example.ui.components

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BrowserTab
import com.example.model.ReaderContent
import com.example.model.SpeedBoostState
import com.example.util.AdBlocker
import com.example.util.SpeedBooster
import com.example.util.UrlUtils
import org.json.JSONObject

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebContainer(
    tab: BrowserTab,
    adBlockerEnabled: Boolean,
    javascriptEnabled: Boolean,
    speedBoostState: SpeedBoostState = SpeedBoostState(),
    findInPageQuery: String,
    isFindInPageActive: Boolean,
    onNavigationStateChanged: (title: String?, url: String?, progress: Int?, isLoading: Boolean?, canGoBack: Boolean?, canGoForward: Boolean?) -> Unit,
    onTrackerBlocked: () -> Unit,
    onDownloadStarted: (fileName: String, url: String, fileSize: Long, mimeType: String) -> Unit,
    onReaderContentExtracted: (ReaderContent) -> Unit,
    onFindMatchCountUpdated: (activeMatchOrdinal: Int, numberOfMatches: Int) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onPageLoadMetrics: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pageStartTime = remember(tab.id) { longArrayOf(0L) }

    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = javascriptEnabled
                domStorageEnabled = true
                databaseEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                allowFileAccess = false
                allowContentAccess = false
            }

            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                try {
                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(mimetype)
                        addRequestHeader("User-Agent", userAgent)
                        setDescription("Downloading file…")
                        setTitle(fileName)
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    }
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)

                    Toast.makeText(context, "Downloading $fileName", Toast.LENGTH_SHORT).show()
                    onDownloadStarted(fileName, url, contentLength, mimetype ?: "")
                } catch (e: Exception) {
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                onFindMatchCountUpdated(activeMatchOrdinal, numberOfMatches)
            }
        }
    }

    DisposableEffect(tab.id) {
        onWebViewCreated(webView)
        onDispose {
            webView.stopLoading()
        }
    }

    // Apply Speed optimizations dynamically
    LaunchedEffect(speedBoostState) {
        SpeedBooster.applySpeedOptimizations(webView, speedBoostState)
    }

    // Apply Desktop User Agent toggle
    LaunchedEffect(tab.isDesktopSite) {
        if (tab.isDesktopSite) {
            webView.settings.userAgentString = DESKTOP_USER_AGENT
        } else {
            webView.settings.userAgentString = null // Reset to default mobile
        }
        if (!tab.isHome && webView.url != null) {
            webView.reload()
        }
    }

    // Apply JavaScript toggle
    LaunchedEffect(javascriptEnabled) {
        webView.settings.javaScriptEnabled = javascriptEnabled
    }

    // Handle Find in Page
    LaunchedEffect(findInPageQuery, isFindInPageActive) {
        if (isFindInPageActive && findInPageQuery.isNotBlank()) {
            webView.findAllAsync(findInPageQuery)
        } else if (!isFindInPageActive) {
            webView.clearMatches()
        }
    }

    // Update clients
    LaunchedEffect(webView, adBlockerEnabled) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val requestUrl = request?.url?.toString() ?: return false
                if (requestUrl.startsWith("tel:") ||
                    requestUrl.startsWith("mailto:") ||
                    requestUrl.startsWith("sms:") ||
                    requestUrl.startsWith("geo:") ||
                    requestUrl.startsWith("intent:")
                ) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(requestUrl))
                        context.startActivity(intent)
                        return true
                    } catch (_: Exception) {
                        return true
                    }
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                pageStartTime[0] = System.currentTimeMillis()
                onNavigationStateChanged(
                    view?.title,
                    url,
                    15,
                    true,
                    view?.canGoBack(),
                    view?.canGoForward()
                )
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (pageStartTime[0] > 0) {
                    val duration = (System.currentTimeMillis() - pageStartTime[0]).coerceAtLeast(35)
                    onPageLoadMetrics(duration)
                }
                onNavigationStateChanged(
                    view?.title,
                    url,
                    100,
                    false,
                    view?.canGoBack(),
                    view?.canGoForward()
                )
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val reqUrl = request?.url?.toString()
                if (adBlockerEnabled && AdBlocker.isAdOrTracker(reqUrl)) {
                    onTrackerBlocked()
                    return AdBlocker.createEmptyResponse()
                }
                if (speedBoostState.isEnhancedSpeedEnabled && SpeedBooster.isBloatOrTelemetry(reqUrl)) {
                    onTrackerBlocked()
                    return AdBlocker.createEmptyResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onNavigationStateChanged(
                    view?.title,
                    view?.url,
                    newProgress,
                    newProgress < 100,
                    view?.canGoBack(),
                    view?.canGoForward()
                )
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank()) {
                    onNavigationStateChanged(
                        title,
                        view?.url,
                        null,
                        null,
                        view?.canGoBack(),
                        view?.canGoForward()
                    )
                }
            }
        }
    }

    // Load URL when tab url changes and not on home
    LaunchedEffect(tab.url) {
        if (!tab.isHome && tab.url.isNotBlank() && webView.url != tab.url) {
            webView.loadUrl(tab.url)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
            .fillMaxSize()
            .testTag("web_view_container")
    )
}

// Function to trigger reader mode extraction on a WebView
fun extractReaderModeContent(webView: WebView, onResult: (ReaderContent?) -> Unit) {
    webView.evaluateJavascript(UrlUtils.READER_EXTRACTION_SCRIPT) { rawResult ->
        try {
            if (rawResult == null || rawResult == "null") {
                onResult(null)
                return@evaluateJavascript
            }
            // Parse escaped json string from evaluateJavascript
            val unquoted = if (rawResult.startsWith("\"") && rawResult.endsWith("\"")) {
                org.json.JSONTokener(rawResult).nextValue().toString()
            } else {
                rawResult
            }
            val json = JSONObject(unquoted)
            val title = json.optString("title", "Article")
            val text = json.optString("text", "")
            if (text.isNotBlank()) {
                onResult(
                    ReaderContent(
                        title = title,
                        textContent = text,
                        url = webView.url ?: ""
                    )
                )
            } else {
                onResult(null)
            }
        } catch (_: Exception) {
            onResult(null)
        }
    }
}
