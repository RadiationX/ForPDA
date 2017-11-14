package forpdateam.ru.forpda.ui.fragments.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.common.webview.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment implements IPostFunctions, ExtendedWebView.JsLifeCycleListener {
    private final static String LOG_TAG = ThemeFragmentWeb.class.getSimpleName();
    public final static String JS_INTERFACE = "ITheme";
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

        messagePanel.setHeightChangeListener(newHeight -> {
            webView.setPaddingBottom(newHeight);
        });
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        webView.setJsLifeCycleListener(this);
        refreshLayout.addView(webView);
        refreshLayoutLongTrigger(refreshLayout);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_POSTS_FUNCTIONS);
        registerForContextMenu(webView);
        fab.setOnClickListener(v -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true);
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true);
            }
        });
        webView.setOnDirectionListener(direction -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_down));
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getVecDrawable(fab.getContext(), R.drawable.ic_arrow_up));
            }
        });
        //Кастомизация менюхи при выделении текста
        webView.setActionModeListener((actionMode, callback, type) -> {
            Menu menu = actionMode.getMenu();
            ArrayList<MenuItem> items = new ArrayList<>();
            for (int i = 0; i < menu.size(); i++) {
                items.add(menu.getItem(i));
            }
            menu.clear();

            menu.add(R.string.copy)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_content_copy))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("copySelectedText()");
                        actionMode.finish();
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (currentPage.canQuote())
                menu.add(R.string.quote)
                        .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_quote_post))
                        .setOnMenuItemClickListener(item -> {
                            webView.evalJs("selectionToQuote()");
                            actionMode.finish();
                            return true;
                        })
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(R.string.all_text)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_select_all))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("selectAllPostText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(R.string.share)
                    .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_share))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("shareSelectedText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            for (MenuItem item : items) {
                if (item.getIntent() != null) {
                    menu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle())
                            .setIntent(item.getIntent())
                            .setNumericShortcut(item.getNumericShortcut())
                            .setAlphabeticShortcut(item.getAlphabeticShortcut());
                }
            }
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
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", currentPage.getHtml(), "text/html", "utf-8", null);
        webView.updatePaddingBottom();
    }

    @Override
    protected void saveToHistory(ThemePage themePage) {
        Log.d(LOG_TAG, "saveToHistory " + themePage);
        history.add(themePage);
    }

    @Override
    protected void updateHistoryLast(ThemePage themePage) {
        Log.d(LOG_TAG, "updateHistoryLast " + themePage + " : " + currentPage);
        ThemePage lastHistory = history.get(history.size() - 1);
        themePage.getAnchors().addAll(lastHistory.getAnchors());
        themePage.setScrollY(lastHistory.getScrollY());
        history.set(history.size() - 1, themePage);
    }

    @Override
    protected void updateShowAvatarState(boolean isShow) {
        webView.evalJs("updateShowAvatarState(" + isShow + ")");
    }

    @Override
    protected void updateTypeAvatarState(boolean isCircle) {
        webView.evalJs("updateTypeAvatarState(" + isCircle + ")");
    }

    @Override
    protected void setFontSize(int size) {
        webView.setRelativeFontSize(size);
    }

    @Override
    protected void updateHistoryLastHtml() {
        Log.d(LOG_TAG, "updateHistoryLastHtml " + currentPage);
        webView.evalJs("ITheme.callbackUpdateHistoryHtml('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
        Log.d(LOG_TAG, "save scrollY " + webView.getScrollY());
        webView.evalJs("console.log('JAVASCRIPT save scrollY '+window.scrollY)");
    }

    @JavascriptInterface
    public void callbackUpdateHistoryHtml(String value) {
        ThemePage themePage = history.get(history.size() - 1);
        Log.d(LOG_TAG, "updateHistoryLastHtml " + themePage + " : " + currentPage);

        themePage.setScrollY(webView.getScrollY());
        themePage.setHtml(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_POSTS_FUNCTIONS);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    private class ThemeWebViewClient extends CustomWebViewClient {
        @Override
        public boolean handleUri(Uri uri) {
            Log.d(LOG_TAG, "handle " + uri);
            try {
                if (checkIsPoll(uri.toString())) return true;
                if (uri.getHost() != null && uri.getHost().matches("4pda.ru")) {
                    if (uri.getPathSegments().get(0).equals("forum")) {
                        String param = uri.getQueryParameter("showtopic");
                        Log.d(LOG_TAG, "param showtopic: " + param);
                        if (param != null && !param.equals(Uri.parse(tab_url).getQueryParameter("showtopic"))) {
                            load(uri);
                            return true;
                        }
                        param = uri.getQueryParameter("act");
                        if (param == null)
                            param = uri.getQueryParameter("view");
                        Log.d(LOG_TAG, "param act|view: " + param);
                        if (param != null && param.equals("findpost")) {
                            String postId = uri.getQueryParameter("pid");
                            if (postId == null)
                                postId = uri.getQueryParameter("p");
                            Log.d(LOG_TAG, "param pid|p: " + postId);
                            if (postId != null) {
                                postId = postId.replaceAll("[^\\d][\\s\\S]*", "");
                            }
                            Log.d(LOG_TAG, "param postId: " + postId);
                            if (postId != null && getPostById(Integer.parseInt(postId.trim())) != null) {
                                Matcher matcher = Theme.elemToScrollPattern.matcher(uri.toString());
                                String elem = null;
                                while (matcher.find()) {
                                    elem = matcher.group(1);
                                }
                                Log.d(LOG_TAG, " scroll to " + postId + " : " + elem);
                                String finalAnchor = (elem == null ? "entry" : "").concat(elem != null ? elem : postId);
                                if (App.get().getPreferences().getBoolean("theme.anchor_history", true)) {
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
            } catch (Exception ex) {
                ACRA.getErrorReporter().handleException(ex);
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

            //Log.d(LOG_TAG, "IThemeJ: " + url);
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

    private class ThemeChromeClient extends CustomWebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (loadAction == NORMAL_ACTION)
                webView.evalJs("onProgressChanged()");
            /*else if (loadAction == BACK_ACTION || loadAction == REFRESH_ACTION)
                webView.scrollTo(0, currentPage.getScrollY());*/
        }
    }


    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
        Log.d(LOG_TAG, "DOMContentLoaded");
        actions.add("setLoadAction(" + loadAction + ");");
        Log.e("WebConsole", "" + currentPage.getScrollY() + " : " + App.get().getDensity() + " : " + ((int) (currentPage.getScrollY() / App.get().getDensity())));
        actions.add("setLoadScrollY(" + ((int) (currentPage.getScrollY() / App.get().getDensity())) + ");");
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
        setAction(NORMAL_ACTION);
        actions.add("setLoadAction(" + NORMAL_ACTION + ");");
    }

    /*
    *
    * JavaScript Interface functions
    *
    * */

    @JavascriptInterface
    @Override
    public void firstPage() {
        if (getContext() == null)
            return;
        runInUiThread(super::firstPage);
    }

    @JavascriptInterface
    @Override
    public void prevPage() {
        if (getContext() == null)
            return;
        runInUiThread(super::prevPage);
    }

    @JavascriptInterface
    @Override
    public void nextPage() {
        if (getContext() == null)
            return;
        runInUiThread(super::nextPage);
    }

    @JavascriptInterface
    @Override
    public void lastPage() {
        if (getContext() == null)
            return;
        runInUiThread(super::lastPage);
    }

    @JavascriptInterface
    @Override
    public void selectPage() {
        if (getContext() == null)
            return;
        runInUiThread(super::selectPage);
    }

    @JavascriptInterface
    @Override
    public void showUserMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.showUserMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void showReputationMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.showReputationMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void showPostMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.showPostMenu(postId));
    }

    @JavascriptInterface
    @Override
    public void reportPost(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.reportPost(postId));
    }

    @JavascriptInterface
    @Override
    public void reply(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.reply(postId));
    }

    @JavascriptInterface
    @Override
    public void quotePost(final String text, final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.quotePost(text, postId));
    }

    @JavascriptInterface
    @Override
    public void deletePost(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.deletePost(postId));
    }

    @JavascriptInterface
    @Override
    public void editPost(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.editPost(postId));
    }

    @JavascriptInterface
    @Override
    public void votePost(final String postId, final boolean type) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.votePost(postId, type));
    }

    @JavascriptInterface
    @Override
    public void setHistoryBody(final String index, final String body) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.setHistoryBody(index, body));
    }

    @JavascriptInterface
    @Override
    public void copySelectedText(final String text) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.copySelectedText(text));
    }

    @JavascriptInterface
    @Override
    public void toast(final String text) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.toast(text));
    }

    @JavascriptInterface
    @Override
    public void log(final String text) {
        if (getContext() == null)
            return;
        runInUiThread(() -> super.log(text));
    }

    @JavascriptInterface
    @Override
    public void showPollResults() {
        if (getContext() == null)
            return;
        runInUiThread(super::showPollResults);
    }

    @JavascriptInterface
    @Override
    public void showPoll() {
        if (getContext() == null)
            return;
        runInUiThread(super::showPoll);
    }

    @Override
    public void deletePostUi(IBaseForumPost post) {
        if (getContext() == null || post == null) {
            return;
        }
        webView.evalJs("deletePost(" + post.getId() + ");");
    }

    @JavascriptInterface
    public void copySpoilerLink(String postId, String spoilNumber) {
        if (getContext() == null)
            return;
        runInUiThread(() -> {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.spoiler_link_copy_ask)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        IBaseForumPost post = getPostById(Integer.parseInt(postId));
                        String s = "https://4pda.ru/forum/index.php?act=findpost&pid=" + post.getId() + "&anchor=Spoil-" + post.getId() + "-" + spoilNumber;
                        Utils.copyToClipBoard(s);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    @JavascriptInterface
    public void setPollOpen(String sValue) {
        if (getContext() == null)
            return;
        runInUiThread(() -> {
            boolean value = Boolean.parseBoolean(sValue);
            currentPage.setPollOpen(value);
        });
    }

    @JavascriptInterface
    public void setHatOpen(String sValue) {
        if (getContext() == null)
            return;
        runInUiThread(() -> {
            boolean value = Boolean.parseBoolean(sValue);
            currentPage.setHatOpen(value);
        });
    }

    @JavascriptInterface
    public void shareSelectedText(String text) {
        if (getContext() == null)
            return;
        runInUiThread(() -> {
            Utils.shareText(text);
        });
    }

    @JavascriptInterface
    public void anchorDialog(String postId, String name) {
        if (getContext() == null)
            return;
        runInUiThread(() -> {
            IBaseForumPost post = getPostById(Integer.parseInt(postId));
            String link = "https://4pda.ru/forum/index.php?act=findpost&pid=" + post.getId() + "&anchor=" + name;
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.link_to_anchor)
                    .setMessage(link)
                    .setPositiveButton(R.string.copy, (dialog, which) -> {
                        Utils.copyToClipBoard(link);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }
}
