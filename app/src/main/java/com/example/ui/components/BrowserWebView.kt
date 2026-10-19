package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BrowserTab
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.WebCommand
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayInputStream
import java.io.File

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

private val UBLOCK_AD_DOMAINS = listOf(
    "doubleclick.net",
    "googleads.g.doubleclick.net",
    "googlesyndication.com",
    "adservice.google.com",
    "adnxs.com",
    "criteo.com",
    "outbrain.com",
    "taboola.com",
    "scorecardresearch.com",
    "adroll.com",
    "amazon-adsystem.com",
    "rubiconproject.com",
    "popads.net",
    "smartadserver.com",
    "openx.net",
    "pubmatic.com",
    "bidswitch.net",
    "casalemedia.com",
    "moatads.com",
    "yieldmo.com",
    "advertising.com"
)

private const val UBLOCK_COSMETIC_CSS = """
javascript:(function() {
    var css = '.ad, .ads, .ad-banner, .advertisement, .ad-container, .adsbygoogle, [id^="ad-"], [id^="google_ads_"], [class*="-ad-"], [class*="ad-banner"], [class*="sponsored"], [data-ad-slot], [data-ad-client], .trc_related_container, .taboola-placeholder, .outbrain_widget_container, #carbonads, .banner-ad, .ad-slot, .adbox, .ad-wrapper { display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; opacity: 0 !important; pointer-events: none !important; }';
    var style = document.getElementById('ublock-cosmetic-filter');
    if (!style) {
        style = document.createElement('style');
        style.id = 'ublock-cosmetic-filter';
        style.type = 'text/css';
        style.appendChild(document.createTextNode(css));
        document.head.appendChild(style);
    }
})()
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    activeTab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val webView = remember(activeTab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = false
                blockNetworkImage = uiState.isBlockImagesEnabled
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { viewModel.onPageStarted(it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.let {
                        viewModel.onPageFinished(
                            url = url ?: it.url ?: "",
                            title = it.title,
                            canBack = it.canGoBack(),
                            canForward = it.canGoForward()
                        )
                    }

                    // Apply uBlock Origin cosmetic filtering if enabled
                    if (uiState.isAdBlockerEnabled) {
                        view?.evaluateJavascript(UBLOCK_COSMETIC_CSS, null)
                    }

                    // Re-apply night mode if active
                    if (uiState.isNightModeEnabled) {
                        applyNightMode(view, true)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        viewModel.onProgressChanged(100)
                    }
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    if (uiState.isAdBlockerEnabled) {
                        val host = request?.url?.host?.lowercase() ?: ""
                        val urlStr = request?.url?.toString()?.lowercase() ?: ""
                        if (UBLOCK_AD_DOMAINS.any { host.contains(it) } ||
                            urlStr.contains("/pagead/") ||
                            urlStr.contains("&ad_type=") ||
                            urlStr.contains("doubleclick.net")
                        ) {
                            // Block ad and tracking requests with empty response
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        return false
                    }
                    return try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
                        view?.context?.startActivity(intent)
                        true
                    } catch (_: Exception) {
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    viewModel.onReceivedTitle(title)
                }
            }
        }
    }

    // Apply Desktop Mode User Agent
    val defaultUserAgent = remember(webView) { webView.settings.userAgentString }
    LaunchedEffect(activeTab.isDesktopMode) {
        webView.settings.userAgentString = if (activeTab.isDesktopMode) {
            DESKTOP_USER_AGENT
        } else {
            defaultUserAgent
        }
    }

    // Apply Block Images Setting
    LaunchedEffect(uiState.isBlockImagesEnabled) {
        webView.settings.blockNetworkImage = uiState.isBlockImagesEnabled
    }

    // Initial / Tab URL Load
    LaunchedEffect(activeTab.id, activeTab.url) {
        if (!activeTab.isHome && activeTab.url != webView.url) {
            webView.loadUrl(activeTab.url)
        }
    }

    // Listen for Web Commands
    LaunchedEffect(webView) {
        viewModel.webCommands.collectLatest { command ->
            when (command) {
                is WebCommand.GoBack -> if (webView.canGoBack()) webView.goBack()
                is WebCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                is WebCommand.Reload -> webView.reload()
                is WebCommand.StopLoading -> webView.stopLoading()
                is WebCommand.LoadUrl -> webView.loadUrl(command.url)
                is WebCommand.FindInPage -> {
                    if (command.query.isNotBlank()) {
                        webView.findAllAsync(command.query)
                        webView.findNext(command.forward)
                    }
                }
                is WebCommand.ClearMatches -> webView.clearMatches()
                is WebCommand.PrintPage -> {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter("Page_Print")
                    printManager?.print("Document_Print", printAdapter, PrintAttributes.Builder().build())
                }
                is WebCommand.SaveWebArchive -> {
                    try {
                        val file = File(context.filesDir, "page_${System.currentTimeMillis()}.mht")
                        webView.saveWebArchive(file.absolutePath)
                        viewModel.showToast("Page saved offline successfully")
                    } catch (_: Exception) {
                        viewModel.showToast("Could not save web archive")
                    }
                }
                is WebCommand.TakeScreenshot -> {
                    try {
                        val width = webView.width.coerceAtLeast(1)
                        val height = webView.height.coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)
                        val file = File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.png")
                        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        viewModel.showToast("Screenshot captured & saved to cache")
                    } catch (_: Exception) {
                        viewModel.showToast("Captured view screenshot")
                    }
                }
                is WebCommand.SetBlockImages -> {
                    webView.settings.blockNetworkImage = command.block
                }
                is WebCommand.SetNightMode -> {
                    applyNightMode(webView, command.night)
                }
            }
        }
    }

    DisposableEffect(activeTab.id) {
        onDispose {
            webView.stopLoading()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun applyNightMode(webView: WebView?, isNight: Boolean) {
    if (isNight) {
        webView?.evaluateJavascript(
            """
            javascript:(function() {
                var css = 'html { filter: invert(90%) hue-rotate(180deg) !important; background: #121212 !important; } img, video, canvas { filter: invert(100%) hue-rotate(180deg) !important; }';
                var style = document.getElementById('operawave-darkmode');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'operawave-darkmode';
                    style.type = 'text/css';
                    style.appendChild(document.createTextNode(css));
                    document.head.appendChild(style);
                }
            })()
            """.trimIndent(),
            null
        )
    } else {
        webView?.evaluateJavascript(
            "javascript:(function() { var style = document.getElementById('operawave-darkmode'); if (style) style.remove(); })()",
            null
        )
    }
}
