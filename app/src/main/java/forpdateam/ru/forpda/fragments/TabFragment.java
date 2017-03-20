package forpdateam.ru.forpda.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxFragment;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.favorites.FavoritesAdapter;
import forpdateam.ru.forpda.pagination.PaginationHelper;
import forpdateam.ru.forpda.settings.SettingsActivity;

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

    protected TabConfiguration configuration = new TabConfiguration();

    private String title = null, tabTitle = null, subtitle = null, parentTag = null;

    protected RelativeLayout fragmentContainer, fragmentContent;
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout appBarLayout;
    protected CollapsingToolbarLayout toolbarLayout;
    protected Toolbar toolbar;
    protected ImageView toolbarBackground, icNoNetwork, toolbarImageView;
    protected TextView toolbarTitleView, toolbarSubtitleView;
    protected View view, notifyDot;
    protected FloatingActionButton fab;


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

    protected final void setTitle(String newTitle) {
        this.title = newTitle;
        if (tabTitle == null)
            getMainActivity().updateTabList();
        toolbarTitleView.setText(getTitle());
    }

    protected final String getSubtitle() {
        return subtitle;
    }

    protected final void setSubtitle(String newSubtitle) {
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

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        notifyDot = toolbarLayout.findViewById(R.id.notify_dot);
        fragmentContent = (RelativeLayout) coordinatorLayout.findViewById(R.id.fragment_content);
        icNoNetwork = (ImageView) fragmentContent.findViewById(R.id.ic_no_network);
        //// TODO: 20.03.17 удалить и юзать только там, где нужно
        fab = (FloatingActionButton) coordinatorLayout.findViewById(R.id.fab);

        toolbar.setNavigationOnClickListener(configuration.isAlone() ? getMainActivity().getToggleListener() : getMainActivity().getRemoveTabListener());
        toolbar.setNavigationIcon(configuration.isAlone() ? R.drawable.ic_menu_gray_24dp : R.drawable.ic_arrow_back_gray_24dp);

        if (!Client.getInstance().getNetworkState()) {
            if (!configuration.isUseCache())
                icNoNetwork.setVisibility(View.VISIBLE);
            //if (!getTag().equals(TabManager.getActiveTag())) return view;
            Snackbar.make(getCoordinatorLayout(), "No network connection", Snackbar.LENGTH_LONG).show();
        }

        //Для обновления вьюх
        setTitle(title);
        setSubtitle(subtitle);

        updateNotifyDot();
        Api.get().addObserver((observable, o) -> updateNotifyDot());

        Client.getInstance().addNetworkObserver((observable, o) -> {
            if ((!configuration.isUseCache() || icNoNetwork.getVisibility() == View.VISIBLE) && (boolean) o) {
                loadData();
                icNoNetwork.setVisibility(View.GONE);
            }
        });
        return view;
    }

    protected void baseInflateFragment(LayoutInflater inflater, @LayoutRes int res) {
        inflater.inflate(res, fragmentContent, true);
    }

    protected void setWhiteBackground() {
        fragmentContent.setBackgroundColor(Color.WHITE);
    }

    protected void viewsReady() {
        if (Client.getInstance().getNetworkState() && !configuration.isUseCache()) {
            loadData();
        }
        addBaseToolbarMenu();
    }

    protected void addBaseToolbarMenu() {
        toolbar.getMenu().add("SETTINGS").setOnMenuItemClickListener(menuItem -> {
            getMainActivity().startActivity(new Intent(getContext(), SettingsActivity.class));
            return false;
        });
        toolbar.getMenu().add("logout").setOnMenuItemClickListener(menuItem -> {
            new Task().execute();
            return false;
        });
    }

    protected void updateNotifyDot() {
        if (!App.getInstance().getPreferences().getBoolean("main.show_notify_dot", true)) {
            notifyDot.setVisibility(View.GONE);
            return;
        }
        if (Api.get().getAllCounts() > 0)
            notifyDot.setVisibility(View.VISIBLE);
        else
            notifyDot.setVisibility(View.GONE);
    }

    class Task extends AsyncTask {
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Api.Auth().tryLogout();
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (exception != null) {
                new AlertDialog.Builder(getMainActivity())
                        .setMessage(exception.getMessage())
                        .create()
                        .show();
            } else {
                Toast.makeText(getContext(), "logout complete", Toast.LENGTH_LONG).show();
                Api.Auth().doOnLogout();
            }
        }
    }

    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        // TODO: 20.12.16 not work in 25.1.0
        //params.setBehavior(new ScrollAwareFABBehavior(fab.getContext(), null));
        fab.requestLayout();
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


    protected final View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    protected final MainActivity getMainActivity() {
        return (MainActivity) getActivity();
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

        /*public Builder setTitle(String title) {
            tClass.setTitle(title);
            return this;
        }*/

        public T build() {
            return tClass;
        }
    }
}
