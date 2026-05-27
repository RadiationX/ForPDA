package forpdateam.ru.forpda.ui.fragments.news.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.lifecycle.lifecycleScope
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentPresenter
import forpdateam.ru.forpda.presentation.articles.detail.content.ArticleContentView
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import ru.radiationx.coretypes.ArticleAnswerId
import ru.radiationx.coretypes.ArticlePollId
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 03.09.17.
 */

class ArticleContentFragment : MvpAppCompatFragment(), ArticleContentView, TabTopScroller {

    private val webView: ExtendedWebView
        get() = requireView() as ExtendedWebView

    private lateinit var topScroller: WebViewTopScroller

    private val utils by inject<Utils>()
    private val linkHandler by inject<LinkHandler>()
    private val systemLinkHandler by inject<SystemLinkHandler>()
    private val router by inject<TabRouter>()
    private val webViewClient by inject<CustomWebViewClient>()
    private val presenter by quillMoxyPresenter<ArticleContentPresenter>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ExtendedWebView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (parentFragment as? NewsDetailsFragment)?.attachWebView(webView)
        topScroller =
            WebViewTopScroller(webView, (parentFragment as NewsDetailsFragment).getAppBar())
        webView.setDialogsHelper(
            DialogsHelper(
                context = webView.context,
                linkHandler = linkHandler,
                systemLinkHandler = systemLinkHandler,
                utils = utils,
                router = router,
            )
        )
        registerForContextMenu(webView)
        webView.webViewClient = webViewClient
        webView.webChromeClient = CustomWebChromeClient()
        webView.addJavascriptInterface(this, JS_INTERFACE)
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun setRefreshing(isRefreshing: Boolean) {}

    override fun showData(article: DetailsPage) {
        webView.loadDataWithBaseURL(
            "https://4pda.to/forum/",
            article.html,
            "text/html",
            "utf-8",
            null
        )
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    override fun setFontSize(size: Int) {
        webView.setRelativeFontSize(size)
    }

    @JavascriptInterface
    fun toComments() {
        viewLifecycleOwner.lifecycleScope.launch {
            (parentFragment as NewsDetailsFragment).fragmentsPager.currentItem = 1
        }
    }

    @JavascriptInterface
    fun sendPoll(id: String, answer: String, from: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val pollId = ArticlePollId(id.toInt())
            val answerIds = answer.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .map { ArticleAnswerId(it.toInt()) }
            presenter.sendPoll(from, pollId, answerIds)
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
