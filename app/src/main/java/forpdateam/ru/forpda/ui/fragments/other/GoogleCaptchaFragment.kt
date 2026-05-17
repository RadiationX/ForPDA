package forpdateam.ru.forpda.ui.fragments.other

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

/**
 * Created by radiationx on 09.11.17.
 */

class GoogleCaptchaFragment : TabFragment() {
    private lateinit var webView: ExtendedWebView
    private var content = ""

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
                webView.context,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler,
                App.get().Di().utils,
                App.get().Di().router
            )
        )
        attachWebView(webView)
        fragmentContent.addView(webView)

        setSubtitle("Это из-за VPN/Proxy и т.д.")
        webView.webViewClient = CaptchaWebViewClient()
        webView.loadDataWithBaseURL("https://4pda.to/forum/", content, "text/html", "utf-8", null)
    }

    internal inner class CaptchaWebViewClient : CustomWebViewClient() {
        override fun handleUri(uri: Uri): Boolean {
            Log.e("SUKA", uri.toString())
            if (Pattern.compile("https://4pda.to/cdn-cgi/l/chk_captcha").matcher(uri.toString())
                    .find()
            ) {
                runBlocking {
                    runCatching {
                        val nr = NetworkRequest.Builder().url(uri.toString()).withoutBody().build()
                        App.get().Di().webClient.request(nr)
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
