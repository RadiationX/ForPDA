package forpdateam.ru.forpda.fragments.search;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.fragments.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.fragments.theme.ThemeDialogsHelper;
import forpdateam.ru.forpda.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.fragments.theme.ThemeHelper;
import forpdateam.ru.forpda.fragments.theme.editpost.EditPostFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.FabOnScroll;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ExtendedWebView;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * Created by radiationx on 29.01.17.
 */

public class SearchFragment extends TabFragment implements IPostFunctions, ExtendedWebView.JsLifeCycleListener {
    private final static String LOG_TAG = SearchFragment.class.getSimpleName();
    protected final static String JS_INTERFACE = "ISearch";
    private boolean scrollButtonEnable = Preferences.Main.isScrollButtonEnable();
    private ViewGroup searchSettingsView;
    private ViewGroup nickBlock, resourceBlock, resultBlock, sortBlock, sourceBlock;
    private Spinner resourceSpinner, resultSpinner, sortSpinner, sourceSpinner;
    private TextView nickField;
    private Button submitButton, saveSettingsButton;

    private List<String> resourceItems = Arrays.asList(SearchSettings.RESOURCE_FORUM.second, SearchSettings.RESOURCE_NEWS.second);
    private List<String> resultItems = Arrays.asList(SearchSettings.RESULT_TOPICS.second, SearchSettings.RESULT_POSTS.second);
    private List<String> sortItems = Arrays.asList(SearchSettings.SORT_DA.second, SearchSettings.SORT_DD.second, SearchSettings.SORT_REL.second);
    private List<String> sourceItems = Arrays.asList(SearchSettings.SOURCE_ALL.second, SearchSettings.SOURCE_TITLES.second, SearchSettings.SOURCE_CONTENT.second);

    private SearchSettings settings = new SearchSettings();

    private Subscriber<SearchResult> mainSubscriber = new Subscriber<>(this);
    private ExtendedWebView webView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private SearchAdapter adapter = new SearchAdapter();
    private SearchWebViewClient webViewClient;

    private StringBuilder titleBuilder = new StringBuilder();
    private PaginationHelper paginationHelper;
    private AlertDialogMenu<SearchFragment, IBaseForumPost> createdTopicsDialogMenu, tempTopicsDialogMenu;

