package com.example.util

import android.content.Context
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.model.VpnServer
import com.example.model.VpnState
import java.util.concurrent.Executors

object VpnManager {

    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Applies the proxy override to all WebViews in the application.
     */
    fun applyVpnState(vpnState: VpnState, onComplete: () -> Unit = {}) {
        if (!vpnState.isConnected) {
            clearProxy(onComplete)
            return
        }

        if (vpnState.customProxyEnabled && vpnState.customProxyHost.isNotBlank()) {
            val rule = if (vpnState.customProxyType.equals("SOCKS5", ignoreCase = true)) {
                "socks://${vpnState.customProxyHost}:${vpnState.customProxyPort}"
            } else {
                "${vpnState.customProxyHost}:${vpnState.customProxyPort}"
            }
            setProxy(rule, onComplete)
        } else {
            // Apply selected VPN virtual server proxy
            val server = vpnState.selectedServer
            val rule = "${server.proxyHost}:${server.proxyPort}"
            setProxy(rule, onComplete)
        }
    }

    private fun setProxy(proxyRule: String, onComplete: () -> Unit) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                val proxyConfig = ProxyConfig.Builder()
                    .addProxyRule(proxyRule)
                    .addBypassRule("<local>")
                    .addBypassRule("apex://*")
                    .build()

                ProxyController.getInstance().setProxyOverride(
                    proxyConfig,
                    executor,
                    Runnable { onComplete() }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    /**
     * Clears proxy override, reverting WebView to direct connections.
     */
    fun clearProxy(onComplete: () -> Unit = {}) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                ProxyController.getInstance().clearProxyOverride(
                    executor,
                    Runnable { onComplete() }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    /**
     * Generates a virtual IP address based on the selected server country.
     */
    fun getVirtualIpForServer(server: VpnServer): String {
        return when (server.countryCode) {
            "US" -> "104.28.19.${(10..240).random()}"
            "GB" -> "185.156.46.${(10..240).random()}"
            "DE" -> "194.36.108.${(10..240).random()}"
            "NL" -> "149.202.88.${(10..240).random()}"
            "SG" -> "128.199.182.${(10..240).random()}"
            "JP" -> "133.130.120.${(10..240).random()}"
            "CA" -> "198.50.210.${(10..240).random()}"
            else -> "104.16.132.${(10..240).random()}"
        }
    }

    /**
     * Returns true if an error code or description indicates a network block, geo-restriction, or firewall block.
     */
    fun isNetworkBlockedError(errorCode: Int, description: String?): Boolean {
        val desc = description?.lowercase() ?: ""
        return errorCode == -2 // ERROR_HOST_LOOKUP
                || errorCode == -6 // ERROR_CONNECT
                || errorCode == -8 // ERROR_TIMEOUT
                || errorCode == -11 // ERROR_FAILED_SSL_HANDSHAKE
                || desc.contains("refused")
                || desc.contains("blocked")
                || desc.contains("reset")
                || desc.contains("firewall")
                || desc.contains("dns")
    }

    /**
     * Constructs a web proxy unblocker URL for a blocked webpage.
     */
    fun buildUnblockedUrl(originalUrl: String): String {
        if (originalUrl.startsWith("apex://")) return originalUrl
        val cleanUrl = if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            "https://$originalUrl"
        } else {
            originalUrl
        }
        // Route through reliable privacy reader/proxy gateway for circumvention
        return "https://r.jina.ai/$cleanUrl"
    }
}
