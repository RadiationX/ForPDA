package forpdateam.ru.forpda.ui.fragments.search;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.common.webview.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeDialogsHelper;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.ui.fragments.theme.ThemeHelper;
import forpdateam.ru.forpda.ui.fragments.theme.editpost.EditPostFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;
import forpdateam.ru.forpda.ui.views.FabOnScroll;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * Created by radiationx on 29.01.17.
 */

public class SearchFragment extends TabFragment implements IPostFunctions, ExtendedWebView.JsLifeCycleListener, SearchAdapter.OnItemClickListener<SearchItem> {
    private final static String LOG_TAG = SearchFragment.class.getSimpleName();
    protected final static String JS_INTERFACE = "ISearch";
    private boolean scrollButtonEnable = false;
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

    private ExtendedWebView webView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private SearchAdapter adapter = new SearchAdapter();
    private CustomWebViewClient webViewClient;

    private StringBuilder titleBuilder = new StringBuilder();
    private PaginationHelper paginationHelper;
    private DynamicDialogMenu<SearchFragment, IBaseForumPost> dialogMenu;

    private Observer searchPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Theme.SHOW_AVATARS: {
                updateShowAvatarState(Preferences.Theme.isShowAvatars(getContext()));
                break;
            }
            case Preferences.Theme.CIRCLE_AVATARS: {
                updateTypeAvatarState(Preferences.Theme.isCircleAvatars(getContext()));
                break;
            }
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));
            }
            case Preferences.Main.SCROLL_BUTTON_ENABLE: {
                scrollButtonEnable = Preferences.Main.isScrollButtonEnable(getContext());
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
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_search));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        scrollButtonEnable = Preferences.Main.isScrollButtonEnable(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String savedSettings = App.get().getPreferences().getString("search_settings", null);
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
        FabOnScroll behavior = new FabOnScroll(fab.getContext());
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

        baseInflateFragment(inflater, R.layout.fragment_search);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
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
        attachWebView(webView);
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        refreshLayout.addView(recyclerView);

        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());

        contentController.setMainRefresh(refreshLayout);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();

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

        webView.setJsLifeCycleListener(this);
        webView.addJavascriptInterface(this, ThemeFragmentWeb.JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.addJavascriptInterface(this, JS_POSTS_FUNCTIONS);
        webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));

        fab.setSize(FloatingActionButton.SIZE_MINI);
        if (scrollButtonEnable) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setScaleX(0.0f);
        fab.setScaleY(0.0f);
        fab.setAlpha(0.0f);

        setCardsBackground();
        App.get().addPreferenceChangeObserver(searchPreferenceObserver);

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

        searchView.setQueryHint(getString(R.string.search_keywords));

        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchEditFrame.getLayoutParams();
        params.leftMargin = 0;

        View searchSrcText = searchView.findViewById(R.id.search_src_text);
        searchSrcText.setPadding(0, searchSrcText.getPaddingTop(), 0, searchSrcText.getPaddingBottom());


        fillSettingsData();
        searchItem.expandActionView();
        submitButton.setOnClickListener(v -> startSearch());
        saveSettingsButton.setOnClickListener(v -> saveSettings());
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);
        recyclerView.setAdapter(adapter);
        refreshLayoutStyle(refreshLayout);
        refreshLayoutLongTrigger(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        adapter.setOnItemClickListener(this);

        if (App.get().getPreferences().getBoolean("search.tooltip.settings", true)) {
            for (int toolbarChildIndex = 0; toolbarChildIndex < toolbar.getChildCount(); toolbarChildIndex++) {
                View childView = toolbar.getChildAt(toolbarChildIndex);
                if (childView instanceof ActionMenuView) {
                    ActionMenuView menuView = (ActionMenuView) childView;
                    for (int menuChildIndex = 0; menuChildIndex < menuView.getChildCount(); menuChildIndex++) {
                        try {
                            ActionMenuItemView itemView = (ActionMenuItemView) menuView.getChildAt(menuChildIndex);
                            if (settingsMenuItem == itemView.getItemData()) {
                                tooltip = new SimpleTooltip.Builder(getContext())
                                        .anchorView(itemView)
                                        .text(R.string.tooltip_search_settings)
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

            App.get().getPreferences().edit().putBoolean("search.tooltip.settings", false).apply();
        }


    }

    private MenuItem settingsMenuItem;

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    Utils.copyToClipBoard(settings.toUrl());
                    return false;
                });
        toolbar.inflateMenu(R.menu.qms_contacts_menu);

        settingsMenuItem = menu.add(R.string.settings)
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

        searchItem = menu.findItem(R.id.action_search);
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
        App.get().getPreferences().edit().putString("search_settings", saveUrl).apply();
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
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        if (settings.getQuery().isEmpty() && settings.getNick().isEmpty()) {
            return true;
        }
        buildTitle();
        hidePopupWindows();
        /*if (searchSettingsView.getVisibility() == View.VISIBLE) {
            searchSettingsView.setVisibility(View.GONE);
        }*/
        setRefreshing(true);
        boolean withHtml = settings.getResult().equals(SearchSettings.RESULT_POSTS.first) && settings.getResourceType().equals(SearchSettings.RESOURCE_FORUM.first);
        subscribe(RxApi.Search().getSearch(settings, withHtml), this::onLoadData, new SearchResult(), v -> loadData());
        return true;
    }

    private void onLoadData(SearchResult searchResult) {
        setRefreshing(false);
        recyclerView.scrollToPosition(0);
        hidePopupWindows();
        data = searchResult;
        Log.d("SUKA", "SEARCH SIZE " + searchResult.getItems().size());
        if (data.getItems().isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_search)
                        .setTitle(R.string.funny_search_nodata_title)
                        .setDesc(R.string.funny_search_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        Log.d("SUKA", "" + data.getSettings().getResult() + " : " + data.getSettings().getResourceType());
        if (data.getSettings().getResult().equals(SearchSettings.RESULT_POSTS.first) && data.getSettings().getResourceType().equals(SearchSettings.RESOURCE_FORUM.first)) {
            for (int i = 0; i < refreshLayout.getChildCount(); i++) {
                if (refreshLayout.getChildAt(i) instanceof RecyclerView) {
                    refreshLayout.removeViewAt(i);
                    fixTargetView();
                    break;
                }
            }
            if (refreshLayout.getChildCount() <= 1) {
                if (scrollButtonEnable) {
                    fab.setVisibility(View.VISIBLE);
                }
                refreshLayout.addView(webView);
                Log.d(LOG_TAG, "add webview");
            }
            if (webViewClient == null) {
                webViewClient = new CustomWebViewClient();
                webView.setWebViewClient(webViewClient);
                webView.setWebChromeClient(new CustomWebChromeClient());
            }
            Log.d("SUKA", "SEARCH SHOW WEBVIEW");
            webView.loadDataWithBaseURL("https://4pda.ru/forum/", data.getHtml(), "text/html", "utf-8", null);
        } else {
            for (int i = 0; i < refreshLayout.getChildCount(); i++) {
                if (refreshLayout.getChildAt(i) instanceof ExtendedWebView) {
                    refreshLayout.removeViewAt(i);
                    fixTargetView();
                }
            }
            if (refreshLayout.getChildCount() <= 1) {
                fab.setVisibility(View.GONE);
                refreshLayout.addView(recyclerView);
                Log.d(LOG_TAG, "add recyclerview");
            }
            Log.d("SUKA", "SEARCH SHOW RECYCLERVIEW");
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
        App.get().removePreferenceChangeObserver(searchPreferenceObserver);
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_POSTS_FUNCTIONS);
        webView.removeJavascriptInterface(ThemeFragmentWeb.JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
        actions.add("window.scrollTo(0, 0);");
    }

    @Override
    public void onItemClick(SearchItem item) {
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
    }

    @Override
    public boolean onItemLongClick(SearchItem item) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.topic_to_begin), (context, data1) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId());
            });
            dialogMenu.addItem(getString(R.string.topic_newposts), (context, data1) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId() + "&view=getnewpost");
            });
            dialogMenu.addItem(getString(R.string.topic_lastposts), (context, data1) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + data1.getTopicId() + "&view=getlastpost");
            });
            dialogMenu.addItem(getString(R.string.copy_link), (context, data1) -> {
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
            dialogMenu.addItem(getString(R.string.open_theme_forum), (context, data1) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + data1.getForumId()));
            dialogMenu.addItem(getString(R.string.add_to_favorites), ((context, data1) -> {
                FavoritesHelper.addWithDialog(getContext(), aBoolean -> {
                    Toast.makeText(getContext(), getString(aBoolean ? R.string.favorites_added : R.string.error_occurred), Toast.LENGTH_SHORT).show();
                }, data1.getId());
            }));
        }
        dialogMenu.disallowAll();
        if (settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first)) {
            dialogMenu.allow(3);
        } else {
            dialogMenu.allowAll();
        }
        dialogMenu.show(getContext(), SearchFragment.this, item);
        return false;
    }

    @JavascriptInterface
    public void firstPage() {
        if (getContext() == null)
            return;
        runInUiThread(() -> paginationHelper.firstPage());
    }

    @JavascriptInterface
    public void prevPage() {
        if (getContext() == null)
            return;
        runInUiThread(() -> paginationHelper.prevPage());
    }

    @JavascriptInterface
    public void nextPage() {
        if (getContext() == null)
            return;
        runInUiThread(() -> paginationHelper.nextPage());
    }

    @JavascriptInterface
    public void lastPage() {
        if (getContext() == null)
            return;
        runInUiThread(() -> paginationHelper.lastPage());
    }

    @JavascriptInterface
    public void selectPage() {
        if (getContext() == null)
            return;
        runInUiThread(() -> paginationHelper.selectPageDialog());
    }


    public IBaseForumPost getPostById(int postId) {
        for (IBaseForumPost post : data.getItems())
            if (post.getId() == postId)
                return post;
        return null;
    }

    @JavascriptInterface
    public void showUserMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> showUserMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showReputationMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> showReputationMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void showPostMenu(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> showPostMenu(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void reply(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> reply(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void quotePost(final String text, final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> quotePost(text, getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void deletePost(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> deletePost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void editPost(final String postId) {
        if (getContext() == null)
            return;
        runInUiThread(() -> editPost(getPostById(Integer.parseInt(postId))));
    }

    @JavascriptInterface
    public void votePost(final String postId, final boolean type) {
        if (getContext() == null)
            return;
        runInUiThread(() -> votePost(getPostById(Integer.parseInt(postId)), type));
    }

    @Override
    public void showUserMenu(IBaseForumPost post) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.showUserMenu(getContext(), this, post);
    }

    @Override
    public void showReputationMenu(IBaseForumPost post) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.showReputationMenu(getContext(), this, post);
    }

    @Override
    public void showPostMenu(IBaseForumPost post) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.showPostMenu(getContext(), this, post);
    }

    @Override
    public void reportPost(IBaseForumPost post) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.tryReportPost(getContext(), post);
    }

    @Override
    public void reply(IBaseForumPost post) {
        if (getContext() == null)
            return;
        Toast.makeText(getContext(), R.string.action_not_available, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void quotePost(String text, IBaseForumPost post) {
        if (getContext() == null)
            return;
        Toast.makeText(getContext(), R.string.action_not_available, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deletePost(IBaseForumPost post) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.deletePost(getContext(), post, aBoolean -> {
            if (aBoolean)
                webView.evalJs("deletePost(" + post.getId() + ");");
        });
    }

    @Override
    public void editPost(IBaseForumPost post) {
        String title;
        if (post instanceof SearchItem) {
            SearchItem item = (SearchItem) post;
            title = item.getTitle();
        } else {
            title = "пост из поиска_";
        }
        TabManager.get().add(EditPostFragment.newInstance(post.getId(), post.getTopicId(), post.getForumId(), 0, title));
    }

    @Override
    public void votePost(IBaseForumPost post, boolean type) {
        if (getContext() == null)
            return;
        ThemeHelper.votePost(s -> toast(s.isEmpty() ? getString(R.string.unknown_error) : s), post.getId(), type);
    }

    @Override
    public void changeReputation(IBaseForumPost post, boolean type) {
        if (getContext() == null)
            return;
        ThemeDialogsHelper.changeReputation(getContext(), post, type);
    }

    @JavascriptInterface
    public void toast(final String text) {
        if (getContext() == null)
            return;
        runInUiThread(() -> Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show());
    }
}
