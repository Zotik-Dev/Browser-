package com.example.util

import android.net.Uri
import java.io.ByteArrayInputStream
import java.util.Locale

object AdBlocker {
    private val BLOCKED_HOSTS = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "google-analytics.com",
        "adservice.google.com",
        "adnxs.com",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "criteo.com",
        "moatads.com",
        "quantserve.com",
        "adroll.com",
        "advertising.com",
        "popads.net",
        "bidswitch.net",
        "smartadserver.com",
        "amazon-adsystem.com",
        "zedo.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "casale-media.com"
    )

    fun isAdOrTracker(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return false
        }
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false

        for (blocked in BLOCKED_HOSTS) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return true
            }
        }
        return false
    }

    fun createEmptyResponse(): android.webkit.WebResourceResponse {
        return android.webkit.WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
