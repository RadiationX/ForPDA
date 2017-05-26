package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment {
    private final static String JS_INTERFACE = "ITheme";
    private ExtendedWebView webView;
    private WebViewClient webViewClient;
    private WebChromeClient chromeClient;

    @Override
    public void scrollToAnchor(String anchor) {
        webView.evalJs("scrollToElement(\"" + anchor + "\")");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void addShowingView() {
        messagePanel.setHeightChangeListener(newHeight -> webView.evalJs("setPaddingBottom(" + (newHeight / getResources().getDisplayMetrics().density) + ");"));
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new ExtendedWebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        /*webView.setClipToPadding(false);
        webView.setPadding(0, 0, 0, App.px64);*/
        //webView.loadUrl("about:blank");
        refreshLayout.addView(webView);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_POSTS_FUNCTIONS);
        webView.getSettings().setJavaScriptEnabled(true);
        registerForContextMenu(webView);

        //Кастомизация менюхи при выделении текста
        webView.setActionModeListener((actionMode, callback, type) -> {
            Menu menu = actionMode.getMenu();
            menu.clear();

            menu.add("Копировать")
                    .setIcon(App.getAppDrawable(R.drawable.ic_content_copy_gray_24dp))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("copySelectedText()");
                        actionMode.finish();
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (currentPage.canQuote())
                menu.add("Цитировать")
                        .setIcon(App.getAppDrawable(R.drawable.ic_quote_post_gray_24dp))
                        .setOnMenuItemClickListener(item -> {
                            webView.evalJs("selectionToQuote()");
                            actionMode.finish();
                            return true;
                        })
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add("Весь текст")
                    .setIcon(App.getAppDrawable(R.drawable.ic_select_all_gray_24dp))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("selectAllPostText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        });
    }

    @Override
    protected void findNext(boolean next) {
        webView.findNext(next);
    }

    @Override
    protected void findText(String text) {
        webView.findAllAsync(text);
    }

    @Override
    protected void updateView() {
        super.updateView();
        if (webViewClient == null) {
            webViewClient = new ThemeFragmentWeb.ThemeWebViewClient();
            webView.setWebViewClient(webViewClient);
        }
        if (chromeClient == null) {
            chromeClient = new ThemeFragmentWeb.ThemeChromeClient();
            webView.setWebChromeClient(chromeClient);
        }
        webView.loadDataWithBaseURL("http://4pda.ru/forum/", currentPage.getHtml(), "text/html", "utf-8", null);
    }

    @Override
    protected void saveToHistory(ThemePage themePage) {
        Log.e("console", "saveToHistory " + themePage);
        history.add(themePage);
    }

    @Override
    protected void updateHistoryLast(ThemePage themePage) {
        Log.e("console", "updateHistoryLast " + themePage + " : " + currentPage);
        history.set(history.size() - 1, themePage);
    }

    @Override
    protected void updateHistoryLastHtml() {
        Log.e("console", "updateHistoryLastHtml");
        webView.evalJs("ITheme.callbackUpdateHistoryHtml('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
        Log.e("console", "save scrollY " + webView.getScrollY());
        webView.evalJs("console.log('JAVASCRIPT save scrollY '+window.scrollY)");
    }

    @JavascriptInterface
    public void callbackUpdateHistoryHtml(String value) {
        ThemePage themePage = history.get(history.size() - 1);
        Log.e("console", "updateHistoryLastHtml " + themePage + " : " + currentPage);

        themePage.setScrollY(webView.getScrollY());
        themePage.setHtml(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(webView);
        webView.setActionModeListener(null);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_POSTS_FUNCTIONS);
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.loadUrl("about:blank");
        webView.clearHistory();
        webView.clearSslPreferences();
        webView.clearDisappearingChildren();
        webView.clearFocus();
        webView.clearFormData();
        webView.clearMatches();
        ((ViewGroup) webView.getParent()).removeAllViews();
        if (getMainActivity().getWebViews().size() < 10) {
            getMainActivity().getWebViews().add(webView);
        }
    }

    private class ThemeWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }


        private boolean handleUri(Uri uri) {
            Log.d("FORPDA_LOG", "handle " + uri);
            if (checkIsPoll(uri.toString())) return true;
            if (uri.getHost() != null && uri.getHost().matches("4pda.ru")) {
                if (uri.getPathSegments().get(0).equals("forum")) {
                    String param = uri.getQueryParameter("showtopic");
                    Log.d("FORPDA_LOG", "param" + param);
                    if (param != null && !param.equals(Uri.parse(tab_url).getQueryParameter("showtopic"))) {
                        load(uri);
                        return true;
                    }
                    param = uri.getQueryParameter("act");
                    if (param == null)
                        param = uri.getQueryParameter("view");
                    Log.d("FORPDA_LOG", "param" + param);
                    if (param != null && param.equals("findpost")) {
                        String postId = uri.getQueryParameter("pid");
                        if (postId == null)
                            postId = uri.getQueryParameter("p");
                        Log.d("FORPDA_LOG", "param" + postId);
                        if (postId != null && getPostById(Integer.parseInt(postId.trim())) != null) {
                            Matcher matcher = Theme.elemToScrollPattern.matcher(uri.toString());
                            String elem = null;
                            while (matcher.find()) {
                                elem = matcher.group(1);
                            }
                            Log.d("FORPDA_LOG", " scroll to " + postId + " : " + elem);
                            String finalAnchor = (elem == null ? "entry" : "").concat(elem != null ? elem : postId);
                            if (App.getInstance().getPreferences().getBoolean("theme.anchor_history", true)) {
                                currentPage.addAnchor(finalAnchor);
                            }
                            scrollToAnchor(finalAnchor);
                            return true;
                        } else {
                            load(uri);
                            return true;
                        }
                    }
                }
            }
            String url = uri.toString();
            if (Theme.attachImagesPattern.matcher(url).find()) {
                for (ThemePost post : currentPage.getPosts()) {
                    for (Pair<String, String> image : post.getAttachImages()) {
                        if (image.first.contains(url)) {
                            ArrayList<String> list = new ArrayList<>();
                            for (Pair<String, String> attaches : post.getAttachImages()) {
                                list.add(attaches.first);
                            }
                            ImageViewerActivity.startActivity(App.getContext(), list, post.getAttachImages().indexOf(image));
                            return true;
                        }
                    }
                }
            }
            IntentHandler.handle(uri.toString());

            return true;
        }

        private boolean checkIsPoll(String url) {
            Matcher m = Pattern.compile("4pda.ru.*?addpoll=1").matcher(url);
            if (m.find()) {
                Uri uri = Uri.parse(url);
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", Integer.toString(currentPage.getId()))
                        .appendQueryParameter("st", "" + currentPage.getPagination().getCurrent() * currentPage.getPagination().getPerPage())
                        .build();
                load(uri);
                return true;
            }
            return false;
        }

        private void load(Uri uri) {
            tab_url = uri.toString();
            loadData(NORMAL_ACTION);
        }

        private final Pattern p = Pattern.compile("\\.(jpg|png|gif|bmp)");
        private Matcher m = p.matcher("");

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

            Log.d("FORPDA_LOG", "IThemeJ: " + url);
            if (loadAction == NORMAL_ACTION) {
                if (!url.contains("forum/uploads") && !url.contains("android_asset") && !url.contains("style_images") && m.reset(url).find()) {
                    webView.evalJs("onProgressChanged()");
                }
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            /*//TODO сделать привязку к событиям js, вместо этого говнища
            updateHistoryLastHtml();*/
        }
    }

    private class ThemeChromeClient extends WebChromeClient {
        final static String tag = "WebConsole";

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (loadAction == NORMAL_ACTION)
                webView.evalJs("onProgressChanged()");
            /*else if (loadAction == BACK_ACTION || loadAction == REFRESH_ACTION)
                webView.scrollTo(0, currentPage.getScrollY());*/
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String message = "";
            message += "\"" + consoleMessage.message() + "\"";
            if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                message += ", [" + consoleMessage.sourceId() + "]";
            }
            message += ", (" + consoleMessage.lineNumber() + ")";

            ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
            if (level == ConsoleMessage.MessageLevel.DEBUG) {
                Log.d(tag, message);
            } else if (level == ConsoleMessage.MessageLevel.ERROR) {
                Log.e(tag, message);
            } else if (level == ConsoleMessage.MessageLevel.WARNING) {
                Log.w(tag, message);
            } else if (level == ConsoleMessage.MessageLevel.LOG || level == ConsoleMessage.MessageLevel.TIP) {
                Log.i(tag, message);
            }
            return true;
        }
    }


    @JavascriptInterface
    public void domContentLoaded() {
        run(() -> {
            Log.e("console", "DOMContentLoaded");
            String script = "";
            script += "setLoadAction(" + loadAction + ");";
            script += "setLoadScrollY(" + ((int) (currentPage.getScrollY() / App.getInstance().getDensity())) + ");";
            script += "nativeEvents.onNativeDomComplete();";
            webView.evalJs(script);
        });
    }

    @JavascriptInterface
    public void onPageLoaded() {
        run(() -> {
            setAction(NORMAL_ACTION);
            Log.e("console", "onPageLoaded");
            String script = "";
            script += "setLoadAction(" + NORMAL_ACTION + ");";
            script += "nativeEvents.onNativePageComplete()";
            webView.evalJs(script);
        });
    }

    /*
    *
    * JavaScript Interface functions
    *
    * */
    @JavascriptInterface
    @Override
    public void firstPage() {
        run(super::firstPage);
    }

    @JavascriptInterface
    @Override
    public void prevPage() {
        run(super::prevPage);
    }

    @JavascriptInterface
    @Override
    public void nextPage() {
        run(super::nextPage);
    }

    @JavascriptInterface
    @Override
    public void lastPage() {
        run(super::lastPage);
    }

    @JavascriptInterface
    @Override
    public void selectPage() {
        run(super::selectPage);
    }

    @JavascriptInterface
    @Override
    public void showUserMenu(final String postId) {
        run(() -> super.showUserMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void showReputationMenu(final String postId) {
        run(() -> super.showReputationMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void showPostMenu(final String postId) {
        run(() -> super.showPostMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void reportPost(final String postId) {
        run(() -> super.reportPost(postId));
    }

    @JavascriptInterface
    @Override
    public void insertNick(final String postId) {
        run(() -> super.insertNick(postId));
    }

    @JavascriptInterface
    @Override
    public void quotePost(final String text, final String postId) {
        run(() -> super.quotePost(text, postId));
    }

    @JavascriptInterface
    @Override
    public void deletePost(final String postId) {
        run(() -> super.deletePost(postId));
    }

    @JavascriptInterface
    @Override
    public void editPost(final String postId) {
        run(() -> super.editPost(postId));
    }

    @JavascriptInterface
    @Override
    public void votePost(final String postId, final boolean type) {
        run(() -> super.votePost(postId, type));
    }

    @JavascriptInterface
    @Override
    public void setHistoryBody(final String index, final String body) {
        run(() -> super.setHistoryBody(index, body));
    }

    @JavascriptInterface
    @Override
    public void copySelectedText(final String text) {
        run(() -> super.copySelectedText(text));
    }

    @JavascriptInterface
    @Override
    public void toast(final String text) {
        run(() -> super.toast(text));
    }

    @JavascriptInterface
    @Override
    public void log(final String text) {
        run(() -> super.log(text));
    }

    @JavascriptInterface
    @Override
    public void showPollResults() {
        run(super::showPollResults);
    }

    @JavascriptInterface
    @Override
    public void showPoll() {
        run(super::showPoll);
    }

    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }
}
