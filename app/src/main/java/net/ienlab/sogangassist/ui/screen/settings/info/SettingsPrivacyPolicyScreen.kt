package net.ienlab.sogangassist.ui.screen.settings.info

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import net.ienlab.sogangassist.R

@Composable
fun SettingsPrivacyPolicyScreen(
    modifier: Modifier = Modifier
) {
    val url = stringResource(id = R.string.url_privacy_policy)
    Column {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                settings.let {
                    it.loadWithOverviewMode = true
                    it.useWideViewPort = true
                    it.setSupportZoom(true)
                    it.builtInZoomControls = false
                    it.javaScriptEnabled = true
                    it.javaScriptCanOpenWindowsAutomatically = true
                    it.domStorageEnabled = true
                }
                }
            },
            update = { webView ->
                webView.loadUrl(url)
            },
            modifier = modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}