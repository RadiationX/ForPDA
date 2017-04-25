package forpdateam.ru.forpda;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.Observer;

import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.fragments.search.SearchFragment;

/**
 * Created by radiationx on 07.08.16.
 */
public class MenuDrawer {
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private ArrayList<MenuItem> createdMenuItems = new ArrayList<>();
    private MenuAdapter adapter;
    private int active = -1;
    private Observer loginObserver = (observable, o) -> {
        menuItems.clear();
        initMenuItems();
        adapter.notifyDataSetChanged();
        if ((boolean) o && TabManager.getInstance().getSize() <= 1) {
            //select(findByClassName(NewsListFragment.class.getSimpleName()));
            select(findByClassName(FavoritesFragment.class.getSimpleName()));
        }
        if (!(boolean) o) {
            App.getInstance().getPreferences().edit().remove("menu_drawer_last").apply();
        }
    };

    private Observer countsObserver = (observable1, o) -> {
        MenuItem item = getByClass(QmsContactsFragment.class);
        if (item != null) {
            item.count = ClientHelper.getQmsCount();
        }
        item = getByClass(MentionsFragment.class);
        if (item != null) {
            item.count = ClientHelper.getMentionsCount();
        }
        item = getByClass(FavoritesFragment.class);
        if (item != null) {
            item.count = ClientHelper.getFavoritesCount();
        }
        adapter.notifyDataSetChanged();
    };

    public void destroy() {
        ClientHelper.getInstance().removeLoginObserver(loginObserver);
        ClientHelper.getInstance().removeCountsObserver(countsObserver);
        adapter.clear();
    }

    public MenuDrawer(MainActivity activity, DrawerLayout drawerLayout, Bundle savedInstanceState) {
        initMenuItems();
        ListView menuList = (ListView) activity.findViewById(R.id.menu_list);
        drawer = (NavigationView) activity.findViewById(R.id.menu_drawer);
        adapter = new MenuAdapter(activity);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener((adapterView, view, i, l) -> {
            select(menuItems.get(i));
            close();
        });
        this.drawerLayout = drawerLayout;
        ClientHelper.getInstance().addLoginObserver(loginObserver);
        ClientHelper.getInstance().addCountsObserver(countsObserver);
        //String last = App.getInstance().getPreferences().getString("menu_drawer_last", Api.Auth_Unclear().getAuthState() ? NewsListFragment.class.getSimpleName() : AuthFragment.class.getSimpleName());
        String last = App.getInstance().getPreferences().getString("menu_drawer_last", ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN ? FavoritesFragment.class.getSimpleName() : AuthFragment.class.getSimpleName());
        Log.d("FORPDA_LOG", "LAAST " + last);

        if (last != null) {
            if (savedInstanceState == null) {
                select(findByClassName(last));
            } else {
                setActive(last);
            }
        }
    }

