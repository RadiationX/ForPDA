package forpdateam.ru.forpda.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ScrollAwareFABBehavior;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.views.ContentController;
import io.reactivex.disposables.CompositeDisposable;

import static android.content.Context.ACCESSIBILITY_SERVICE;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabFragment extends Fragment {
    private final static String LOG_TAG = TabFragment.class.getSimpleName();
    public final static String ARG_TITLE = "TAB_TITLE";
    public final static String TAB_SUBTITLE = "TAB_SUBTITLE";
    public final static String ARG_TAB = "TAB_URL";
    private final static String BUNDLE_PREFIX = "tab_fragment_";
    private final static String BUNDLE_TITLE = "title";
    private final static String BUNDLE_TAB_TITLE = "tab_title";
    private final static String BUNDLE_SUBTITLE = "subtitle";
    private final static String BUNDLE_PARENT_TAG = "parent_tag";

    public final static int REQUEST_PICK_FILE = 1228;
    public final static int REQUEST_SAVE_FILE = 1117;
    public final static int REQUEST_STORAGE = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Thread mUiThread;

    protected TabConfiguration configuration = new TabConfiguration();

    private String title = null, tabTitle = null, subtitle = null, parentTag = null;

    protected ProgressBar toolbarProgress;
    protected RelativeLayout fragmentContainer;
    protected ViewGroup fragmentContent;
    protected ViewGroup additionalContent;
    protected ProgressBar contentProgress;
    protected LinearLayout noNetwork, titlesWrapper;
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout appBarLayout;
    protected CollapsingToolbarLayout toolbarLayout;
    protected Toolbar toolbar;
    protected ImageView toolbarBackground, toolbarImageView;
    protected TextView toolbarTitleView, toolbarSubtitleView;
    protected Spinner toolbarSpinner;
    protected View view, notifyDot;
    protected FloatingActionButton fab;
    private boolean showNotifyDot = Preferences.Main.isShowNotifyDot();
    private boolean notifyDotFav = Preferences.Main.isShowNotifyDotFav();
    private boolean notifyDotQms = Preferences.Main.isShowNotifyDotQms();
    private boolean notifyDotMentions = Preferences.Main.isShowNotifyDotMentions();
    private boolean alreadyCallLoad = false;
    protected ContentController contentController;

    protected CompositeDisposable disposable = new CompositeDisposable();

    protected Observer countsObserver = (observable, o) -> updateNotifyDot();
    protected Observer networkObserver = (observable, o) -> {
        if (o == null)
            o = true;
        if ((!configuration.isUseCache() || noNetwork.getVisibility() == View.VISIBLE) && (boolean) o) {
            if (!alreadyCallLoad)
                loadData();
            noNetwork.setVisibility(View.GONE);
        }
    };
    protected Observer tabPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.SHOW_NOTIFY_DOT: {
                showNotifyDot = Preferences.Main.isShowNotifyDot();
                updateNotifyDot();
                break;
            }
            case Preferences.Main.NOTIFY_DOT_FAV: {
                notifyDotFav = Preferences.Main.isShowNotifyDotFav();
                updateNotifyDot();
                break;
            }
            case Preferences.Main.NOTIFY_DOT_QMS: {
                notifyDotQms = Preferences.Main.isShowNotifyDotQms();
                updateNotifyDot();
                break;
            }
            case Preferences.Main.NOTIFY_DOT_MENTIONS: {
                notifyDotMentions = Preferences.Main.isShowNotifyDotMentions();
                updateNotifyDot();
                break;
            }
        }
    };
    private Observer statusBarSizeObserver = (observable1, o) -> {
        if (notifyDot != null) {
            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) notifyDot.getLayoutParams();
            params.topMargin = App.getStatusBarHeight();
            notifyDot.setLayoutParams(params);
        }
        if (toolbar != null) {
            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
            params.topMargin = App.getStatusBarHeight();
            toolbar.setLayoutParams(params);
        }
    };

    public TabFragment() {
        parentTag = TabManager.getActiveTag();
    }

    public String getParentTag() {
        return parentTag;
    }

    public TabConfiguration getConfiguration() {
        return configuration;
    }

    /* For TabManager etc */
    public String getTitle() {
        return title == null ? configuration.getDefaultTitle() : title;
    }

    public final void setTitle(String newTitle) {
        this.title = newTitle;
        if (tabTitle == null)
            getMainActivity().updateTabList();
        toolbarTitleView.setText(getTitle());
    }

    protected final String getSubtitle() {
        return subtitle;
    }

    public final void setSubtitle(String newSubtitle) {
        this.subtitle = newSubtitle;
        if (subtitle == null) {
            if (toolbarSubtitleView.getVisibility() != View.GONE)
                toolbarSubtitleView.setVisibility(View.GONE);
        } else {
            if (toolbarSubtitleView.getVisibility() != View.VISIBLE)
                toolbarSubtitleView.setVisibility(View.VISIBLE);
            toolbarSubtitleView.setText(getSubtitle());
        }
    }

    public String getTabTitle() {
        return tabTitle == null ? getTitle() : tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
        getMainActivity().updateTabList();
    }

    public Menu getMenu() {
        return toolbar.getMenu();
    }


    public CompositeDisposable getDisposable() {
        return disposable;
    }

    //False - можно закрывать
    //True - еще нужно что-то сделать, не закрывать
    @CallSuper
    public boolean onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed " + this);
        return false;
    }

    public void hidePopupWindows() {
        getMainActivity().hideKeyboard();
    }

    //Загрузка каких-то данных, выполняется только при наличии сети
    @CallSuper
    public void loadData() {
        alreadyCallLoad = true;

    }

    @CallSuper
    public void loadCacheData() {

    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiThread = Thread.currentThread();
        Log.d(LOG_TAG, "onDestroy " + this);
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(BUNDLE_PREFIX.concat(BUNDLE_TITLE));
            subtitle = savedInstanceState.getString(BUNDLE_PREFIX.concat(BUNDLE_SUBTITLE));
            tabTitle = savedInstanceState.getString(BUNDLE_PREFIX.concat(BUNDLE_TAB_TITLE));
            parentTag = savedInstanceState.getString(BUNDLE_PREFIX.concat(BUNDLE_PARENT_TAG));
        }
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            subtitle = getArguments().getString(TAB_SUBTITLE);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_base, container, false);
        //Осторожно! Чувствительно к структуре разметки! (по идеи так должно работать чуть быстрее)
        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container);
        coordinatorLayout = (CoordinatorLayout) fragmentContainer.findViewById(R.id.coordinator_layout);
        appBarLayout = (AppBarLayout) coordinatorLayout.findViewById(R.id.appbar_layout);
        toolbarLayout = (CollapsingToolbarLayout) appBarLayout.findViewById(R.id.toolbar_layout);
        toolbarBackground = (ImageView) toolbarLayout.findViewById(R.id.toolbar_image_background);
        toolbar = (Toolbar) toolbarLayout.findViewById(R.id.toolbar);
        toolbarImageView = (ImageView) toolbar.findViewById(R.id.toolbar_image_icon);
        toolbarTitleView = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarSubtitleView = (TextView) toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarProgress = (ProgressBar) toolbar.findViewById(R.id.toolbar_progress);
        titlesWrapper = (LinearLayout) toolbar.findViewById(R.id.toolbar_titles_wrapper);
        toolbarSpinner = (Spinner) toolbar.findViewById(R.id.toolbar_spinner);
        notifyDot = findViewById(R.id.notify_dot);
        fragmentContent = (ViewGroup) coordinatorLayout.findViewById(R.id.fragment_content);
        additionalContent = (ViewGroup) coordinatorLayout.findViewById(R.id.additional_content);
        contentProgress = (ProgressBar) additionalContent.findViewById(R.id.content_progress);
        noNetwork = (LinearLayout) coordinatorLayout.findViewById(R.id.no_network);
        //// TODO: 20.03.17 удалить и юзать только там, где нужно
        fab = (FloatingActionButton) coordinatorLayout.findViewById(R.id.fab);

        contentController = new ContentController(contentProgress, additionalContent, fragmentContent);

        toolbarTitleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        toolbarTitleView.setHorizontallyScrolling(true);
        toolbarTitleView.setMarqueeRepeatLimit(3);
        toolbarTitleView.setSelected(true);
        toolbarTitleView.setHorizontalFadingEdgeEnabled(true);
        toolbarTitleView.setFadingEdgeLength(App.px16);

        App.get().addStatusBarSizeObserver(statusBarSizeObserver);

        //fragmentContainer.setPadding(0, App.getStatusBarHeight(), 0, 0);

        boolean isMenu = configuration.isAlone() || configuration.isMenu();
        toolbar.setNavigationOnClickListener(isMenu ? getMainActivity().getToggleListener() : getMainActivity().getRemoveTabListener());
        toolbar.setNavigationIcon(isMenu ? R.drawable.ic_toolbar_hamburger : R.drawable.ic_toolbar_arrow_back);
        toolbar.setNavigationContentDescription(isMenu ? getString(R.string.open_menu) : getString(R.string.close_tab));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.toolbar_shadow_prelp).setVisibility(View.VISIBLE);
        }

        if (!Client.getInstance().getNetworkState()) {
            if (!configuration.isUseCache())
                noNetwork.setVisibility(View.VISIBLE);
            //if (!getTag().equals(TabManager.getActiveTag())) return view;
            Snackbar.make(getCoordinatorLayout(), "No network connection", Snackbar.LENGTH_LONG).show();
        }

        //Для обновления вьюх
        setTitle(title);
        setSubtitle(subtitle);

        updateNotifyDot();
        ClientHelper.getInstance().addCountsObserver(countsObserver);
        Client.getInstance().addNetworkObserver(networkObserver);
        App.get().addPreferenceChangeObserver(tabPreferenceObserver);
        return view;
    }

    protected void baseInflateFragment(LayoutInflater inflater, @LayoutRes int res) {
        inflater.inflate(res, fragmentContent, true);
    }

    protected void setListsBackground(View view) {
        view.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
    }

    protected void setListsBackground() {
        setListsBackground(fragmentContainer);
    }

    protected void setCardsBackground(View view) {
        view.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_cards));
    }

    protected void setCardsBackground() {
        setCardsBackground(fragmentContainer);
    }


    protected void viewsReady() {
        addBaseToolbarMenu();
        if (Client.getInstance().getNetworkState() && !configuration.isUseCache()) {
            if (!alreadyCallLoad)
                loadData();
        } else {
            loadCacheData();
        }
    }


    @CallSuper
    protected void addBaseToolbarMenu() {

    }

    @CallSuper
    protected void refreshToolbarMenuItems(boolean enable) {

    }

   /* @CallSuper
    protected void refreshToolbarMenu(){
        getMenu().clear();
        addBaseToolbarMenu();

    }*/

    protected void updateNotifyDot() {
        if (!showNotifyDot) {
            notifyDot.setVisibility(View.GONE);
            return;
        }
        if (decideShowDot()) {
            notifyDot.setVisibility(View.VISIBLE);
        } else {
            notifyDot.setVisibility(View.GONE);
        }
    }

    private boolean decideShowDot() {
        if (ClientHelper.getAllCounts() > 0) {
            if (ClientHelper.getFavoritesCount() > 0 && notifyDotFav) {
                return true;
            }
            if (ClientHelper.getQmsCount() > 0 && notifyDotQms) {
                return true;
            }
            if (ClientHelper.getMentionsCount() > 0 && notifyDotMentions) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        ScrollAwareFABBehavior behavior = new ScrollAwareFABBehavior(fab.getContext(), null);
        params.setBehavior(behavior);
        fab.requestLayout();
    }

    protected void refreshLayoutStyle(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setProgressBackgroundColorSchemeColor(App.getColorFromAttr(getContext(), R.attr.colorPrimary));
        refreshLayout.setColorSchemeColors(App.getColorFromAttr(getContext(), R.attr.colorAccent));
    }

    protected void refreshLayoutLongTrigger(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setDistanceToTriggerSync(App.px48 * 3);
        refreshLayout.setProgressViewEndTarget(false, App.px48 * 3);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_PREFIX.concat(BUNDLE_TITLE), title);
        outState.putString(BUNDLE_PREFIX.concat(BUNDLE_SUBTITLE), subtitle);
        outState.putString(BUNDLE_PREFIX.concat(BUNDLE_TAB_TITLE), tabTitle);
        outState.putString(BUNDLE_PREFIX.concat(BUNDLE_PARENT_TAG), parentTag);
    }


    public final View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    public final MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume " + this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause " + this);
        hidePopupWindows();
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy " + this);
        if (!disposable.isDisposed())
            disposable.dispose();
        hidePopupWindows();
        contentController.destroy();
        ClientHelper.getInstance().removeCountsObserver(countsObserver);
        Client.getInstance().removeNetworkObserver(networkObserver);
        App.get().removePreferenceChangeObserver(tabPreferenceObserver);
        App.get().removeStatusBarSizeObserver(statusBarSizeObserver);
    }

    protected boolean isTalkBackEnabled() {
        AccessibilityManager am = (AccessibilityManager) getActivity().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        Log.d("SUKA", "CHECK TALKBACK " + isAccessibilityEnabled + " : " + isExploreByTouchEnabled);
        //return isExploreByTouchEnabled;
        return false;
    }

    public final void runInUiThread(final Runnable action) {
        if (Thread.currentThread() == mUiThread) {
            action.run();
        } else {
            mHandler.post(action);
        }
    }

    protected void startRefreshing() {
        contentController.startRefreshing();
    }

    protected void stopRefreshing() {
        contentController.stopRefreshing();
    }

    protected void setRefreshing(boolean refreshing) {
        if (refreshing)
            startRefreshing();
        else
            stopRefreshing();
    }

    /* Experiment */
    public static class Builder<T extends TabFragment> {
        private T tClass;

        public Builder(Class<T> tClass) {
            try {
                this.tClass = tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Builder setArgs(Bundle args) {
            tClass.setArguments(args);
            return this;
        }

        public Builder setIsMenu() {
            tClass.configuration.setMenu(true);
            return this;
        }

        /*public Builder setTitle(String title) {
            tClass.setTitle(title);
            return this;
        }*/

        public T build() {
            return tClass;
        }
    }
}
