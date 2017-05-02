package forpdateam.ru.forpda.views.drawers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.settings.SettingsActivity;
import forpdateam.ru.forpda.views.drawers.adapters.MenuAdapter;
import forpdateam.ru.forpda.views.drawers.adapters.TabAdapter;

/**
 * Created by radiationx on 02.05.17.
 */

public class Drawers {
    private MainActivity activity;
    private DrawerLayout drawerLayout;

    private NavigationView menuDrawer;
    private RecyclerView menuListView;
    private MenuAdapter menuAdapter;
    private MenuItems allMenuItems = new MenuItems();
    private ArrayList<MenuItems.MenuItem> menuItems = new ArrayList<>();
    private MenuItems.MenuItem lastActive;

    private NavigationView tabDrawer;
    private RecyclerView tabListView;
    private TabAdapter tabAdapter;

    private Observer loginObserver = (observable, o) -> {
        menuItems.clear();
        fillMenuItems();
        menuAdapter.notifyDataSetChanged();
        if ((boolean) o && TabManager.getInstance().getSize() <= 1) {
            //select(findByClassName(NewsListFragment.class.getSimpleName()));
            selectMenuItem(findMenuItem(FavoritesFragment.class));
        }
        if (!(boolean) o) {
            App.getInstance().getPreferences().edit().remove("menu_drawer_last").apply();
        }
    };

