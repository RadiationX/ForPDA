package forpdateam.ru.forpda.ui.fragments.other

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.extensions.getDrawableResAttr
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.ISystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.announce.AnnouncePresenter
import forpdateam.ru.forpda.presentation.announce.AnnounceView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 16.10.17.
 */

class AnnounceFragment : TabFragment(), AnnounceView, TabTopScroller {

    private var searchViewTag = 0
    private lateinit var webView: ExtendedWebView
    private lateinit var topScroller: WebViewTopScroller

    private val utils by inject<Utils>()
    private val linkHandler by inject<ILinkHandler>()
    private val systemLinkHandler by inject<ISystemLinkHandler>()
    private val router by inject<TabRouter>()
    private val webViewClient by inject<CustomWebViewClient>()
    private val presenter by quillMoxyPresenter<AnnouncePresenter>()

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

    @SuppressLint("JavascriptInterface")
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

        webView.addJavascriptInterface(this, JS_INTERFACE)
        webView.webViewClient = webViewClient
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
                val searchClose =
                    searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView?
                if (searchClose != null)
                    (searchClose.parent as ViewGroup).removeView(searchClose)

                val navButtonsParams = ViewGroup.LayoutParams(
                    searchView.context.getDimenPx(R.dimen.dp48),
                    searchView.context.getDimenPx(R.dimen.dp48)
                )

                val backgroundRes = requireContext().getDrawableResAttr(android.R.attr.actionBarItemBackground)

                val btnNext = AppCompatImageButton(searchView.context)
                btnNext.setImageResource(R.drawable.ic_toolbar_search_next)
                btnNext.setBackgroundResource(backgroundRes)

                val btnPrev = AppCompatImageButton(searchView.context)
                btnPrev.setImageResource(R.drawable.ic_toolbar_search_prev)
                btnPrev.setBackgroundResource(backgroundRes)

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

    override fun onDestroyView() {
        super.onDestroyView()
        webView.endWork()
    }

    companion object {
        const val ARG_ANNOUNCE_ID = "ARG_ANNOUNCE_ID"
        const val ARG_FORUM_ID = "ARG_FORUM_ID"
        const val JS_INTERFACE = "IAnnounce"
    }
}
