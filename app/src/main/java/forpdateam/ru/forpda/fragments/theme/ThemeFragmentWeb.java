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
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment {
    //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
    private final static int BACK_ACTION = 0, REFRESH_ACTION = 1, NORMAL_ACTION = 2;
    private final static String JS_INTERFACE = "ITheme";
    private int action = NORMAL_ACTION;

    private SwipeRefreshLayout refreshLayout;
    private ExtendedWebView webView;
    public ThemePage pageData;
    private WebViewClient webViewClient;
    private WebChromeClient chromeClient;
    private List<ThemePage> history = new ArrayList<>();

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        baseInflateFragment(inflater, R.layout.fragment_theme);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new ExtendedWebView(getContext());
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

        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.getSettings().setJavaScriptEnabled(true);
        registerForContextMenu(webView);

        //Кастомизация менюхи при выделении текста
        webView.setActionModeListener((actionMode, callback, type) -> {
            Menu menu = actionMode.getMenu();
            menu.clear();

            menu.add("Копировать")
                    .setIcon(App.getAppDrawable(R.drawable.ic_content_copy_white_24dp))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("copySelectedText()");
                        actionMode.finish();
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (pageData.canQuote())
                menu.add("Цитировать")
                        .setIcon(App.getAppDrawable(R.drawable.ic_quote_post_white_24dp))
                        .setOnMenuItemClickListener(item -> {
                            webView.evalJs("selectionToQuote()");
                            actionMode.finish();
                            return true;
                        })
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add("Весь текст")
                    .setIcon(App.getAppDrawable(R.drawable.ic_select_all_white_24dp))
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("selectAllPostText()");
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        });
        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setVisibility(View.VISIBLE);
        return view;
    }


    public void refreshOptionsMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        menu.add("Обновить").setIcon(App.getAppDrawable(R.drawable.ic_refresh_white_24dp)).setOnMenuItemClickListener(menuItem -> {
            action = REFRESH_ACTION;
            loadData();
            return false;
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (pageData != null) {
            menu.add("Ссылка").setOnMenuItemClickListener(menuItem -> false);
            menu.add("Найти на странице").setOnMenuItemClickListener(menuItem -> false);
            menu.add("Найти в теме").setOnMenuItemClickListener(menuItem -> false);
        }

        SubMenu subMenu = menu.addSubMenu("Опции темы");
        if (pageData != null) {
            if (pageData.isInFavorite()) {
                subMenu.add("Удалить из избранного").setOnMenuItemClickListener(menuItem -> false);
            } else {
                subMenu.add("Добавить в избранное").setOnMenuItemClickListener(menuItem -> false);
            }
            subMenu.add("Открыть форум темы").setOnMenuItemClickListener(menuItem -> false);
            subMenu.add("Кто читает тему").setOnMenuItemClickListener(menuItem -> false);
            subMenu.add("Кто писал сообщения").setOnMenuItemClickListener(menuItem -> false);
        }
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        //new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 15);
        /*getCompositeDisposable().add(Api.Theme().getPage(getTabUrl(), true)
                .onErrorReturn(throwable -> {
                    handleErrorRx(throwable, v -> loadData());
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadData, this::handleErrorRx, this::clearDisposables));*/

        Subscriber<ThemePage> generator = new Subscriber<>();

        //"подписка"
        generator.subscribe(ThemePage.class, Api.Theme().getPage(getTabUrl(), true), this::onLoadData);
    }


    private void onLoadData(ThemePage themePage) throws Exception {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);
        if (themePage == null || themePage.getId() == 0 || themePage.getUrl() == null) {
            return;
        }
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
        unregisterForContextMenu(webView);
        webView.setActionModeListener(null);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.setWebChromeClient(null);
        webView.setWebChromeClient(null);
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

    @Override
    public void onDestroy() {
        history.clear();
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

        private final Pattern p = Pattern.compile("\\.(jpg|png|gif|bmp)");
        private Matcher m = p.matcher("");

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

            //Log.d("kek", "IThemeJ: " + url);
            if (action == NORMAL_ACTION) {
                if (!url.contains("style_images") && m.reset(url).find()) {
                    webView.evalJs("onProgressChanged()");
                }
            }
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

    //Жалоба на сообщение
    private final static String reportWarningText = "Вам не нужно указывать здесь тему и сообщение, модератор автоматически получит эту информацию.\n\n" +
            "Пожалуйста, используйте эту возможность форума только для жалоб о некорректном сообщении!\n" +
            "Для связи с модератором используйте личные сообщения.";

    public void reportPost(ThemePost post) {
        if (App.getInstance().getPreferences().getBoolean("show_report_warning", true)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Внимание!")
                    .setMessage(reportWarningText)
                    .setPositiveButton("Ок", (dialogInterface, i) -> {
                        App.getInstance().getPreferences().edit().putBoolean("show_report_warning", false).apply();
                        showReportDialog(pageData.getId(), post.getId());
                    })
                    .show();
        } else {
            showReportDialog(pageData.getId(), post.getId());
        }
    }

    @SuppressLint("InflateParams")
    public void showReportDialog(int themeId, int postId) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);

        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);

        new AlertDialog.Builder(getContext())
                .setTitle("Жалоба на пост ".concat(getPostById(postId).getNick()))
                .setView(layout)
                .setPositiveButton("Отправить", (dialogInterface, i) -> doReportPost(themeId, postId, messageField.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void doReportPost(int themeId, int postId, String message) {
        getCompositeDisposable().add(Api.Theme().reportPost(themeId, postId, message)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Toast.makeText(getContext(), s == null ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show();
                }));
    }

    //Удаление сообщения
    public void deletePost(ThemePost post) {
        new AlertDialog.Builder(getContext())
                .setMessage("Удалить пост ".concat(post.getNick()).concat(" ?"))
                .setPositiveButton("Да", (dialogInterface, i) -> doDeletePost(post.getId()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void doDeletePost(int postId) {
        getCompositeDisposable().add(Api.Theme().deletePost(postId)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                    return false;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Toast.makeText(getContext(), aBoolean ? "Сообщение удалено" : "Ошибка", Toast.LENGTH_SHORT).show();
                    /*if (aBoolean) {
                        webView.evalJs("document.querySelector('div[name*=del" + postId + "]').remove();");
                    }*/
                }));
    }

    //Изменение репутации сообщения
    public void votePost(ThemePost post, boolean type) {
        getCompositeDisposable().add(Api.Theme().votePost(post.getId(), type)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Toast.makeText(getContext(), s == null ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show();
                }));
    }

    //Изменение репутации пользователя
    @SuppressLint("InflateParams")
    public void changeReputation(ThemePost post, boolean type) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText((type ? "Повысить" : "Понизить").concat(" репутацию ").concat(post.getNick()).concat(" ?"));

        new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton("Да", (dialogInterface, i) -> doChangeReputation(post.getId(), post.getUserId(), type, messageField.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void doChangeReputation(int postId, int userId, boolean type, String message) {
        getCompositeDisposable().add(Api.Theme().changeReputation(postId, userId, type, message)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Toast.makeText(getContext(), s == null ? "Репутация изменена" : s, Toast.LENGTH_SHORT).show();
                }));
    }

    //Вставка ответа пользователю
    public void insertNick(ThemePost post) {
        String insert = String.format(Locale.getDefault(), "[SNAPBACK]%s[/SNAPBACK] [b]%s,[/b]\n", post.getId(), post.getNick());
        Toast.makeText(getContext(), insert, Toast.LENGTH_SHORT).show();
    }

    public void quotePost(String text, ThemePost post) {
        String insert = String.format(Locale.getDefault(), "[quote name=\"%s\" date=\"%s\" post=%S]%s[/quote]", post.getNick(), post.getDate(), post.getId(), text);
        Toast.makeText(getContext(), insert, Toast.LENGTH_SHORT).show();
    }

    public void editPost(ThemePost post) {
        Toast.makeText(getContext(), "editpost ".concat(Integer.toString(post.getId())), Toast.LENGTH_SHORT).show();
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

    public void jumpToPage(int st) {
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
        run(() -> ThemeDialogsHelper.selectPage(this, pageData));
    }

    @JavascriptInterface
    public void showUserMenu(final String postId) {
        run(() -> ThemeDialogsHelper.showUserMenu(this, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showReputationMenu(final String postId) {
        run(() -> ThemeDialogsHelper.showReputationMenu(this, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showPostMenu(final String postId) {
        run(() -> ThemeDialogsHelper.showPostMenu(this, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void reportPost(final String postId) {
        run(() -> reportPost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void insertNick(final String postId) {
        run(() -> insertNick(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void quotePost(final String text, final String postId) {
        run(() -> quotePost(text, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void deletePost(final String postId) {
        run(() -> deletePost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void editPost(final String postId) {
        run(() -> editPost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void votePost(final String postId, final boolean type) {
        run(() -> votePost(getPostById(Integer.parseInt(postId)), type));
    }

    @JavascriptInterface
    public void setHistoryBody(final String index, final String body) {
        run(() -> history.get(Integer.parseInt(index)).setHtml(body.replaceAll("data-block-init=\"1\"", "")));
    }

    @JavascriptInterface
    public void copySelectedText(final String text) {
        run(() -> Utils.copyToClipBoard(text));
    }

    @JavascriptInterface
    public void toast(final String text) {
        run(() -> Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show());
    }

    @JavascriptInterface
    public void log(final String text) {
        Log.d("kek", "ITheme: ".concat(text));
    }
}
