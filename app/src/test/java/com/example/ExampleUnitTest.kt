package com.example

import com.example.data.model.BrowserTab
import com.example.viewmodel.BrowserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun urlFormatting_detectsDomainAndAddsHttps() {
        val result = BrowserViewModel.formatUrlOrQuery("wikipedia.org")
        assertEquals("https://wikipedia.org", result)
    }

    @Test
    fun urlFormatting_keepsExistingHttps() {
        val result = BrowserViewModel.formatUrlOrQuery("https://github.com")
        assertEquals("https://github.com", result)
    }

    @Test
    fun urlFormatting_convertsKeywordsToGoogleSearch() {
        val result = BrowserViewModel.formatUrlOrQuery("latest tech news")
        assertEquals("https://www.google.com/search?q=latest+tech+news", result)
    }

    @Test
    fun browserTab_homeDetection() {
        val homeTab = BrowserTab(url = "about:home")
        assertTrue(homeTab.isHome)
        assertEquals("Search or enter address", homeTab.displayHost)

        val webTab = BrowserTab(url = "https://www.google.com")
        assertFalse(webTab.isHome)
        assertEquals("google.com", webTab.displayHost)
        assertTrue(webTab.isSecure)
    }
}
