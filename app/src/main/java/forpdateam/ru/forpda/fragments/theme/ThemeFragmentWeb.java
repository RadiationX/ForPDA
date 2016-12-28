package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;

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
    private Subscriber<ThemePage> mainSubscriber = new Subscriber<>();
    private Subscriber<String> helperSubscriber = new Subscriber<>();
    TabLayout tabLayout;
    //Тег для вьюхи поиска. Чтобы создавались кнопки и т.д, только при вызове поиска, а не при каждом создании меню.
    private int searchViewTag = 0;
    private final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);

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
        /*webView.setClipToPadding(false);
        webView.setPadding(0, 0, 0, App.px64);*/
        webView.loadUrl("about:blank");
        refreshLayout.addView(webView);
        tabLayout = (TabLayout) inflater.inflate(R.layout.theme_toolbar, (ViewGroup) toolbar.getParent(), false);
        ((ViewGroup) toolbar.getParent()).addView(tabLayout, ((ViewGroup) toolbar.getParent()).indexOfChild(toolbar));
        tabLayout.addTab(tabLayout.newTab().setIcon(App.getAppDrawable(R.drawable.chevron_double_left)).setTag("first"));
        tabLayout.addTab(tabLayout.newTab().setIcon(App.getAppDrawable(R.drawable.chevron_left)).setTag("prev"));
        tabLayout.addTab(tabLayout.newTab().setText("Выбор").setTag("selectPage"));
        tabLayout.addTab(tabLayout.newTab().setIcon(App.getAppDrawable(R.drawable.chevron_right)).setTag("next"));
        tabLayout.addTab(tabLayout.newTab().setIcon(App.getAppDrawable(R.drawable.chevron_double_right)).setTag("last"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (refreshLayout.isRefreshing()) return;
                assert tab.getTag() != null;
                switch ((String) tab.getTag()) {
                    case "first":
                        firstPage();
                        break;
                    case "prev":
                        prevPage();
                        break;
                    case "selectPage":
                        selectPage();
                        break;
                    case "next":
                        nextPage();
                        break;
                    case "last":
                        lastPage();
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
        viewsReady();


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(App.px56 + App.px24);

        refreshLayout.setOnRefreshListener(this::loadData);

        webView.addJavascriptInterface(this, JS_INTERFACE);
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
            if (pageData.canQuote())
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
        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setVisibility(View.VISIBLE);
        return view;
    }


    public void refreshOptionsMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        menu.add("Обновить").setIcon(App.getAppDrawable(R.drawable.ic_refresh_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            action = REFRESH_ACTION;
            loadData();
            return false;
        })/*.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/;
        if (pageData != null) {
            menu.add("Ссылка").setOnMenuItemClickListener(menuItem -> {
                Utils.copyToClipBoard(getTabUrl());
                return false;
            });
            addSearchOnPageItem(menu);
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

    private void addSearchOnPageItem(Menu menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setTag(searchViewTag);

        searchView.setOnSearchClickListener(v -> {
            if (searchView.getTag().equals(searchViewTag)) {
                ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                if (searchClose != null)
                    ((ViewGroup) searchClose.getParent()).removeView(searchClose);

                ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(App.px48, App.px48);
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true);

                AppCompatImageButton btnNext = new AppCompatImageButton(searchView.getContext());
                btnNext.setImageDrawable(App.getAppDrawable(R.drawable.ic_search_next_gray_24dp));
                btnNext.setBackgroundResource(outValue.resourceId);

                AppCompatImageButton btnPrev = new AppCompatImageButton(searchView.getContext());
                btnPrev.setImageDrawable(App.getAppDrawable(R.drawable.ic_search_prev_gray_24dp));
                btnPrev.setBackgroundResource(outValue.resourceId);

                ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
                ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

                btnNext.setOnClickListener(v1 -> webView.findNext(true));
                btnPrev.setOnClickListener(v1 -> webView.findNext(false));
                searchViewTag++;
            }
        });

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                webView.findAllAsync(newText);
                return false;
            }
        });
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Theme().getPage(getTabUrl(), true), this::onLoadData, new ThemePage(), v -> loadData());
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
        updateFavorites(themePage);
        updateView();
        updateNavigation(themePage);
    }

    private void updateFavorites(ThemePage themePage) {
        if (themePage.getCurrentPage() < themePage.getAllPagesCount()) return;
        String tag = TabManager.getInstance().getTagContainClass(FavoritesFragment.class);
        if (tag == null) return;
        ((FavoritesFragment) TabManager.getInstance().get(tag)).markRead(themePage.getId());
    }

    private void updateNavigation(ThemePage themePage) {
        tabLayout.setVisibility(View.VISIBLE);
        boolean prevDisabled = themePage.getCurrentPage() <= 1;
        boolean nextDisabled = themePage.getCurrentPage() == themePage.getAllPagesCount();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == 2) continue;
            boolean b = i < 2 ? prevDisabled : nextDisabled;
            if (b)
                tabLayout.getTabAt(i).getIcon().setColorFilter(colorFilter);
            else
                tabLayout.getTabAt(i).getIcon().clearColorFilter();
        }
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
        if (toolbar.getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            toolbar.collapseActionView();
            return true;
        }
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
            if (checkIsPoll(uri.toString())) return true;
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
                            Matcher matcher = Theme.elemToScrollPattern.matcher(uri.toString());
                            String elem = null;
                            while (matcher.find()) {
                                elem = matcher.group(1);
                            }
                            Log.d("kek", " scroll to " + postId + " : " + elem);
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

        private boolean checkIsPoll(String url) {
            Matcher m = Pattern.compile("4pda.ru.*?addpoll=1").matcher(url);
            if (m.find()) {
                Uri uri = Uri.parse(url);
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", Integer.toString(pageData.getId()))
                        .appendQueryParameter("st", "" + pageData.getCurrentPage() * pageData.getPostsOnPageCount())
                        .build();
                load(uri);
                return true;
            }
            return false;
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

            Log.d("kek", "IThemeJ: " + url);
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
        helperSubscriber.subscribe(Api.Theme().reportPost(themeId, postId, message), s -> {
            Toast.makeText(getContext(), s.isEmpty() ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show();
        }, "", v -> doReportPost(themeId, postId, message));
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
        helperSubscriber.subscribe(Api.Theme().deletePost(postId), s -> {
            Toast.makeText(getContext(), !s.isEmpty() ? "Сообщение удалено" : "Ошибка", Toast.LENGTH_SHORT).show();
        }, "", v -> doDeletePost(postId));
    }

    //Изменение репутации сообщения
    public void votePost(ThemePost post, boolean type) {
        helperSubscriber.subscribe(Api.Theme().votePost(post.getId(), type), s -> {
            Toast.makeText(getContext(), s.isEmpty() ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show();
        }, "", v -> votePost(post, type));
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
        helperSubscriber.subscribe(Api.Theme().changeReputation(postId, userId, type, message), s -> {
            Toast.makeText(getContext(), s.isEmpty() ? "Репутация изменена" : s, Toast.LENGTH_SHORT).show();
        }, "error", v -> doChangeReputation(postId, userId, type, message));
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

    @JavascriptInterface
    public void showPollResults() {
        run(() -> {
            setTabUrl(getTabUrl().replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&mode=show&poll_open=true"));
            loadData();
        });

    }

    @JavascriptInterface
    public void showPoll() {
        run(() -> {
            setTabUrl(getTabUrl().replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&poll_open=true"));
            loadData();
        });

    }
}