    private Observer countsObserver = (observable1, o) -> {
        MenuItems.MenuItem item = findMenuItem(QmsContactsFragment.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getQmsCount());
        }
        item = findMenuItem(MentionsFragment.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getMentionsCount());
        }
        item = findMenuItem(FavoritesFragment.class);
        if (item != null) {
            item.setNotifyCount(ClientHelper.getFavoritesCount());
        }
        menuAdapter.notifyDataSetChanged();
    };

    public Drawers(MainActivity activity, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        menuDrawer = (NavigationView) activity.findViewById(R.id.menu_drawer);
        tabDrawer = (NavigationView) activity.findViewById(R.id.tab_drawer);

        menuListView = (RecyclerView) activity.findViewById(R.id.menu_list);
        tabListView = (RecyclerView) activity.findViewById(R.id.tab_list);

        menuListView.setLayoutManager(new LinearLayoutManager(activity));
        tabListView.setLayoutManager(new LinearLayoutManager(activity));


        menuAdapter = new MenuAdapter(menuItems);
        tabAdapter = new TabAdapter();

        menuListView.setAdapter(menuAdapter);
        tabListView.setAdapter(tabAdapter);
    }

    public void init(Bundle savedInstanceState) {
        initMenu(savedInstanceState);
        initTabs(savedInstanceState);
        String className = ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN ? FavoritesFragment.class.getSimpleName() : AuthFragment.class.getSimpleName();
        String last = App.getInstance().getPreferences().getString("menu_drawer_last", className);
        Log.d("FORPDA_LOG", "LAAST " + last);

        MenuItems.MenuItem item = null;
        if (savedInstanceState != null) {
            TabFragment tabFragment = TabManager.getInstance().get(TabManager.getActiveTag());
            if (tabFragment != null) {
                item = findMenuItem(tabFragment.getClass());
            }

            Log.e("FORPDA_LOG", "AAAA " + tabFragment + " : " + item);
            if (item != null) {
                item.setAttachedTabTag(tabFragment.getTag());
                item.setActive(true);
                lastActive = item;
            } else {
                item = findMenuItem(last);
            }
        } else {
            item = findMenuItem(last);
        }
        Log.e("FORPDA_LOG", "FINAL ITEM " + item);
        selectMenuItem(item);

        /*if (savedInstanceState == null) {
            select(findByClassName(last));
        } else {
            setActive(last);
        }*/
    }

    public void destroy() {
        ClientHelper.getInstance().removeLoginObserver(loginObserver);
        ClientHelper.getInstance().removeCountsObserver(countsObserver);
        //menuAdapter.clear();
        //tabAdapter.clear();
    }

    public void setStatusBarHeight(int height) {
        menuDrawer.setPadding(0, height, 0, 0);
        tabDrawer.setPadding(0, height, 0, 0);
    }

    private void initMenu(Bundle savedInstanceState) {
        fillMenuItems();
        ClientHelper.getInstance().addLoginObserver(loginObserver);
        ClientHelper.getInstance().addCountsObserver(countsObserver);
        menuAdapter.setItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItems.MenuItem menuItem, int position) {
                selectMenuItem(menuItem);
                closeMenu();
            }
        });
    }

    private void fillMenuItems() {
        menuItems.clear();
        for (MenuItems.MenuItem item : allMenuItems.getCreatedMenuItems()) {
            if (item.getTabClass() == AuthFragment.class && ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
                continue;
            } else if (ClientHelper.getAuthState() != ClientHelper.AUTH_STATE_LOGIN) {
                if (item.getTabClass() == ProfileFragment.class || item.getTabClass() == QmsContactsFragment.class || item.getTabClass() == FavoritesFragment.class || item.getTabClass() == MentionsFragment.class) {
                    continue;
                }
            }
            menuItems.add(item);
        }
    }

    private void selectMenuItem(MenuItems.MenuItem item) {
        Log.e("FORPDA_LOG", "selectMenuItem " + item);
        if (item == null) return;
        try {
            if (item.getTabClass() == null) {
                switch (item.getAction()){
                    case MenuItems.ACTION_APP_SETTINGS:{
                        activity.startActivity(new Intent(activity, SettingsActivity.class));
                        break;
                    }
                }

            } else {
                TabFragment tabFragment = TabManager.getInstance().get(item.getAttachedTabTag());
                if (tabFragment == null) {
                    for (TabFragment fragment : TabManager.getInstance().getFragments()) {
                        if (fragment.getClass() == item.getTabClass() && fragment.getConfiguration().isMenu()) {
                            tabFragment = fragment;
                            break;
                        }
                    }
                }

                if (tabFragment == null) {
                    tabFragment = item.getTabClass().newInstance();
                    tabFragment.getConfiguration().setMenu(true);
                    TabManager.getInstance().add(tabFragment);
                    item.setAttachedTabTag(tabFragment.getTag());
                } else {
                    TabManager.getInstance().select(tabFragment);
                }

                if (lastActive != null)
                    lastActive.setActive(false);
                item.setActive(true);
                lastActive = item;
                menuAdapter.notifyDataSetChanged();
                App.getInstance().getPreferences().edit().putString("menu_drawer_last", item.getTabClass().getSimpleName()).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setActiveMenu(TabFragment fragment) {
        for (MenuItems.MenuItem item : menuItems) {
            if (item.getTabClass() == fragment.getClass()) {
                if (lastActive != null)
                    lastActive.setActive(false);
                item.setActive(true);
                item.setAttachedTabTag(fragment.getTag());
                lastActive = item;
                menuAdapter.notifyDataSetChanged();
            }
        }
    }

    private MenuItems.MenuItem findMenuItem(String className) {
        for (MenuItems.MenuItem item : menuItems) {
            if (item.getTabClass().getSimpleName().equals(className))
                return item;
        }
        return null;
    }

    private MenuItems.MenuItem findMenuItem(Class<? extends TabFragment> classObject) {
        for (MenuItems.MenuItem item : menuItems) {
            if (item.getTabClass() == classObject)
                return item;
        }
        return null;
    }

    public void openMenu() {
        drawerLayout.openDrawer(menuDrawer);
    }

    public void closeMenu() {
        drawerLayout.closeDrawer(menuDrawer);
    }

    public void toggleMenu() {
        if (drawerLayout.isDrawerOpen(menuDrawer)) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void initTabs(Bundle savedInstanceState) {
        tabAdapter.setItemClickListener((tabFragment, position) -> {
            TabManager.getInstance().select(tabFragment);
            closeTabs();
        });
        tabAdapter.setCloseClickListener((tabFragment, position) -> {
            TabManager.getInstance().remove(tabFragment);
            if (TabManager.getInstance().getSize() < 1) {
                activity.finish();
            }
        });
        TabManager.getInstance().loadState(savedInstanceState);
        TabManager.getInstance().updateFragmentList();
    }

    public void notifyTabsChanged() {
        tabAdapter.notifyDataSetChanged();
    }

    public void openTabs() {
        drawerLayout.openDrawer(tabDrawer);
    }

    public void closeTabs() {
        drawerLayout.closeDrawer(tabDrawer);
    }

    public void toggleTabs() {
        if (drawerLayout.isDrawerOpen(tabDrawer)) {
            closeTabs();
        } else {
            openTabs();
        }
    }
}