    private void select(MenuItem item) {
        Log.d("FORPDA_LOG", "select " + item);
        if (item == null) return;
        if (item.gettClass() == null) {
            ACRA.getErrorReporter().handleException(new Exception("Manual exception"));
            return;
        }
        try {
            TabFragment tabFragment = TabManager.getInstance().get(item.getCreatedTag());
            Log.e("FORPDA_LOG", "MENU SELECT " + tabFragment);
            if (tabFragment == null) {
                for (TabFragment fragment : TabManager.getInstance().getFragments()) {
                    if (fragment.getClass().equals(item.gettClass()) && fragment.getConfiguration().isMenu()) {
                        tabFragment = fragment;
                        break;
                    }
                }
            }
            Log.e("FORPDA_LOG", "MENU SELECT " + tabFragment);
            if (tabFragment == null) {
                tabFragment = (TabFragment) item.gettClass().newInstance();
                tabFragment.getConfiguration().setMenu(true);
                TabManager.getInstance().add(tabFragment);
                item.setCreatedTag(tabFragment.getTag());
            } else {
                TabManager.getInstance().select(tabFragment);
            }
            Log.e("FORPDA_LOG", "YOUU BITCH CHO KAVO " + item.getName() + " : " + menuItems.indexOf(item));

            active = menuItems.indexOf(item);
            Log.d("FORPDA_LOG", "menu active " + active);
            adapter.notifyDataSetChanged();
            App.getInstance().getPreferences().edit().putString("menu_drawer_last", item.gettClass().getSimpleName()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MenuItem findByClassName(String className) {
        for (MenuItem item : menuItems) {
            if (item.gettClass().getSimpleName().equals(className))
                return item;
        }
        return null;
    }

    private void initMenuItems() {
        if (createdMenuItems.size() == 0) {
            createdMenuItems.add(new MenuItem<>("Авторизация", R.drawable.ic_person_add_gray_24dp, AuthFragment.class));
            //createdMenuItems.add(new MenuItem<>("Новости", R.drawable.ic_newspaper_gray, NewsListFragment.class));
            createdMenuItems.add(new MenuItem<>("Избранное", R.drawable.ic_star_black_24dp, FavoritesFragment.class));
            createdMenuItems.add(new MenuItem<>("Контакты", R.drawable.ic_contacts_gray_24dp, QmsContactsFragment.class));
            createdMenuItems.add(new MenuItem<>("Ответы", R.drawable.ic_notifications_gray_24dp, MentionsFragment.class));
            createdMenuItems.add(new MenuItem<>("Форум", R.drawable.ic_forum_gray_24dp, ForumFragment.class));
            createdMenuItems.add(new MenuItem<>("Поиск", R.drawable.ic_search_gray_24dp, SearchFragment.class));
            //createdMenuItems.add(new MenuItem<>("Сообщить об ошибке", R.drawable.ic_error_gray_24dp, null));
            //createdMenuItems.add(new MenuItem<>("Профиль", R.drawable.ic_person_gray_24dp, ProfileFragment.class));
        }

        for (int i = 0; i < createdMenuItems.size(); i++) {
            MenuItem item = createdMenuItems.get(i);
            if (item.gettClass() == AuthFragment.class && ClientHelper.getAuthState() == ClientHelper.AUTH_STATE_LOGIN) {
                continue;
            } else if (ClientHelper.getAuthState() != ClientHelper.AUTH_STATE_LOGIN) {
                if (item.gettClass() == ProfileFragment.class || item.gettClass() == QmsContactsFragment.class || item.gettClass() == FavoritesFragment.class || item.gettClass() == MentionsFragment.class) {
                    continue;
                }
            }
            menuItems.add(item);

        }
    }

    public void toggleState() {
        if (drawerLayout.isDrawerOpen(drawer))
            close();
        else
            open();
    }

    public void open() {
        drawerLayout.openDrawer(drawer);
    }

    public void close() {
        drawerLayout.closeDrawer(drawer);
    }

    public void setActive(String className) {
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).gettClass().getSimpleName().equals(className)) {
                active = i;
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public class MenuAdapter extends ArrayAdapter<MenuItem> {
        private final static int item_res = R.layout.drawer_menu_item;
        private final LayoutInflater inflater;
        private int color = Color.argb(48, 128, 128, 128);

        public MenuAdapter(Context context) {
            super(context, item_res, menuItems);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return menuItems.size();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(item_res, parent, false);
                holder = new ViewHolder();
                assert convertView != null;
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.count = (TextView) convertView.findViewById(R.id.count);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MenuItem item = menuItems.get(position);
            //ViewCompat.setElevation(convertView, 16);

            if (item.getCount() > 0) {
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(Integer.toString(item.count));
            } else {
                holder.count.setVisibility(View.GONE);
            }

            if (position == active) {
                convertView.setBackgroundColor(color);
                holder.text.setTextColor(App.getContext().getResources().getColor(R.color.black));
                holder.icon.setColorFilter(App.getContext().getResources().getColor(R.color.black));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.text.setTextColor(App.getContext().getResources().getColor(R.color.text_color));
                holder.icon.clearColorFilter();
            }

            holder.icon.setImageDrawable(AppCompatResources.getDrawable(App.getContext(), item.getDrawable()));
            holder.text.setText(item.getName());
            return convertView;
        }

        public class ViewHolder {
            public TextView text;
            public TextView count;
            public ImageView icon;
        }
    }

    private MenuItem getByClass(Class className) {
        for (MenuItem item : menuItems) {
            if (item.gettClass() == className)
                return item;
        }
        return null;
    }

    private class MenuItem<T extends TabFragment> {
        private String name;
        private int count = 0;
        private int drawable = R.drawable.adf;
        private Class<T> tClass;
        private String createdTag = null;


        public MenuItem(String name, int res, Class<T> tClass) {
            this.name = name;
            this.drawable = res;
            this.tClass = tClass;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public int getDrawable() {
            return drawable;
        }

        public Class<T> gettClass() {
            return tClass;
        }

        public void setCreatedTag(String createdTag) {
            this.createdTag = createdTag;
        }

        public String getCreatedTag() {
            return createdTag;
        }
    }

    public void setStatusBarHeight(int height) {
        drawer.setPadding(0, height, 0, 0);
    }
}
