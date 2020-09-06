package forpdateam.ru.forpda.ui.fragments

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import forpdateam.ru.forpda.ui.views.ExtendedWebView

class WebViewTopScroller(
        private val webView: ExtendedWebView,
        private val appBarLayout: AppBarLayout
) : TabTopScroller {

    private var lastScrollY = 0
    private var scrolledToTop = false

    init {
        webView.setOnScrollListener { scrollX, scrollY, oldScrollX, oldScrollY ->
            Log.e("webosina", "setOnScrollListener $scrolledToTop, $lastScrollY, ${webView.scrollY}")
            if (scrolledToTop && webView.scrollY > 0) {
                resetState()
            }
        }
    }

    fun resetState() {
        lastScrollY = 0
        scrolledToTop = false
    }

    override fun toggleScrollTop() {
        if (lastScrollY > 0) {
            //appBarLayout.setExpanded(false, true);
            val scrollY = lastScrollY
            lastScrollY = 0
            scrolledToTop = false
            webView.scrollTo(webView.scrollX, scrollY)
        } else {
            appBarLayout.setExpanded(true, true)
            lastScrollY = webView.scrollY
            scrolledToTop = true
            webView.scrollTo(webView.scrollX, 0)
        }
    }
}