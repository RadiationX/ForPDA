package forpdateam.ru.forpda.ui.fragments.other

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import java.util.ArrayList

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.presentation.announce.AnnouncePresenter
import forpdateam.ru.forpda.presentation.announce.AnnounceView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.views.ExtendedWebView

/**
 * Created by radiationx on 16.10.17.
 */

class AnnounceFragment : TabFragment(), AnnounceView, TabTopScroller {

    private var searchViewTag = 0
    private lateinit var webView: ExtendedWebView
    private lateinit var topScroller: WebViewTopScroller

    @InjectPresenter
    lateinit var presenter: AnnouncePresenter

    @ProvidePresenter
    fun providePresenter(): AnnouncePresenter = AnnouncePresenter(
            App.get().Di().forumRepository,
            App.get().Di().announceTemplate,
            App.get().Di().templateManager,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = "Объявление"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.id = getInt(ARG_ANNOUNCE_ID)
            presenter.forumId = getInt(ARG_FORUM_ID)
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

    @SuppressLint("JavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.addJavascriptInterface(this, JS_INTERFACE)
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = CustomWebChromeClient()
        webView.setJsLifeCycleListener(object : ExtendedWebView.JsLifeCycleListener {
            override fun onDomContentComplete(actions: ArrayList<String>) {
                setRefreshing(false)
            }

            override fun onPageComplete(actions: ArrayList<String>) {

            }
        })
        topScroller = WebViewTopScroller(webView, appBarLayout)
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        addSearchOnPageItem(menu)
    }

    override fun showData(data: Announce) {
        setTitle(data.title)
        webView.loadDataWithBaseURL("https://4pda.to/forum/", data.html, "text/html", "utf-8", null)
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    private fun addSearchOnPageItem(menu: Menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu)
        val searchOnPageMenuItem = menu.findItem(R.id.action_search)
        searchOnPageMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        val searchView = searchOnPageMenuItem.actionView as SearchView
        searchView.tag = searchViewTag

        searchView.setOnSearchClickListener { _ ->
            if (searchView.tag == searchViewTag) {
                val searchClose = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView?
                if (searchClose != null)
                    (searchClose.parent as ViewGroup).removeView(searchClose)

                val navButtonsParams = ViewGroup.LayoutParams(App.px48, App.px48)
                val outValue = TypedValue()
                context?.theme?.resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true)

                val btnNext = AppCompatImageButton(searchView.context)
                btnNext.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_toolbar_search_next))
                btnNext.setBackgroundResource(outValue.resourceId)

                val btnPrev = AppCompatImageButton(searchView.context)
                btnPrev.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_toolbar_search_prev))
                btnPrev.setBackgroundResource(outValue.resourceId)

                (searchView.getChildAt(0) as LinearLayout).addView(btnPrev, navButtonsParams)
                (searchView.getChildAt(0) as LinearLayout).addView(btnNext, navButtonsParams)

                btnNext.setOnClickListener { findNext(true) }
                btnPrev.setOnClickListener { findNext(false) }
                searchViewTag++
            }
        }

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        searchView.setIconifiedByDefault(true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                findText(newText)
                return false
            }
        })
    }

    private fun findNext(next: Boolean) {
        webView.findNext(next)
    }

    private fun findText(text: String) {
        webView.findAllAsync(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.endWork()
    }

    companion object {
        const val ARG_ANNOUNCE_ID = "ARG_ANNOUNCE_ID"
        const val ARG_FORUM_ID = "ARG_FORUM_ID"
        const val JS_INTERFACE = "IAnnounce"
    }
}
