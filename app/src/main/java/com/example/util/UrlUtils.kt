package com.example.util

import android.net.Uri
import android.util.Patterns
import com.example.model.SearchEngine
import com.example.model.SecurityState
import java.util.Locale
import java.util.regex.Pattern

object UrlUtils {

    private val WEB_URL_PATTERN: Pattern = Patterns.WEB_URL

    fun resolveInput(input: String, searchEngine: SearchEngine): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "apex://home"

        if (trimmed.equals("apex://home", ignoreCase = true) ||
            trimmed.equals("about:blank", ignoreCase = true)
        ) {
            return "apex://home"
        }

        // Already has scheme
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }

        // Check if looks like a domain name (e.g. google.com, en.wikipedia.org/wiki/Android, localhost:8080)
        val hasSpaces = trimmed.contains(" ")
        val hasDot = trimmed.contains(".") && !trimmed.endsWith(".")

        if (!hasSpaces && (hasDot || trimmed.startsWith("localhost", ignoreCase = true))) {
            return "https://$trimmed"
        }

        // Otherwise, perform search with selected search engine
        return searchEngine.buildSearchUrl(trimmed)
    }

    fun getDisplayHost(url: String): String {
        if (url == "apex://home" || url == "about:blank" || url.isBlank()) {
            return "Apex Home"
        }
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }

    fun getSecurityState(url: String): SecurityState {
        return when {
            url.startsWith("https://", ignoreCase = true) -> SecurityState.SECURE_HTTPS
            url.startsWith("http://", ignoreCase = true) -> SecurityState.INSECURE_HTTP
            else -> SecurityState.INTERNAL_HOME
        }
    }

    // JavaScript to extract article content for reader mode
    const val READER_EXTRACTION_SCRIPT = """
        (function() {
            try {
                var article = document.querySelector('article') || 
                              document.querySelector('[role="main"]') || 
                              document.querySelector('.post-content') || 
                              document.querySelector('.article-content') || 
                              document.body;
                
                var title = document.title || '';
                var h1 = document.querySelector('h1');
                if (h1 && h1.innerText) {
                    title = h1.innerText;
                }
                
                var paragraphs = article.getElementsByTagName('p');
                var textParts = [];
                for (var i = 0; i < paragraphs.length; i++) {
                    var pText = paragraphs[i].innerText.trim();
                    if (pText.length > 20) {
                        textParts.push(pText);
                    }
                }
                
                var content = textParts.join('\n\n');
                if (!content || content.length < 50) {
                    content = article.innerText;
                }
                
                return JSON.stringify({
                    title: title,
                    text: content
                });
            } catch(e) {
                return JSON.stringify({
                    title: document.title,
                    text: "Could not format reader view for this page."
                });
            }
        })();
    """
}
