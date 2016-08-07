package forpdateam.ru.forpda;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private static TabManager instance;
    private final static int containerViewId = R.id.fr_container;
    private final static String prefix = "tab_";
    private final static String bundlePrefix = "tab_manager_";
    private FragmentManager fragmentManager;
    private UpdateListener updateListener;
    private int count = 0;
    private static String activeTag = "";
    private static int activeIndex = 0;
    private List<TabFragment> existingFragments = new ArrayList<>();

    public interface UpdateListener {
        void onAddTab(TabFragment fragment);

        void onRemoveTab(TabFragment fragment);

        void onSelectTab(TabFragment fragment);

        void onChange();
    }

    public static TabManager init(AppCompatActivity activity, UpdateListener listener) {
        if (instance != null) {
            instance = null;
            System.gc();
        }
        instance = new TabManager(activity, listener);
        return instance;
    }

    public static TabManager getInstance() {
        return instance;
    }

    public TabManager(AppCompatActivity activity, UpdateListener listener) {
        fragmentManager = activity.getSupportFragmentManager();
        updateListener = listener;
        update();
    }

    public void saveState(Bundle outState) {
        if (outState == null) return;
        outState.putString(bundlePrefix + "active_tag", activeTag);
        outState.putInt(bundlePrefix + "active_index", activeIndex);
    }

    public void loadState(Bundle state) {
        if (state == null) return;
        activeTag = state.getString(bundlePrefix + "active_tag", "");
        activeIndex = state.getInt(bundlePrefix + "active_index", 0);
    }

    public int getCount() {
        return existingFragments.size();
    }

    public static String getActiveTag() {
        return activeTag;
    }

    public static int getActiveIndex() {
        return activeIndex;
    }

    public List<TabFragment> getFragments() {
        return existingFragments;
    }

    public void update() {
        existingFragments.clear();
        if (fragmentManager.getFragments() == null) return;
        for(int i = 0; i<fragmentManager.getFragments().size(); i++){
            if (fragmentManager.getFragments().get(i) != null) existingFragments.add((TabFragment) fragmentManager.getFragments().get(i));
        }


    }

    private void hideTabs(FragmentTransaction transaction) {
        if (fragmentManager.getFragments() == null) return;
        for (Fragment fragment : fragmentManager.getFragments())
            if (fragment != null && !fragment.isHidden()) {
                transaction.hide(fragment);
                fragment.onPause();
            }
    }

    public TabFragment getActive() {
        return get(activeIndex);
    }

    public TabFragment get(final int index) {
        return existingFragments.get(index);
    }

    public TabFragment get(final String tag) {
        return (TabFragment) fragmentManager.findFragmentByTag(tag);
    }

    public void add(TabFragment tabFragment) {
        if (tabFragment == null)
            return;
        activeTag = prefix + count;
        count++;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideTabs(transaction);
        transaction.add(containerViewId, tabFragment, activeTag).commit();
        fragmentManager.executePendingTransactions();
        update();
        activeIndex = existingFragments.indexOf(tabFragment);
        updateListener.onChange();
        updateListener.onAddTab(tabFragment);
    }

    public void remove(final String tag) {
        remove(get(tag));
    }

    public void remove(TabFragment tabFragment) {
        if (tabFragment == null)
            return;

        fragmentManager.beginTransaction().remove(tabFragment).commit();
        fragmentManager.executePendingTransactions();
        update();

        if (activeIndex >= existingFragments.size() - 1) {
            if (activeIndex != 0)
                activeIndex = existingFragments.size() - 1;
        }

        if (existingFragments.size() == 0)
            activeTag = "";
        else
            activeTag = existingFragments.get(activeIndex).getTag();

        select(activeTag);
        updateListener.onChange();
        updateListener.onRemoveTab(tabFragment);
    }

    public void select(final String tag) {
        select(get(tag));
    }

    public void select(TabFragment tabFragment) {
        if (tabFragment == null)
            return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideTabs(transaction);
        transaction.show(tabFragment).commit();
        tabFragment.onResume();
        fragmentManager.executePendingTransactions();
        update();
        activeTag = tabFragment.getTag();
        activeIndex = existingFragments.indexOf(tabFragment);
        updateListener.onChange();
        updateListener.onSelectTab(tabFragment);
    }
}
