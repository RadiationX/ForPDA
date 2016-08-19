package forpdateam.ru.forpda.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.trello.rxlifecycle.components.support.RxFragment;

import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.client.Client;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabFragment extends RxFragment implements ITabFragment {
    public final static String TITLE_ARG = "TAB_TITLE";
    public final static String URL_ARG = "TAB_URL";
    private final static String prefix = "tab_fragment_";
    protected String tabUrl = "";
    protected View view;
    private int UID = 0;
    private String title = this.getClass().getSimpleName();
    private String subtitle;
    private String parentTag;
    private ImageView icNoNetwork;

    public TabFragment() {
        parentTag = TabManager.getActiveTag();
        setUID();
    }

    /* For TabManager etc */
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getUID() {
        return UID;
    }

    @Override
    public void setUID() {
        UID = (getArguments() + getTabUrl() + getClass().getSimpleName()).hashCode();
        Log.d("UID", "" + UID);
    }

    @Override
    public boolean isAlone() {
        return false;
    }

    @Override
    public String getTabUrl() {
        return tabUrl;
    }

    public void setTabUrl(String tabUrl) {
        this.tabUrl = tabUrl;
    }

    @Override
    public String getParentTag() {
        return parentTag;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void hidePopupWindows() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            parentTag = savedInstanceState.getString(prefix + "parent_tag");
            title = savedInstanceState.getString(prefix + "title");
            subtitle = savedInstanceState.getString(prefix + "subtitle");
        }

        if (isAlone())
            removeArrow();
        else
            setArrow();

        Log.d("kek", "oncreate " + getArguments() + " : " + savedInstanceState + " : " + title);


        if (getArguments() != null) {
            setTitle(getArguments().getString(TITLE_ARG));
            setTabUrl(getArguments().getString(URL_ARG));
        } else {
            setTitle(title);
        }
        if (Client.getInstance().getNetworkState()) {
            loadData();
        }

        Client.getInstance().addNetworkObserver((observable, o) -> {
            if (icNoNetwork.getVisibility() == View.VISIBLE && (boolean) o) {
                loadData();
                icNoNetwork.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        icNoNetwork = (ImageView) view.findViewById(R.id.ic_no_network);
        if (!Client.getInstance().getNetworkState()) {
            icNoNetwork.setVisibility(View.VISIBLE);
            if (!getTag().equals(TabManager.getActiveTag())) return;
            Snackbar.make(getMainActivity().getCoordinatorLayout(), "No network connection", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(prefix + "parent_tag", parentTag);
        outState.putString(prefix + "title", title);
        outState.putString(prefix + "subtitle", subtitle);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("kek", this + " : hidden change " + hidden);
        if (hidden) {
            /*getSupportActionBar().setTitle(null);
            getSupportActionBar().setSubtitle(null);*/
        } else {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle(subtitle);
            if (isAlone())
                removeArrow();
            else
                setArrow();
        }
    }

    public void setArrow() {
        getMainActivity().setHamburgerState(false);
    }

    public void removeArrow() {
        getMainActivity().setHamburgerState(true);
    }


    /* For UI in class */
    protected final String getSubtitle() {
        return subtitle;
    }

    protected final void setTitle(String title) {
        this.title = title;
        getMainActivity().updateTabList();
        if (getTag().equals(TabManager.getActiveTag()))
            getSupportActionBar().setTitle(title);
    }

    protected final void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        if (getTag().equals(TabManager.getActiveTag()))
            getSupportActionBar().setSubtitle(subtitle);
    }


    protected final View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    protected final ActionBar getSupportActionBar() {
        return getMainActivity().getSupportActionBar();
    }

    protected final MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("kek", this + " : onresume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("kek", this + " : onpause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSupportActionBar().setTitle(MainActivity.DEF_TITLE);
        getSupportActionBar().setSubtitle(null);
        if (!isAlone())
            removeArrow();
        else
            setArrow();
    }

    /* Experiment */
    public static class Builder<T extends TabFragment> {
        private T tClass;

        public Builder(Class<T> tClass) {
            try {
                this.tClass = (T) tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Builder setArgs(Bundle args) {
            tClass.setArguments(args);
            return this;
        }

        public Builder setTitle(String title) {
            tClass.setTitle(title);
            return this;
        }

        public T build() {
            tClass.setUID();;
            return tClass;
        }
    }
}
