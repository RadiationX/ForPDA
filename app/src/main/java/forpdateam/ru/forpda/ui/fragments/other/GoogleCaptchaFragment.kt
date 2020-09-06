package forpdateam.ru.forpda.ui.fragments.other

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import java.util.regex.Pattern

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        webView = ExtendedWebView(context)
        webView.setDialogsHelper(DialogsHelper(
                webView.context,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler,
                App.get().Di().router
        ))
        attachWebView(webView)
        fragmentContent.addView(webView)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSubtitle("Это из-за VPN/Proxy и т.д.")
        webView.webViewClient = CaptchaWebViewClient()
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", content, "text/html", "utf-8", null)
    }

    internal inner class CaptchaWebViewClient : CustomWebViewClient() {
        override fun handleUri(uri: Uri): Boolean {
            Log.e("SUKA", uri.toString())
            if (Pattern.compile("https://4pda.ru/cdn-cgi/l/chk_captcha").matcher(uri.toString()).find()) {
                val nr = NetworkRequest.Builder().url(uri.toString()).withoutBody().build()
                val disposable = Observable.fromCallable { App.get().Di().webClient.request(nr) }
                        .onErrorReturn { NetworkResponse(null) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { this@GoogleCaptchaFragment.onResponse() }
                addToDisposable(disposable)
            }
            return true
        }
    }

    private fun onResponse() {
        Toast.makeText(App.getContext(), "Приложение будет перезапущено", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
            val activity = App.getActivity()
            if (activity == null) {
                Toast.makeText(App.getContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show()
            }
            MainActivity.restartApplication(activity)
        }, 1000)
    }
}
