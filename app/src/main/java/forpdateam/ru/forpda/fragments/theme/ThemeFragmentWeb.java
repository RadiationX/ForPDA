package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.NestedWebView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment {
    //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
    private final static int BACK_ACTION = 0, REFRESH_ACTION = 1, NORMAL_ACTION = 2;
    private int action = NORMAL_ACTION;
    private SwipeRefreshLayout refreshLayout;
    private NestedWebView webView;
    private ThemePage pageData;
    private WebViewClient webViewClient;
    private WebChromeClient chromeClient;
    private List<ThemePage> history = new ArrayList<>();

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_theme, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new NestedWebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        webView.loadUrl("about:blank");
        refreshLayout.addView(webView);
        viewsReady();


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);

        refreshLayout.setOnRefreshListener(this::loadData);

        webView.addJavascriptInterface(this, "ITheme");
        webView.getSettings().setJavaScriptEnabled(true);

        return view;
    }


    public void refreshOptionsMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        menu.add("Обновить").setIcon(R.drawable.ic_refresh_white_24dp).setOnMenuItemClickListener(menuItem -> {
            action = REFRESH_ACTION;
            loadData();
            return false;
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (pageData != null) {
            menu.add("Ссылка").setOnMenuItemClickListener(menuItem -> {
                return false;
            });
            menu.add("Найти на странице").setOnMenuItemClickListener(menuItem -> {
                return false;
            });
            menu.add("Найти в теме").setOnMenuItemClickListener(menuItem -> {
                return false;
            });
        }

        SubMenu subMenu = menu.addSubMenu("Опции темы");
        if (pageData != null) {
            if (pageData.isInFavorite()) {
                subMenu.add("Удалить из избранного").setOnMenuItemClickListener(menuItem -> {
                    return false;
                });
            } else {
                subMenu.add("Добавить в избранное").setOnMenuItemClickListener(menuItem -> {
                    return false;
                });
            }
            subMenu.add("Открыть форум темы").setOnMenuItemClickListener(menuItem -> {
                return false;
            });
            subMenu.add("Кто читает тему").setOnMenuItemClickListener(menuItem -> {
                return false;
            });
            subMenu.add("Кто писал сообщения").setOnMenuItemClickListener(menuItem -> {
                return false;
            });

        }
    }


    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        getCompositeDisposable().add(Api.Theme().getPage(getTabUrl(), true)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> ErrorHandler.handle(this, throwable, null)));
    }

    private void bindUi(ThemePage themePage) throws IOException {
        setTabUrl(themePage.getUrl());
        if (pageData != null) {
            if (pageData.getUrl().equals(getTabUrl())) {
                themePage.setScrollY(webView.getScrollY());
            } else {
                pageData.setScrollY(webView.getScrollY());
                history.add(pageData);
                webView.evalJs("ITheme.setHistoryBody(" + (history.size() - 1) + ",'<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        }
        pageData = themePage;
        if (webViewClient == null) {
            webViewClient = new ThemeWebViewClient();
            webView.setWebViewClient(webViewClient);
        }
        if (chromeClient == null) {
            chromeClient = new ThemeChromeClient();
            webView.setWebChromeClient(chromeClient);
        }
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);

        updateView();
    }

    private void updateTitle() {
        setTitle(pageData.getTitle());
    }

    private void updateSubTitle() {
        setSubtitle(String.valueOf(pageData.getCurrentPage()).concat("/").concat(String.valueOf(pageData.getAllPagesCount())));
    }

    private void updateView() {
        setTabUrl(pageData.getUrl());
        updateTitle();
        updateSubTitle();
        refreshOptionsMenu();
        webView.loadDataWithBaseURL(getTabUrl(), pageData.getHtml(), "text/html", "utf-8", null);
    }

    @Override
    public boolean onBackPressed() {
        if (history.size() > 0) {
            action = BACK_ACTION;
            pageData = history.get(history.size() - 1);
            history.remove(history.size() - 1);
            updateView();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView.loadUrl("about:blank");
        webView.clearHistory();
        webView.clearSslPreferences();
        webView.clearDisappearingChildren();
        webView.clearFocus();
        webView.clearFormData();
        webView.clearMatches();
        webView.clearCache(true);
        ((ViewGroup) webView.getParent()).removeAllViews();
        if (getMainActivity().getWebViews().size() < 10) {
            getMainActivity().getWebViews().add(webView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            Log.d("kek", "handle " + uri);
            if (uri.getHost() != null && uri.getHost().matches("4pda.ru")) {
                if (uri.getPathSegments().get(0).equals("forum")) {
                    String param = uri.getQueryParameter("showtopic");
                    Log.d("kek", "param" + param);
                    if (param != null && !param.equals(Uri.parse(getTabUrl()).getQueryParameter("showtopic"))) {
                        load(uri);
                        return true;
                    }
                    param = uri.getQueryParameter("act");
                    if (param == null)
                        param = uri.getQueryParameter("view");
                    Log.d("kek", "param" + param);
                    if (param != null && param.equals("findpost")) {
                        String postId = uri.getQueryParameter("pid");
                        if (postId == null)
                            postId = uri.getQueryParameter("p");
                        Log.d("kek", "param" + postId);
                        if (postId != null && getPostById(Integer.parseInt(postId)) != null) {
                            Log.d("kek", " scroll to " + postId);
                            Matcher matcher = Theme.elemToScrollPattern.matcher(uri.toString());
                            String elem = null;
                            while (matcher.find()) {
                                elem = matcher.group(1);
                            }
                            webView.evalJs("scrollToElement(\"".concat(elem == null ? "entry" : "").concat(elem != null ? elem : postId).concat("\")"));
                            return true;
                        } else {
                            load(uri);
                            return true;
                        }
                    }
                }
            }
            IntentHandler.handle(uri.toString());

            return true;
        }

        private void load(Uri uri) {
            action = NORMAL_ACTION;
            setTabUrl(uri.toString());
            loadData();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (action == NORMAL_ACTION)
                webView.evalJs("onProgressChanged()");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (action == BACK_ACTION || action == REFRESH_ACTION)
                webView.evalJs("window.doOnLoadScroll = false");
            if (action == BACK_ACTION)
                webView.scrollTo(0, pageData.getScrollY());
        }

    }

    private class ThemeChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (action == NORMAL_ACTION)
                webView.evalJs("onProgressChanged()");
            else if (action == BACK_ACTION || action == REFRESH_ACTION)
                webView.scrollTo(0, pageData.getScrollY());
        }
    }


    /*
    *
    * Post functions
    *
    * */

    public void reportPost(ThemePost post) {

    }

    public void insertNick(ThemePost post) {
        String insert = String.format(Locale.getDefault(), "[SNAPBACK]%s[/SNAPBACK] [b]%s,[/b]\n", post.getId(), post.getNick());
        Toast.makeText(getContext(), insert, Toast.LENGTH_SHORT).show();
    }

    public void quotePost(ThemePost post) {

    }

    public void deletePost(ThemePost post) {

    }

    public void editPost(ThemePost post) {

    }

    public void votePost(ThemePost post, boolean type) {

    }

    public void reputationAction(ThemePost post, boolean type) {

    }


    /*
    *
    * JavaScript Interface additional functions
    *
    * */

    private ThemePost getPostById(int postId) {
        for (ThemePost post : pageData.getPosts())
            if (post.getId() == postId)
                return post;
        return null;
    }

    private void jumpToPage(int st) {
        String url = "http://4pda.ru/forum/index.php?showtopic=";
        url = url.concat(Uri.parse(getTabUrl()).getQueryParameter("showtopic"));
        if (st != 0) url = url.concat("&st=").concat(Integer.toString(st));
        setTabUrl(url);
        loadData();
    }

    /*
    *
    * JavaScript Interface functions
    *
    * */

    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }

    @JavascriptInterface
    public void firstPage() {
        if (pageData.getCurrentPage() <= 0) return;
        run(() -> jumpToPage(0));
    }

    @JavascriptInterface
    public void prevPage() {
        if (pageData.getCurrentPage() <= 1) return;
        run(() -> jumpToPage((pageData.getCurrentPage() - 2) * pageData.getPostsOnPageCount()));
    }

    @JavascriptInterface
    public void nextPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        run(() -> jumpToPage(pageData.getCurrentPage() * pageData.getPostsOnPageCount()));
    }

    @JavascriptInterface
    public void lastPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        run(() -> jumpToPage((pageData.getAllPagesCount() - 1) * pageData.getPostsOnPageCount()));
    }

    @JavascriptInterface
    public void selectPage() {
        run(() -> {
            final int[] pages = new int[pageData.getAllPagesCount()];

            for (int i = 0; i < pageData.getAllPagesCount(); i++)
                pages[i] = i + 1;

            LayoutInflater inflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.select_page_layout, null);

            assert view != null;
            final ListView listView = (ListView) view.findViewById(R.id.listview);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new ThemePagesAdapter(getContext(), pages));
            listView.setItemChecked(pageData.getCurrentPage() - 1, true);
            listView.setSelection(pageData.getCurrentPage() - 1);

            AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                    .setView(view)
                    .show();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
                if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                    return;
                }
                jumpToPage(i2 * pageData.getPostsOnPageCount());
                dialog.cancel();
            });

        });
    }


    private ThemePopupMenu<ThemePost> userMenu;

    @JavascriptInterface
    public void showUserMenu(final int postId) {
        run(() -> {
            final ThemePost post = getPostById(postId);
            if (userMenu == null) {
                userMenu = new ThemePopupMenu<>();
                userMenu.addItem("Профиль", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + ((ThemePost) data).getUserId()));
                if (Api.Auth().getState())
                    userMenu.addItem("Личные сообщения QMS", data -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&mid=" + ((ThemePost) data).getUserId()));
                userMenu.addItem("Темы пользователя", data -> Toast.makeText(getContext(), "Не умею", Toast.LENGTH_SHORT).show());
                userMenu.addItem("Сообщения в этой теме", data -> Toast.makeText(getContext(), "Не умею", Toast.LENGTH_SHORT).show());
                userMenu.addItem("Сообщения пользователя", data -> Toast.makeText(getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            }
            new AlertDialog.Builder(getContext())
                    .setTitle(post.getNick())
                    .setItems(userMenu.getTitles(), (dialogInterface, i) -> userMenu.onClick(i, post))
                    .show();
        });
    }

    private ThemePopupMenu<ThemePost> reputationMenu;

    @JavascriptInterface
    public void showReputationMenu(final int postId) {
        run(() -> {
            final ThemePost post = getPostById(postId);
            if (reputationMenu == null) {
                reputationMenu = new ThemePopupMenu<>();
                reputationMenu.addItem("Посмотреть", data -> Toast.makeText(getContext(), "Слепой", Toast.LENGTH_SHORT).show());
            }
            if (Api.Auth().getState()) {
                int index = reputationMenu.containsIndex("Повысить");
                if (index == -1) {
                    if (post.canPlusRep())
                        reputationMenu.addItem(0, "Повысить", data -> reputationAction(post, true));
                } else {
                    if (!post.canPlusRep())
                        reputationMenu.remove(index);
                }

                index = reputationMenu.containsIndex("Понизить");
                if (index == -1) {
                    if (post.canPlusRep())
                        reputationMenu.addItem(2, "Понизить", data -> reputationAction(post, false));
                } else {
                    if (!post.canPlusRep())
                        reputationMenu.remove(index);
                }
            }
            new AlertDialog.Builder(getContext())
                    .setTitle("Репутация ".concat(post.getNick()))
                    .setItems(reputationMenu.getTitles(), (dialogInterface, i) -> reputationMenu.onClick(i, post))
                    .show();
        });
    }

    private ThemePopupMenu<ThemePost> postMenu;

    @JavascriptInterface
    public void showPostMenu(final int postId) {
        run(() -> {
            final ThemePost post = getPostById(postId);
            if (postMenu == null) {
                postMenu = new ThemePopupMenu<>();
                if (Api.Auth().getState()) {
                    if (post.canQuote()) {
                        postMenu.addItem("Ответить", data -> insertNick(post));
                        postMenu.addItem("Цитировать", data -> quotePost(post));
                    }
                    if (post.canReport()) {
                        postMenu.addItem("Пожаловаться", data -> reportPost(post));
                    }
                }
                postMenu.addItem("Ссылка на сообщение", data -> Toast.makeText(getContext(), "Не умею", Toast.LENGTH_SHORT).show());
            }
            new AlertDialog.Builder(getContext())
                    .setItems(postMenu.getTitles(), (dialogInterface, i) -> postMenu.onClick(i, post))
                    .show();
        });
    }

    @JavascriptInterface
    public void reportPost(final int postId) {
        run(() -> reportPost(getPostById(postId)));
    }

    @JavascriptInterface
    public void insertNick(final int postId) {
        run(() -> insertNick(getPostById(postId)));
    }

    @JavascriptInterface
    public void quotePost(final int postId) {
        run(() -> quotePost(getPostById(postId)));
    }

    @JavascriptInterface
    public void deletePost(final int postId) {
        run(() -> deletePost(getPostById(postId)));
    }

    @JavascriptInterface
    public void editPost(final int postId) {
        run(() -> editPost(getPostById(postId)));
    }

    @JavascriptInterface
    public void votePost(final int postId, final boolean type) {
        run(() -> votePost(getPostById(postId), type));
    }

    @JavascriptInterface
    public void setHistoryBody(final String index, final String body) {
        run(() -> history.get(Integer.parseInt(index)).setHtml(body.replaceAll("data-block-init=\"1\"", "")));
    }
}
