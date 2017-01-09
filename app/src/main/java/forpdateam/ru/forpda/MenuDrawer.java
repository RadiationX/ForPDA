package forpdateam.ru.forpda;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.news.NewsListFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.fragments.theme.ThemeFragment;
import io.realm.Realm;

/**
 * Created by radiationx on 07.08.16.
 */
public class MenuDrawer {
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private MenuAdapter adapter;
    private int active = -1;
    private Realm realm;

    public MenuDrawer(MainActivity activity, DrawerLayout drawerLayout) {
        realm = Realm.getDefaultInstance();
        initMenuItems();
        ListView menuList = (ListView) activity.findViewById(R.id.menu_list);
        drawer = (NavigationView) activity.findViewById(R.id.menu_drawer);
        adapter = new MenuAdapter(activity);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d("kek", "clicked " + i + " : " + menuItems.get(i).name);
            select(menuItems.get(i));
            close();
        });
        this.drawerLayout = drawerLayout;
        Api.Auth().addLoginObserver((observable, o) -> {
            menuItems.clear();
            initMenuItems();
            adapter.notifyDataSetChanged();
            if ((boolean) o && TabManager.getInstance().getSize() <= 1) {
                select(findByClassName(NewsListFragment.class.getSimpleName()));
            }
        });
        Api.get().addObserver((observable1, o) -> {
            MenuItem item = getByClass(QmsContactsFragment.class);
            if (item != null) {
                item.count = Api.get().getQmsCount();
            }
            item = getByClass(ThemeFragment.class);
            if (item != null) {
                item.count = Api.get().getMentionsCount();
            }
            item = getByClass(FavoritesFragment.class);
            if (item != null) {
                item.count = Api.get().getFavoritesCount();
            }
            adapter.notifyDataSetChanged();
        });
        String last = App.getInstance().getPreferences().getString("menu_drawer_last", Api.Auth().getState() ? NewsListFragment.class.getSimpleName() : AuthFragment.class.getSimpleName());
        if (last != null)
            select(findByClassName(last));
    }


    public void select(String className) {
        select(findByClassName(className));
    }

    private void select(MenuItem item) {
        if (item == null) return;
        try {
            TabManager.getInstance().add((TabFragment) item.gettClass().newInstance());
            active = menuItems.indexOf(item);
            Log.d("kek", "menu active " + active);
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
        if (!Api.Auth().getState())
            menuItems.add(new MenuItem<>("Авторизация", R.drawable.ic_person_add_gray_24dp, AuthFragment.class));
        menuItems.add(new MenuItem<>("Новости", R.drawable.ic_newspaper_gray, NewsListFragment.class));
        if (Api.Auth().getState()) {
            menuItems.add(new MenuItem<>("Профиль", R.drawable.ic_person_grary_24dp, ProfileFragment.class));
            menuItems.add(new MenuItem<>("Сообщения", R.drawable.ic_mail_gray_24dp, QmsContactsFragment.class));
            menuItems.add(new MenuItem<>("Избранное", R.drawable.ic_star_black_24dp, FavoritesFragment.class));
            menuItems.add(new MenuItem<>("Упоминания", R.drawable.ic_hearing_gray_24dp, ThemeFragment.class));
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

            if (item.getCount() > 0) {
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(Integer.toString(item.count));
            } else {
                holder.count.setVisibility(View.GONE);
            }

            if (position == active) {
                convertView.setBackgroundColor(color);
                holder.text.setTextColor(App.getContext().getResources().getColor(R.color.colorPrimary));
                holder.icon.setColorFilter(App.getContext().getResources().getColor(R.color.colorPrimary));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.text.setTextColor(App.getContext().getResources().getColor(R.color.text_drawer_item_color));
                holder.icon.clearColorFilter();
            }

            holder.icon.setImageDrawable(App.getAppDrawable(item.getDrawable()));
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
    }
}
