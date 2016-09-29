package forpdateam.ru.forpda;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.test.AuthFragment;
import forpdateam.ru.forpda.test.NewsListFragment;
import forpdateam.ru.forpda.test.ThemeFragment;

/**
 * Created by radiationx on 07.08.16.
 */
public class MenuDrawer {
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private MenuAdapter adapter;
    private int active = -1;

    public MenuDrawer(MainActivity activity, DrawerLayout drawerLayout) {
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
        String last = App.getInstance().getPreferences().getString("menu_drawer_last", Api.Auth().getState() ? NewsListFragment.class.getSimpleName() : AuthFragment.class.getSimpleName());
        if (last != null)
            select(findByClassName(last));
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
            menuItems.add(new MenuItem<>("Auth", android.R.drawable.ic_input_add, AuthFragment.class));
        menuItems.add(new MenuItem<>("News List", android.R.drawable.ic_input_add, NewsListFragment.class));
        menuItems.add(new MenuItem<>("Profile", android.R.drawable.ic_input_add, ProfileFragment.class));
        if (Api.Auth().getState()) {
            menuItems.add(new MenuItem<>("QMS Contacts", android.R.drawable.ic_input_add, QmsContactsFragment.class));
            menuItems.add(new MenuItem<>("Favorites", android.R.drawable.ic_input_add, FavoritesFragment.class));
        }
        menuItems.add(new MenuItem<>("Theme", android.R.drawable.ic_input_add, ThemeFragment.class));
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
        private int color = Color.argb(128, 255, 128, 65);

        public MenuAdapter(Context context) {
            super(context, item_res, menuItems);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return menuItems.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(item_res, parent, false);
                holder = new ViewHolder();
                assert convertView != null;
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MenuItem item = menuItems.get(position);

            if (position == active)
                convertView.setBackgroundColor(color);
            else
                convertView.setBackgroundColor(Color.TRANSPARENT);

            holder.icon.setImageDrawable(App.getContext().getResources().getDrawable(item.getDrawable()));
            holder.text.setText(item.getName());
            return convertView;
        }

        public class ViewHolder {
            public TextView text;
            public ImageView icon;
        }
    }

    private class MenuItem<T extends TabFragment> {
        private String name;
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

        public int getDrawable() {
            return drawable;
        }

        public Class<T> gettClass() {
            return tClass;
        }
    }
}
