package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.SearchEngine
import com.example.model.SecurityState
import com.example.util.AdBlocker
import com.example.util.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Apex Browser", appName)
  }

  @Test
  fun testUrlResolution() {
    val resolvedDomain = UrlUtils.resolveInput("wikipedia.org", SearchEngine.GOOGLE)
    assertEquals("https://wikipedia.org", resolvedDomain)

    val resolvedFullUrl = UrlUtils.resolveInput("https://example.com/page", SearchEngine.GOOGLE)
    assertEquals("https://example.com/page", resolvedFullUrl)

    val resolvedSearch = UrlUtils.resolveInput("kotlin flow tutorial", SearchEngine.GOOGLE)
    assertTrue(resolvedSearch.contains("google.com/search?q=kotlin+flow+tutorial"))

    val resolvedHome = UrlUtils.resolveInput("apex://home", SearchEngine.GOOGLE)
    assertEquals("apex://home", resolvedHome)
  }

  @Test
  fun testSecurityState() {
    assertEquals(SecurityState.SECURE_HTTPS, UrlUtils.getSecurityState("https://github.com"))
    assertEquals(SecurityState.INSECURE_HTTP, UrlUtils.getSecurityState("http://insecure.test"))
    assertEquals(SecurityState.INTERNAL_HOME, UrlUtils.getSecurityState("apex://home"))
  }

  @Test
  fun testAdBlocker() {
    assertTrue(AdBlocker.isAdOrTracker("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
    assertTrue(AdBlocker.isAdOrTracker("https://stats.g.doubleclick.net/dc.js"))
    assertFalse(AdBlocker.isAdOrTracker("https://en.wikipedia.org/wiki/Android"))
  }

  @Test
  fun testTabManagementModel() {
    val tab1 = com.example.model.BrowserTab(url = "https://google.com", title = "Google")
    val tab2 = com.example.model.BrowserTab(url = "https://android.com", title = "Android")
    val tabList = mutableListOf(tab1, tab2)

    assertEquals(2, tabList.size)
    assertEquals(tab1.id, tabList[0].id)
    assertEquals("Google", tabList[0].title)

    // Simulate switching active tab
    var activeTabId = tab1.id
    assertEquals(tab1.id, activeTabId)
    activeTabId = tab2.id
    assertEquals(tab2.id, activeTabId)

    // Simulate closing active tab
    tabList.removeIf { it.id == tab2.id }
    assertEquals(1, tabList.size)
    assertEquals(tab1.id, tabList.first().id)
  }

  @Test
  fun testVpnStateAndServerDefaults() {
    val state = com.example.model.VpnState()
    assertFalse(state.isConnected)
    assertFalse(state.isConnecting)
    assertEquals("US", state.selectedServer.countryCode)
    assertEquals(com.example.model.DEFAULT_VPN_SERVERS.size, 8)
    assertEquals(com.example.model.DEFAULT_DNS_PROVIDERS.size, 4)
  }

  @Test
  fun testVpnManagerUnblockedUrl() {
    val unblocked = com.example.util.VpnManager.buildUnblockedUrl("https://restricted-news.com/article")
    assertEquals("https://r.jina.ai/https://restricted-news.com/article", unblocked)

    val homeUrl = com.example.util.VpnManager.buildUnblockedUrl("apex://home")
    assertEquals("apex://home", homeUrl)
  }

  @Test
  fun testVpnManagerNetworkBlockedErrorDetection() {
    assertTrue(com.example.util.VpnManager.isNetworkBlockedError(-2, "Host lookup failed"))
    assertTrue(com.example.util.VpnManager.isNetworkBlockedError(-6, "Connection refused"))
    assertTrue(com.example.util.VpnManager.isNetworkBlockedError(-8, "Connection timed out"))
    assertTrue(com.example.util.VpnManager.isNetworkBlockedError(0, "Site blocked by firewall policy"))
    assertFalse(com.example.util.VpnManager.isNetworkBlockedError(0, "Normal response"))
  }

  @Test
  fun testVpnVirtualIpGeneration() {
    val usServer = com.example.model.DEFAULT_VPN_SERVERS.first { it.countryCode == "US" }
    val ip = com.example.util.VpnManager.getVirtualIpForServer(usServer)
    assertTrue(ip.startsWith("104.28.19."))

    val gbServer = com.example.model.DEFAULT_VPN_SERVERS.first { it.countryCode == "GB" }
    val gbIp = com.example.util.VpnManager.getVirtualIpForServer(gbServer)
    assertTrue(gbIp.startsWith("185.156.46."))
  }
}
