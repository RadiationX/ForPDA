package forpdateam.ru.forpda;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxFragment;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabFragment extends RxFragment implements ITabFragment {
    private final static String prefix = "tab_fragment_";
    protected View view;
    private int UID = 0;
    private String title = this.getClass().getSimpleName();
    private String subtitle;
    private String parentTag;

    public TabFragment() {
        parentTag = TabManager.getActiveTag();
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
        UID = (getArguments().toString() + getDefaultUrl()).hashCode();
    }

    @Override
    public boolean isAlone() {
        return false;
    }

    @Override
    public String getDefaultUrl() {
        return "";
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            parentTag = savedInstanceState.getString(prefix + "parent_tag");
            title = savedInstanceState.getString(prefix + "title");
            subtitle = savedInstanceState.getString(prefix + "subtitle");
        }

        if (isAlone())
            removeArrow();
        else
            setArrow();
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
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setSubtitle(null);
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


    /* Experiment */
    public static class Creator<T extends TabFragment> {
        private T tClass;

        public Creator(Class<T> tClass) {
            try {
                this.tClass = (T) tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Creator setArgs(Bundle args) {
            tClass.setArguments(args);
            return this;
        }

        public Creator setTitle(String title) {
            tClass.setTitle(title);
            return this;
        }

        public T get() {
            return tClass;
        }
    }
}
