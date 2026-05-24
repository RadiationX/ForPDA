package forpdateam.ru.forpda.ui.fragments.other

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.common.CaptchaParser
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 09.11.17.
 */

class GoogleCaptchaFragment : TabFragment() {

    companion object {
        fun newInstance() = GoogleCaptchaFragment()
    }

    private lateinit var webView: ExtendedWebView
    private var content = ""

    private val utils by inject<Utils>()
    private val linkHandler by inject<LinkHandler>()
    private val systemLinkHandler by inject<SystemLinkHandler>()
    private val router by inject<TabRouter>()
    private val webClient by inject<WebClient>()
    private val captchaParser by inject<CaptchaParser>()

    init {
        configuration.defaultTitle = "Проверка"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            content = getString("content", "1")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = ExtendedWebView(requireContext())
        webView.setDialogsHelper(
            DialogsHelper(
                context = webView.context,
                linkHandler = linkHandler,
                systemLinkHandler = systemLinkHandler,
                utils = utils,
                router = router
            )
        )
        attachWebView(webView)
        fragmentContent.addView(webView)

        setSubtitle("Это из-за VPN/Proxy и т.д.")
        webView.webViewClient = CaptchaWebViewClient()
        webView.loadDataWithBaseURL("https://4pda.to/forum/", content, "text/html", "utf-8", null)
    }

    internal inner class CaptchaWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return handleUri(request.url)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleUri(url.toUri())
        }

        private fun handleUri(uri: Uri): Boolean {
            Log.e("SUKA", uri.toString())
            if (captchaParser.checkRedirect(uri.toString())) {
                runBlocking {
                    runCatching {
                        val nr = NetworkRequest.Builder().url(uri.toString()).withoutBody().build()
                        webClient.request(nr)
                    }
                    withContext(Dispatchers.Main) {
                        this@GoogleCaptchaFragment.onResponse()
                    }
                }
            }
            return true
        }
    }

    private fun onResponse() {
        viewLifecycleOwner.lifecycleScope.launch {
            val activity = requireActivity()
            Toast.makeText(activity, "Приложение будет перезапущено", Toast.LENGTH_SHORT).show()
            delay(1000)
            MainActivity.restartApplication(activity)
        }
    }
}
