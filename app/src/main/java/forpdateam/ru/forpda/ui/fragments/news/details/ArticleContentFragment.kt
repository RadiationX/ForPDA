package forpdateam.ru.forpda.ui.fragments.news.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface

import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentPresenter
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentView
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.views.ExtendedWebView

/**
 * Created by radiationx on 03.09.17.
 */

class ArticleContentFragment : MvpAppCompatFragment(), ArticleContentView, TabTopScroller {

    private lateinit var webView: ExtendedWebView
    private lateinit var topScroller: WebViewTopScroller

    @InjectPresenter
    lateinit var presenter: ArticleContentPresenter

    @ProvidePresenter
    fun providePresenter(): ArticleContentPresenter = ArticleContentPresenter(
            (parentFragment as NewsDetailsFragment).provideChildInteractor(),
            App.get().Di().mainPreferencesHolder,
            App.get().Di().templateManager,
            App.get().Di().errorHandler
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        webView = ExtendedWebView(context)
        (parentFragment as? NewsDetailsFragment)?.attachWebView(webView)
        topScroller = WebViewTopScroller(webView, (parentFragment as NewsDetailsFragment).getAppBar())
        webView.setDialogsHelper(DialogsHelper(
                webView.context,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler,
                App.get().Di().router
        ))
        registerForContextMenu(webView)
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = CustomWebChromeClient()
        webView.addJavascriptInterface(this, JS_INTERFACE)
        return webView
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun setRefreshing(isRefreshing: Boolean) {}

    override fun showData(article: DetailsPage) {
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.html, "text/html", "utf-8", null)
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    override fun setFontSize(size: Int) {
        webView.setRelativeFontSize(size)
    }

    @JavascriptInterface
    fun toComments() {
        if (context == null)
            return
        webView.runInUiThread { (parentFragment as NewsDetailsFragment).fragmentsPager.currentItem = 1 }
    }

    @JavascriptInterface
    fun sendPoll(id: String, answer: String, from: String) {
        if (context == null)
            return
        webView.runInUiThread {
            val pollId = Integer.parseInt(id)
            val answers = answer.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val answersId = IntArray(answers.size)
            for (i in answers.indices) {
                answersId[i] = Integer.parseInt(answers[i])
            }
            presenter.sendPoll(from, pollId, answersId)
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.endWork()
    }

    companion object {
        const val JS_INTERFACE = "INews"
    }
}
