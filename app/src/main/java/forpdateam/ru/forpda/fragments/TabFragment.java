package forpdateam.ru.forpda.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.settings.Preferences;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabFragment extends RxFragment {
    public final static String ARG_TITLE = "TAB_TITLE";
    public final static String TAB_SUBTITLE = "TAB_SUBTITLE";
    public final static String ARG_TAB = "TAB_URL";
    private final static String BUNDLE_PREFIX = "tab_fragment_";
    private final static String BUNDLE_TITLE = "title";
    private final static String BUNDLE_TAB_TITLE = "tab_title";
    private final static String BUNDLE_SUBTITLE = "subtitle";
    private final static String BUNDLE_PARENT_TAG = "parent_tag";

    public final static int REQUEST_PICK_FILE = 1228;
    public final static int REQUEST_STORAGE = 1;


    protected TabConfiguration configuration = new TabConfiguration();

    private String title = null, tabTitle = null, subtitle = null, parentTag = null;

    protected RelativeLayout fragmentContainer;
    protected LinearLayout fragmentContent, noNetwork;
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout appBarLayout;
    protected CollapsingToolbarLayout toolbarLayout;
    protected Toolbar toolbar;
    protected ImageView toolbarBackground, toolbarImageView;
    protected TextView toolbarTitleView, toolbarSubtitleView;
    protected View view, notifyDot;
    protected FloatingActionButton fab;
    private AudioManager audioService;
    private boolean showNotifyDot = App.getInstance().getPreferences().getBoolean(Preferences.Main.SHOW_NOTIFY_DOT, true);

    protected Observer countsObserver = (observable, o) -> updateNotifyDot();
    protected Observer networkObserver = (observable, o) -> {
        if (o == null)
            o = true;
        if ((!configuration.isUseCache() || noNetwork.getVisibility() == View.VISIBLE) && (boolean) o) {
            loadData();
            noNetwork.setVisibility(View.GONE);
        }
    };
    protected Observer tabPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.SHOW_NOTIFY_DOT: {
                showNotifyDot = App.getInstance().getPreferences().getBoolean(Preferences.Main.SHOW_NOTIFY_DOT, true);
                updateNotifyDot();
                break;
            }
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


    //False - можно закрывать
    //True - еще нужно что-то сделать, не закрывать
    public boolean onBackPressed() {
        Log.d("FORPDA_LOG", "onbackpressed tab");
        return false;
    }

    public void hidePopupWindows() {
        getMainActivity().hideKeyboard();
    }

    //Загрузка каких-то данных, выполняется только при наличии сети
    public void loadData() {

    }

    public void loadCacheData() {

    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioService = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
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
        notifyDot = findViewById(R.id.notify_dot);
        fragmentContent = (LinearLayout) coordinatorLayout.findViewById(R.id.fragment_content);
        noNetwork = (LinearLayout) fragmentContent.findViewById(R.id.no_network);
        //// TODO: 20.03.17 удалить и юзать только там, где нужно
        fab = (FloatingActionButton) coordinatorLayout.findViewById(R.id.fab);

        //fragmentContainer.setPadding(0, App.getStatusBarHeight(), 0, 0);

        toolbar.setNavigationOnClickListener(configuration.isAlone() || configuration.isMenu() ? getMainActivity().getToggleListener() : getMainActivity().getRemoveTabListener());
        toolbar.setNavigationIcon(configuration.isAlone() || configuration.isMenu() ? R.drawable.ic_toolbar_hamburger : R.drawable.ic_toolbar_arrow_back);

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
        App.getInstance().addPreferenceChangeObserver(tabPreferenceObserver);
        return view;
    }

    protected void baseInflateFragment(LayoutInflater inflater, @LayoutRes int res) {
        inflater.inflate(res, fragmentContent, true);
    }

    protected void setListsBackground() {
        fragmentContent.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_lists));
    }

    protected void setCardsBackground() {
        fragmentContent.setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_for_cards));
    }

    protected void viewsReady() {
        addBaseToolbarMenu();
        if (Client.getInstance().getNetworkState() && !configuration.isUseCache()) {
            loadData();
        } else {
            loadCacheData();
        }
    }



    @CallSuper
    protected void addBaseToolbarMenu() {

    }

    @CallSuper
    protected void refreshToolbarMenuItems(boolean enable){

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
        if (ClientHelper.getAllCounts() > 0) {
            notifyDot.setVisibility(View.VISIBLE);
        } else {
            notifyDot.setVisibility(View.GONE);
        }
    }

    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        // TODO: 20.12.16 not work in 25.1.0
        //params.setBehavior(new ScrollAwareFABBehavior(fab.getContext(), null));
        fab.requestLayout();
    }

    protected void refreshLayoutStyle(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setProgressBackgroundColorSchemeColor(App.getColorFromAttr(getContext(), R.attr.colorPrimary));
        refreshLayout.setColorSchemeColors(App.getColorFromAttr(getContext(), R.attr.colorAccent));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("FORPDA_LOG", "onactivitycreated " + getArguments() + " : " + savedInstanceState + " : " + title);
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

    public void tryPlayClickEffect() {
        try {
            audioService.playSoundEffect(SoundEffectConstants.CLICK);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FORPDA_LOG", this + " : onresume");
    }

    @Override
    public void onPause() {
        super.onPause();
        hidePopupWindows();
        Log.d("FORPDA_LOG", this + " : onpause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePopupWindows();
        ClientHelper.getInstance().removeCountsObserver(countsObserver);
        Client.getInstance().removeNetworkObserver(networkObserver);
        App.getInstance().removePreferenceChangeObserver(tabPreferenceObserver);
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
