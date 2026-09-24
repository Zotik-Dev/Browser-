package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.model.SpeedBoostState

object SpeedBooster {

    // Bloat domains, tracking scripts, and heavy analytics that slow down page loads by seconds
    private val BLOAT_DOMAINS = listOf(
        "google-analytics.com",
        "googletagmanager.com",
        "hotjar.com",
        "segment.io",
        "segment.com",
        "optimizely.com",
        "newrelic.com",
        "clarity.ms",
        "crazyegg.com",
        "fullstory.com",
        "scorecardresearch.com",
        "chartbeat.com",
        "quantserve.com",
        "mixpanel.com",
        "mouseflow.com",
        "heap.io",
        "branch.io",
        "appsflyer.com"
    )

    /**
     * Checks if a resource URL is a known heavy tracking/telemetry beacon that slows page rendering.
     */
    fun isBloatOrTelemetry(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val host = try {
            Uri.parse(url).host?.lowercase() ?: ""
        } catch (_: Exception) {
            return false
        }
        return BLOAT_DOMAINS.any { host.contains(it) }
    }

    /**
     * Applies turbo speed and hardware acceleration optimizations to the WebView.
     */
    fun applySpeedOptimizations(
        webView: WebView,
        speedState: SpeedBoostState
    ) {
        webView.settings.apply {
            // Turbo rendering priority
            if (speedState.isEnhancedSpeedEnabled) {
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                setEnableSmoothTransition(true)
            }

            // Aggressive cache mode for instant repeat loads
            cacheMode = when {
                !speedState.isEnhancedSpeedEnabled -> WebSettings.LOAD_DEFAULT
                speedState.isAggressiveCacheEnabled -> WebSettings.LOAD_DEFAULT
                else -> WebSettings.LOAD_NORMAL
            }

            // Storage and worker threads
            domStorageEnabled = true
            databaseEnabled = true

            // Data saver / image loading
            if (speedState.isEnhancedSpeedEnabled && speedState.isDataSaverEnabled) {
                blockNetworkImage = true
            } else {
                blockNetworkImage = false
            }
            loadsImagesAutomatically = true

            // Fast viewport layout
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Hardware acceleration
        if (speedState.isHardwareAccelerationEnabled) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    /**
     * Formats page load latency nicely (e.g. "⚡ 240ms" or "⚡ 1.2s").
     */
    fun formatLoadTime(ms: Long): String {
        if (ms <= 0) return "Fast"
        return if (ms < 1000) {
            "${ms}ms"
        } else {
            String.format("%.1fs", ms / 1000.0)
        }
    }

    /**
     * Formats data saved nicely.
     */
    fun formatDataSaved(kb: Long): String {
        return if (kb < 1024) {
            "${kb} KB"
        } else {
            String.format("%.1f MB", kb / 1024.0)
        }
    }

    /**
     * Formats time saved nicely.
     */
    fun formatTimeSaved(ms: Long): String {
        val seconds = ms / 1000.0
        return if (seconds < 60) {
            String.format("%.1fs", seconds)
        } else {
            String.format("%.1fm", seconds / 60.0)
        }
    }
}