    private Observer searchPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Theme.SHOW_AVATARS: {
                updateShowAvatarState(Preferences.Theme.isShowAvatars());
                break;
            }
            case Preferences.Theme.CIRCLE_AVATARS: {
                updateTypeAvatarState(Preferences.Theme.isCircleAvatars());
                break;
            }
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize());
            }
            case Preferences.Main.SCROLL_BUTTON_ENABLE: {
                scrollButtonEnable = Preferences.Main.isScrollButtonEnable();
                if (scrollButtonEnable) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        }
    };

    protected void updateShowAvatarState(boolean isShow) {
        webView.evalJs("updateShowAvatarState(" + isShow + ")");
    }

    protected void updateTypeAvatarState(boolean isCircle) {
        webView.evalJs("updateTypeAvatarState(" + isCircle + ")");
    }

    public SearchFragment() {
        configuration.setDefaultTitle("Поиск");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String savedSettings = App.getInstance().getPreferences().getString("search_settings", null);
        if (savedSettings != null) {
            settings = SearchSettings.parseSettings(settings, savedSettings);
        }
        if (getArguments() != null) {
            settings = SearchSettings.fromBundle(settings, getArguments());
        }
    }

    private SearchView searchView;
    private MenuItem searchItem;
    private SearchResult data;
    private BottomSheetDialog dialog;
    private SimpleTooltip tooltip;


    @Override
    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FabOnScroll behavior = new FabOnScroll(fab.getContext(), null);
        params.setBehavior(behavior);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16);
        }
        fab.requestLayout();
    }

    @SuppressLint("JavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initFabBehavior();
        fab.setSize(FloatingActionButton.SIZE_MINI);
        if (scrollButtonEnable) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setScaleX(0.0f);
        fab.setScaleY(0.0f);
        fab.setAlpha(0.0f);
        baseInflateFragment(inflater, R.layout.fragment_search);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        //recyclerView = (RecyclerView) findViewById(R.id.base_list);
        searchSettingsView = (ViewGroup) View.inflate(getContext(), R.layout.search_settings, null);

        nickBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_nick_block);
        resourceBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_resource_block);
        resultBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_result_block);
        sortBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_sort_block);
        sourceBlock = (ViewGroup) searchSettingsView.findViewById(R.id.search_source_block);

        resourceSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_resource_spinner);
        resultSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_result_spinner);
        sortSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_sort_spinner);
        sourceSpinner = (Spinner) searchSettingsView.findViewById(R.id.search_source_spinner);

        nickField = (TextView) searchSettingsView.findViewById(R.id.search_nick_field);

        submitButton = (Button) searchSettingsView.findViewById(R.id.search_submit);
        saveSettingsButton = (Button) searchSettingsView.findViewById(R.id.search_save_settings);

        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        webView.setJsLifeCycleListener(this);
        webView.addJavascriptInterface(this, ThemeFragmentWeb.JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_POSTS_FUNCTIONS);
        webView.setRelativeFontSize(Preferences.Main.getWebViewSize());
        recyclerView = new RecyclerView(getContext());

        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        fab.setOnClickListener(v -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true);
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true);
            }
        });
        webView.setOnDirectionListener(direction -> {
            if (webView.getDirection() == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getAppDrawable(fab.getContext(), R.drawable.ic_arrow_down));
            } else if (webView.getDirection() == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getAppDrawable(fab.getContext(), R.drawable.ic_arrow_up));
            }
        });

        refreshLayout.addView(recyclerView);
        viewsReady();
        setCardsBackground();
        App.getInstance().addPreferenceChangeObserver(searchPreferenceObserver);
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout);
        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                settings.setSt(pageNumber);
                loadData();
            }
        });

        //searchSettingsView.setVisibility(View.GONE);
        dialog = new BottomSheetDialog(getContext());
        //dialog.setPeekHeight(App.getKeyboardHeight());
        //dialog.getWindow().getDecorView().setFitsSystemWindows(true);


        setItems(resourceSpinner, (String[]) resourceItems.toArray(), 0);
        setItems(resultSpinner, (String[]) resultItems.toArray(), 0);
        setItems(sortSpinner, (String[]) sortItems.toArray(), 0);
        setItems(sourceSpinner, (String[]) sourceItems.toArray(), 1);

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setQueryHint("Ключевые слова");
        fillSettingsData();
        searchItem.expandActionView();
        submitButton.setOnClickListener(v -> startSearch());
        saveSettingsButton.setOnClickListener(v -> saveSettings());
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        adapter.setOnItemClickListener(item -> {
            String url = "";
            if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
                url = "https://4pda.ru/index.php?p=" + item.getId();
            } else {
                url = "https://4pda.ru/forum/index.php?showtopic=" + item.getTopicId();
                if (item.getId() != 0) {
                    url += "&view=findpost&p=" + item.getId();
                }
            }
            IntentHandler.handle(url);
        });
        adapter.setOnLongItemClickListener(item -> {
            if (createdTopicsDialogMenu == null) {
                createdTopicsDialogMenu = new AlertDialogMenu<>();
                tempTopicsDialogMenu = new AlertDialogMenu<>();
                createdTopicsDialogMenu.addItem("К первому", (context, data1) -> {
                    IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId());
                });
                createdTopicsDialogMenu.addItem("К непрочитанному", (context, data1) -> {
                    IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId() + "&view=getnewpost");
                });
                createdTopicsDialogMenu.addItem("К последнему", (context, data1) -> {
                    IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId() + "&view=getlastpost");
                });
                createdTopicsDialogMenu.addItem("Скопировать ссылку", (context, data1) -> {
                    String url = "";
                    if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
                        url = "https://4pda.ru/index.php?p=" + item.getId();
                    } else {
                        url = "https://4pda.ru/forum/index.php?showtopic=" + item.getTopicId();
                        if (item.getId() != 0) {
                            url += "&view=findpost&p=" + item.getId();
                        }
                    }
                    Utils.copyToClipBoard(url);
                });
                createdTopicsDialogMenu.addItem("Открыть форум темы", (context, data1) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + data1.getForumId()));
                createdTopicsDialogMenu.addItem("Добавить в избранное", ((context, data1) -> {
                    new AlertDialog.Builder(context.getContext())
                            .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> {
                                FavoritesHelper.add(aBoolean -> {
                                    Toast.makeText(getContext(), aBoolean ? "Тема добавлена в избранное" : "Ошибочка вышла", Toast.LENGTH_SHORT).show();
                                }, data1.getId(), Favorites.SUB_TYPES[which1]);
                            })
                            .show();
                }));
            }
            tempTopicsDialogMenu.clear();
            if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
                tempTopicsDialogMenu.addItem(createdTopicsDialogMenu.get(3));
            } else {
                tempTopicsDialogMenu.addItems(createdTopicsDialogMenu.getItems());
            }

            new AlertDialog.Builder(getContext())
                    .setItems(tempTopicsDialogMenu.getTitles(), (dialog, which) -> tempTopicsDialogMenu.onClick(which, SearchFragment.this, item))
                    .show();
        });

        if (App.getInstance().getPreferences().getBoolean("search.tooltip.settings", true)) {
            for (int toolbarChildIndex = 0; toolbarChildIndex < toolbar.getChildCount(); toolbarChildIndex++) {
                View view = toolbar.getChildAt(toolbarChildIndex);
                if (view instanceof ActionMenuView) {
                    ActionMenuView menuView = (ActionMenuView) view;
                    for (int menuChildIndex = 0; menuChildIndex < menuView.getChildCount(); menuChildIndex++) {
                        try {
                            ActionMenuItemView itemView = (ActionMenuItemView) menuView.getChildAt(menuChildIndex);
                            if (settingsMenuItem == itemView.getItemData()) {
                                tooltip = new SimpleTooltip.Builder(getContext())
                                        .anchorView(itemView)
                                        .text("Нажимите сюда, чтобы настроить поисковой запрос")
                                        .gravity(Gravity.BOTTOM)
                                        .animated(false)
                                        .modal(true)
                                        .transparentOverlay(false)
                                        .backgroundColor(Color.BLACK)
                                        .textColor(Color.WHITE)
                                        .padding((float) App.px16)
                                        .build();
                                tooltip.show();
                                break;
                            }
                        } catch (ClassCastException ignore) {
                        }
                    }
                    break;
                }
            }

            App.getInstance().getPreferences().edit().putBoolean("search.tooltip.settings", false).apply();
        }


        return view;
    }

    private MenuItem settingsMenuItem;

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add("Скопировать ссылку")
                .setOnMenuItemClickListener(menuItem -> {
                    Utils.copyToClipBoard(settings.toUrl());
                    return false;
                });
        toolbar.inflateMenu(R.menu.qms_contacts_menu);

        settingsMenuItem = getMenu().add("Настройки")
                .setIcon(R.drawable.ic_toolbar_tune).setOnMenuItemClickListener(menuItem -> {
                    hidePopupWindows();
                    if (searchSettingsView != null && searchSettingsView.getParent() != null && searchSettingsView.getParent() instanceof ViewGroup) {
                        ((ViewGroup) searchSettingsView.getParent()).removeView(searchSettingsView);
                    }
                    if (searchSettingsView != null) {
                        dialog.setContentView(searchSettingsView);
                        dialog.show();
                    }
                    return false;
                });
        settingsMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        searchItem = getMenu().findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(true);
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (tooltip != null && tooltip.isShowing()) {
            tooltip.dismiss();
            return true;
        }
        return super.onBackPressed();
    }

    private boolean checkArg(String arg, Pair<String, String> pair) {
        return arg.equals(pair.first);
    }

    private boolean checkName(String arg, Pair<String, String> pair) {
        return arg.equals(pair.second);
    }

    private void setSelection(Spinner spinner, List<String> items, Pair<String, String> pair) {
        spinner.setSelection(items.indexOf(pair.second));
    }

    private void fillSettingsData() {

        searchView.post(() -> {
            searchView.setQuery(settings.getQuery(), false);
        });

        nickField.setText(settings.getNick());

        if (checkArg(settings.getResourceType(), SearchSettings.RESOURCE_NEWS)) {
            setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_NEWS);
        } else if (checkArg(settings.getResourceType(), SearchSettings.RESOURCE_FORUM)) {
            setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_FORUM);
        }

        if (checkArg(settings.getResult(), SearchSettings.RESULT_TOPICS)) {
            setSelection(resultSpinner, resultItems, SearchSettings.RESULT_TOPICS);
        } else if (checkArg(settings.getResult(), SearchSettings.RESULT_POSTS)) {
            setSelection(resultSpinner, resultItems, SearchSettings.RESULT_POSTS);
        }

        if (checkArg(settings.getSort(), SearchSettings.SORT_DA)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_DA);
        } else if (checkArg(settings.getSort(), SearchSettings.SORT_DD)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_DD);
        } else if (checkArg(settings.getSort(), SearchSettings.SORT_REL)) {
            setSelection(sortSpinner, sortItems, SearchSettings.SORT_REL);
        }

        if (checkArg(settings.getSource(), SearchSettings.SOURCE_ALL)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_ALL);
        } else if (checkArg(settings.getSource(), SearchSettings.SOURCE_TITLES)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_TITLES);
        } else if (checkArg(settings.getSource(), SearchSettings.SOURCE_CONTENT)) {
            setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_CONTENT);
        }
    }

    private AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String arg;
            if (parent == resourceSpinner) {
                arg = resourceItems.get(position);
                if (checkName(arg, SearchSettings.RESOURCE_NEWS)) {
                    settings.setResourceType(SearchSettings.RESOURCE_NEWS.first);
                    setNewsMode();
                } else if (checkName(arg, SearchSettings.RESOURCE_FORUM)) {
                    settings.setResourceType(SearchSettings.RESOURCE_FORUM.first);
                    setForumMode();
                }
            } else if (parent == resultSpinner) {
                arg = resultItems.get(position);
                if (checkName(arg, SearchSettings.RESULT_TOPICS)) {
                    settings.setResult(SearchSettings.RESULT_TOPICS.first);
                } else if (checkName(arg, SearchSettings.RESULT_POSTS)) {
                    settings.setResult(SearchSettings.RESULT_POSTS.first);
                }
            } else if (parent == sortSpinner) {
                arg = sortItems.get(position);
                if (checkName(arg, SearchSettings.SORT_DA)) {
                    settings.setSort(SearchSettings.SORT_DA.first);
                } else if (checkName(arg, SearchSettings.SORT_DD)) {
                    settings.setSort(SearchSettings.SORT_DD.first);
                } else if (checkName(arg, SearchSettings.SORT_REL)) {
                    settings.setSort(SearchSettings.SORT_REL.first);
                }
            } else if (parent == sourceSpinner) {
                arg = sourceItems.get(position);
                if (checkName(arg, SearchSettings.SOURCE_ALL)) {
                    settings.setSource(SearchSettings.SOURCE_ALL.first);
                } else if (checkName(arg, SearchSettings.SOURCE_TITLES)) {
                    settings.setSource(SearchSettings.SOURCE_TITLES.first);
                } else if (checkName(arg, SearchSettings.SOURCE_CONTENT)) {
                    settings.setSource(SearchSettings.SOURCE_CONTENT.first);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void setNewsMode() {
        nickBlock.setVisibility(View.GONE);
        resultBlock.setVisibility(View.GONE);
        sortBlock.setVisibility(View.GONE);
        sourceBlock.setVisibility(View.GONE);
    }

    private void setForumMode() {
        nickBlock.setVisibility(View.VISIBLE);
        resultBlock.setVisibility(View.VISIBLE);
        sortBlock.setVisibility(View.VISIBLE);
        sourceBlock.setVisibility(View.VISIBLE);
    }

    private void setItems(Spinner spinner, String[] items, int selection) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getMainActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selection);
        spinner.setOnItemSelectedListener(listener);
    }

    private void saveSettings() {
        SearchSettings saveSettings = new SearchSettings();
        saveSettings.setResult(settings.getResult());
        saveSettings.setSort(settings.getSort());
        saveSettings.setSource(settings.getSource());
        String saveUrl = saveSettings.toUrl();
        Log.d(LOG_TAG, "SAVE SETTINGS " + saveUrl);
        App.getInstance().getPreferences().edit().putString("search_settings", saveUrl).apply();
    }

    private void startSearch() {
        settings.setSt(0);
        settings.setQuery(searchView.getQuery().toString());
        settings.setNick(nickField.getText().toString());
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        loadData();
    }

    private void buildTitle() {
        titleBuilder.setLength(0);
        titleBuilder.append("Поиск");
        if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
            titleBuilder.append(" новостей");
        } else {
            if (settings.getResult().equals(SearchSettings.RESULT_POSTS.first)) {
                titleBuilder.append(" сообщений");
            } else {
                titleBuilder.append(" тем");
            }
            if (!settings.getNick().isEmpty()) {
                titleBuilder.append(" пользователя \"").append(settings.getNick()).append("\"");
            }
        }
        if (!settings.getQuery().isEmpty()) {
            titleBuilder.append(" по запросу \"").append(settings.getQuery()).append("\"");
        }
        setTitle(titleBuilder.toString());
    }

    @Override
    public void loadData() {
        super.loadData();
        if (settings.getQuery().isEmpty() && settings.getNick().isEmpty()) {
            return;
        }
        buildTitle();
        hidePopupWindows();
        /*if (searchSettingsView.getVisibility() == View.VISIBLE) {
            searchSettingsView.setVisibility(View.GONE);
        }*/
        refreshLayout.setRefreshing(true);
        boolean withHtml = settings.getResult().equals(SearchSettings.RESULT_POSTS.first) && settings.getResourceType().equals(SearchSettings.RESOURCE_FORUM.first);
        mainSubscriber.subscribe(RxApi.Search().getSearch(settings, withHtml), this::onLoadData, new SearchResult(), v -> loadData());
    }

    private void onLoadData(SearchResult searchResult) {
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        hidePopupWindows();
        data = searchResult;
        /*if (refreshLayout.getChildCount() > 1) {
            Log.d("FORPDA_LOG", "" + refreshLayout.getChildAt(0));
        }*/
        if (data.getSettings().getResult().equals(SearchSettings.RESULT_POSTS.first) && data.getSettings().getResourceType().equals(SearchSettings.RESOURCE_FORUM.first)) {
            if (refreshLayout.getChildCount() > 1) {
                if (refreshLayout.getChildAt(0) instanceof RecyclerView) {
                    refreshLayout.removeViewAt(0);
                    fixTargetView();
                    if (scrollButtonEnable) {
                        fab.setVisibility(View.VISIBLE);
                    }
                    refreshLayout.addView(webView);
                    Log.d(LOG_TAG, "add webview");
                }
            } else {
                if (scrollButtonEnable) {
                    fab.setVisibility(View.VISIBLE);
                }
                refreshLayout.addView(webView);
                Log.d(LOG_TAG, "add webview");
            }
            if (webViewClient == null) {
                webViewClient = new SearchWebViewClient();
                webView.setWebViewClient(webViewClient);
            }
            webView.loadDataWithBaseURL("https://4pda.ru/forum/", data.getHtml(), "text/html", "utf-8", null);
        } else {
            if (refreshLayout.getChildCount() > 1) {
                if (refreshLayout.getChildAt(0) instanceof ExtendedWebView) {
                    refreshLayout.removeViewAt(0);
                    fixTargetView();
                    fab.setVisibility(View.GONE);
                    refreshLayout.addView(recyclerView);
                    Log.d(LOG_TAG, "add recyclerview");
                }
            } else {
                fab.setVisibility(View.GONE);
                refreshLayout.addView(recyclerView);
                Log.d(LOG_TAG, "add recyclerview");
            }
            adapter.clear();
            adapter.addAll(data.getItems());
        }

        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
    }


    //Поле mTarget это вьюха, от которой зависит обработка движений
    private void fixTargetView() {
        try {
            Field field = refreshLayout.getClass().getDeclaredField("mTarget");
            field.setAccessible(true);
            field.set(refreshLayout, null);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.getInstance().removePreferenceChangeObserver(searchPreferenceObserver);
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_POSTS_FUNCTIONS);
        webView.removeJavascriptInterface(ThemeFragmentWeb.JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.destroy();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
    }

    private class SearchWebViewClient extends WebViewClient {

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
            IntentHandler.handle(uri.toString());
            return true;
        }
    }


    @JavascriptInterface
    public void firstPage() {
        webView.runInUiThread(() -> paginationHelper.firstPage());
    }

    @JavascriptInterface
    public void prevPage() {
        webView.runInUiThread(() -> paginationHelper.prevPage());
    }

    @JavascriptInterface
    public void nextPage() {
        webView.runInUiThread(() -> paginationHelper.nextPage());
    }

    @JavascriptInterface
    public void lastPage() {
        webView.runInUiThread(() -> paginationHelper.lastPage());
    }

    @JavascriptInterface
    public void selectPage() {
        webView.runInUiThread(() -> paginationHelper.selectPageDialog());
    }


    public IBaseForumPost getPostById(int postId) {
        for (IBaseForumPost post : data.getItems())
            if (post.getId() == postId)
                return post;
        return null;
    }

    @JavascriptInterface
    public void showUserMenu(final String postId) {
        webView.runInUiThread(() -> showUserMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showReputationMenu(final String postId) {
        webView.runInUiThread(() -> showReputationMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showPostMenu(final String postId) {
        webView.runInUiThread(() -> showPostMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void insertNick(final String postId) {
        webView.runInUiThread(() -> insertNick(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void quotePost(final String text, final String postId) {
        webView.runInUiThread(() -> quotePost(text, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void deletePost(final String postId) {
        webView.runInUiThread(() -> deletePost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void editPost(final String postId) {
        webView.runInUiThread(() -> editPost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void votePost(final String postId, final boolean type) {
        webView.runInUiThread(() -> votePost(getPostById(Integer.parseInt(postId)), type));
    }

    @Override
    public void showUserMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showUserMenu(getContext(), this, post);
    }

    @Override
    public void showReputationMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showReputationMenu(getContext(), this, post);
    }

    @Override
    public void showPostMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showPostMenu(getContext(), this, post);
    }

    @Override
    public void reportPost(IBaseForumPost post) {
        ThemeDialogsHelper.tryReportPost(getContext(), post);
    }

    @Override
    public void insertNick(IBaseForumPost post) {
        Toast.makeText(getContext(), "Действие невозможно", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void quotePost(String text, IBaseForumPost post) {
        Toast.makeText(getContext(), "Действие невозможно", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deletePost(IBaseForumPost post) {
        ThemeDialogsHelper.deletePost(getContext(), post, aBoolean -> {
            if (aBoolean)
                webView.evalJs("deletePost(" + post.getId() + ");");
        });
    }

    @Override
    public void editPost(IBaseForumPost post) {
        TabManager.getInstance().add(EditPostFragment.newInstance(post.getId(), post.getTopicId(), post.getForumId(), 0, "?Сообщение из поиска?"));
    }

    @Override
    public void votePost(IBaseForumPost post, boolean type) {
        ThemeHelper.votePost(s -> toast(s.isEmpty() ? "Неизвестная ошибка" : s), post.getId(), type);
    }

    @Override
    public void changeReputation(IBaseForumPost post, boolean type) {
        ThemeDialogsHelper.changeReputation(getContext(), post, type);
    }

    @JavascriptInterface
    public void toast(final String text) {
        webView.runInUiThread(() -> Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show());
    }
}
