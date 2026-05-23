package com.example.motivationcalendarapi.ui.exercise.technique

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeShortsWebView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(videoId) {
        "https://www.youtube.com/embed/$videoId" +
                "?autoplay=0" +
                "&playsinline=1" +
                "&rel=0" +
                "&modestbranding=1" +
                "&iv_load_policy=3" +
                "&fs=1" +
                "&enablejsapi=1" +
                "&origin=https://trainify.app"
    }

    var webViewRef: WebView? = null

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                setBackgroundColor(Color.BLACK)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.loadsImagesAutomatically = true
                settings.mediaPlaybackRequiresUserGesture = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.userAgentString = settings.userAgentString + " TrainifyAndroidWebView"

                webViewClient = LockedYoutubeWebViewClient(videoId)
                webChromeClient = FullscreenWebChromeClient(context, this)
                loadUrlWithReferer(embedUrl)
            }
        },
        update = { webView ->
            webViewRef = webView
            if (webView.url != embedUrl) {
                webView.loadUrlWithReferer(embedUrl)
            }
        }
    )

    DisposableEffect(videoId) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
            }
        }
    }
}

private class LockedYoutubeWebViewClient(
    private val videoId: String
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return true
        return shouldBlockNavigation(uri)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val uri = url?.let(Uri::parse) ?: return true
        return shouldBlockNavigation(uri)
    }

    private fun shouldBlockNavigation(uri: Uri): Boolean {
        val host = uri.host.orEmpty()
        val path = uri.path.orEmpty()
        val videoFromQuery = uri.getQueryParameter("v")

        if (uri.scheme == "about") return false

        val isYoutubeHost = host == "www.youtube.com" ||
                host == "youtube.com" ||
                host == "m.youtube.com" ||
                host == "www.youtube-nocookie.com" ||
                host == "youtube-nocookie.com"

        if (!isYoutubeHost) return true

        val isAllowedEmbed = path == "/embed/$videoId" ||
                path == "/embed/$videoId/" ||
                path.startsWith("/embed/") && path.contains(videoId)

        val isAllowedPlayerResource = path.startsWith("/s/") ||
                path.startsWith("/iframe_api") ||
                path.startsWith("/youtubei/") ||
                path.startsWith("/api/") ||
                path.startsWith("/get_video_info")

        val isSameVideoWatch = (path == "/watch" && videoFromQuery == videoId)

        return !(isAllowedEmbed || isAllowedPlayerResource || isSameVideoWatch)
    }
}

private class FullscreenWebChromeClient(
    context: Context,
    private val playerWebView: WebView
) : WebChromeClient() {
    private val activity = context.findActivity()
    private var fullscreenDialog: Dialog? = null
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var previousOrientation: Int? = null

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        val currentActivity = activity ?: return
        if (view == null) {
            callback?.onCustomViewHidden()
            return
        }
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback
        previousOrientation = currentActivity.requestedOrientation
        currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        fullscreenDialog = Dialog(currentActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            window?.apply {
                setBackgroundDrawableResource(android.R.color.black)
                addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
            setOnDismissListener { onHideCustomView() }
            show()
        }

        playerWebView.postDelayed({
            playerWebView.playCurrentYoutubeVideo()
        }, 250L)
        playerWebView.postDelayed({
            playerWebView.playCurrentYoutubeVideo()
        }, 900L)
    }

    override fun onHideCustomView() {
        val dialog = fullscreenDialog
        val view = customView

        fullscreenDialog = null
        customView = null

        if (view?.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
        }

        dialog?.setOnDismissListener(null)
        dialog?.dismiss()

        customViewCallback?.onCustomViewHidden()
        customViewCallback = null

        activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        previousOrientation = null
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: android.os.Message?
    ): Boolean {
        return false
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun WebView.playCurrentYoutubeVideo() {
    evaluateJavascript(
        """
        (function() {
            try {
                var iframe = document.querySelector('iframe');
                var command = JSON.stringify({event:'command', func:'playVideo', args:[]});
                window.postMessage(command, '*');
                if (iframe && iframe.contentWindow) {
                    iframe.contentWindow.postMessage(command, '*');
                }
                var video = document.querySelector('video');
                if (video) { video.play(); }
            } catch (e) {}
        })();
        """.trimIndent(),
        null
    )
}

private fun WebView.loadUrlWithReferer(url: String) {
    loadUrl(
        url,
        mapOf(
            "Referer" to "https://trainify.app/",
            "Origin" to "https://trainify.app"
        )
    )
}
