package forpdateam.ru.forpda.ui.fragments.theme

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.presentation.theme.ThemeJsInterface
import forpdateam.ru.forpda.presentation.theme.ThemePresenter
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import java.util.*
import java.util.regex.Pattern

/**
 * Created by radiationx on 20.10.16.
 */

class ThemeFragmentWeb : ThemeFragment(), ExtendedWebView.JsLifeCycleListener, TabTopScroller {
    private lateinit var webView: ExtendedWebView
    private lateinit var webViewClient: WebViewClient
    private lateinit var chromeClient: WebChromeClient
    private lateinit var jsInterface: ThemeJsInterface
    private lateinit var topScroller: WebViewTopScroller

    override fun scrollToAnchor(anchor: String?) {
        webView.evalJs("scrollToElement(\"$anchor\")")
        topScroller.resetState()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        jsInterface = ThemeJsInterface(presenter)
        messagePanel.setHeightChangeListener { newHeight ->
            webView.paddingBottom = newHeight
        }

        webViewClient = ThemeWebViewClient()
        chromeClient = ThemeChromeClient()

        webView = ExtendedWebView(context)
        webView.setDialogsHelper(DialogsHelper(
                webView.context,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler,
                App.get().Di().router
        ))
        attachWebView(webView)
        webView.setJsLifeCycleListener(this)
        refreshLayout.addView(webView)
        refreshLayoutLongTrigger(refreshLayout)
        webView.addJavascriptInterface(this, "IThemeView")
        webView.addJavascriptInterface(jsInterface, JS_INTERFACE)
        webView.webViewClient = webViewClient
        webView.webChromeClient = chromeClient
        registerForContextMenu(webView)
        fab.setOnClickListener {
            if (webView.direction == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true)
            } else if (webView.direction == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true)
            }
        }
        webView.setOnDirectionListener { direction ->
            if (direction == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getVecDrawable(fab.context, R.drawable.ic_arrow_down))
            } else if (direction == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getVecDrawable(fab.context, R.drawable.ic_arrow_up))
            }
        }
        topScroller = WebViewTopScroller(webView, appBarLayout)

        //Кастомизация менюхи при выделении текста
        webView.setActionModeListener(object : ExtendedWebView.OnStartActionModeListener {
            override fun onCreate(actionMode: ActionMode, callback: ActionMode.Callback) {
                val menu = actionMode.menu
                val items = ArrayList<MenuItem>()
                for (i in 0 until menu.size()) {
                    items.add(menu.getItem(i))
                }
                menu.clear()

                menu.add(0, R.id.action_mode_item_copy, 0, R.string.copy)
                        .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_content_copy))
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

                if (!authHolder.get().isAuth() || presenter.canQuote()) {
                    menu.add(0, R.id.action_mode_item_quote, 0, R.string.quote)
                            .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_quote_post))
                            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }

                menu.add(0, R.id.action_mode_item_select_all, 0, R.string.all_text)
                        .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_select_all))
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

                menu.add(0, R.id.action_mode_item_share, 0, R.string.share)
                        .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_share))
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

                for (item in items) {
                    Log.e("ExtendedWebView", "fillItem " + item.itemId + " : " + item.title + " : " + item.titleCondensed + " : " + item.intent + " : " + item.menuInfo)
                    if (item.intent != null) {
                        menu.add(item.groupId, item.itemId, item.order, item.title)
                                .setIntent(item.intent)
                                .setNumericShortcut(item.numericShortcut).alphabeticShortcut = item.alphabeticShortcut
                    }
                }
            }

            override fun onClick(actionMode: ActionMode, item: MenuItem): Boolean {
                Log.e("ExtendedWebView", "onClick " + item.itemId)
                var result = false
                when (item.itemId) {
                    R.id.action_mode_item_copy -> {
                        webView.evalJs("copySelectedText()")
                        result = true
                    }
                    R.id.action_mode_item_quote -> {
                        webView.evalJs("selectionToQuote()")
                        result = true
                    }
                    R.id.action_mode_item_select_all -> {
                        webView.evalJs("selectAllPostText()")
                        result = true
                    }
                    R.id.action_mode_item_share -> {
                        webView.evalJs("shareSelectedText()")
                        result = true
                    }
                }
                return result
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun findNext(next: Boolean) {
        webView.findNext(next)
    }

    override fun findText(text: String) {
        webView.findAllAsync(text)
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    override fun updateView(page: ThemePage) {
        super.updateView(page)
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", page.html, "text/html", "utf-8", null)
        webView.updatePaddingBottom()
    }

    override fun updateShowAvatarState(isShow: Boolean) {
        webView.evalJs("updateShowAvatarState($isShow)")
    }

    override fun updateTypeAvatarState(isCircle: Boolean) {
        webView.evalJs("updateTypeAvatarState($isCircle)")
    }

    override fun updateScrollButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.GONE
        }
    }

    override fun setFontSize(size: Int) {
        Log.e("kokosina", "setFontSize $size")
        webView.setRelativeFontSize(size)
    }

    override fun updateHistoryLastHtml() {
        //Log.d(LOG_TAG, "updateHistoryLastHtml " + currentPage);
        webView.evalJs("IThemeView.callbackUpdateHistoryHtml('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')")
        Log.d(LOG_TAG, "save scrollY " + webView.scrollY)
        webView.evalJs("console.log('JAVASCRIPT save scrollY '+window.scrollY)")
    }

    @JavascriptInterface
    fun callbackUpdateHistoryHtml(value: String) {
        runInUiThread(Runnable { presenter.updateHistoryLastHtml(value, webView.scrollY) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterForContextMenu(webView)
        webView.removeJavascriptInterface("IThemeView")
        webView.removeJavascriptInterface(JS_INTERFACE)
        webView.setJsLifeCycleListener(null)
        webView.endWork()
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {
        Log.d(LOG_TAG, "DOMContentLoaded")
        actions.add("setLoadAction(" + presenter.loadAction + ");")
        //Log.e("WebConsole", "" + currentPage.getScrollY() + " : " + App.get().getDensity() + " : " + ((int) (currentPage.getScrollY() / App.get().getDensity())));
        actions.add("setLoadScrollY(" + (presenter.getPageScrollY() / webView.resources.displayMetrics.density).toInt() + ");")
    }

    override fun onPageComplete(actions: ArrayList<String>) {
        presenter.loadAction = ThemePresenter.ActionState.NORMAL
        actions.add("setLoadAction(" + ThemePresenter.ActionState.NORMAL + ");")
    }

    override fun deletePostUi(post: IBaseForumPost) {
        webView.evalJs("deletePost(" + post.id + ");")
    }

    override fun openAnchorDialog(post: IBaseForumPost, anchorName: String) {
        dialogsHelper.openAnchorDialog(presenter, post, anchorName)
    }

    override fun openSpoilerLinkDialog(post: IBaseForumPost, spoilNumber: String) {
        dialogsHelper.openSpoilerLinkDialog(presenter, post, spoilNumber)
    }

    private inner class ThemeWebViewClient : CustomWebViewClient() {
        private val p = Pattern.compile("\\.(jpg|png|gif|bmp)")
        private val m = p.matcher("")

        override fun handleUri(uri: Uri): Boolean {
            presenter.handleNewUrl(uri)
            return true
        }

        override fun onLoadResource(view: WebView, url: String) {
            super.onLoadResource(view, url)
            if (presenter.loadAction === ThemePresenter.ActionState.NORMAL) {
                if (!url.contains("forum/uploads") && !url.contains("android_asset") && !url.contains("style_images") && m.reset(url).find()) {
                    webView.evalJs("onProgressChanged()")
                }
            }
        }
    }

    private inner class ThemeChromeClient : CustomWebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            if (presenter.loadAction === ThemePresenter.ActionState.NORMAL) {
                webView.evalJs("onProgressChanged()")
            }
        }
    }

    companion object {
        private val LOG_TAG = ThemeFragmentWeb::class.java.simpleName
        const val JS_INTERFACE = "IThemePresenter"
    }

}
