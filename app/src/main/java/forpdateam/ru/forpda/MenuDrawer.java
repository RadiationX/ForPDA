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

import java.util.ArrayList;

import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.test.LoginFragment;
import forpdateam.ru.forpda.test.NewsListFragment;
import forpdateam.ru.forpda.test.ProfileFragment;
import forpdateam.ru.forpda.test.QmsFragment;
import forpdateam.ru.forpda.test.ThemeFragment;

/**
 * Created by radiationx on 07.08.16.
 */
public class MenuDrawer {
    private MenuAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private ArrayList<MenuItem> menuItems = new ArrayList<>();



    public MenuDrawer(MainActivity activity, DrawerLayout drawerLayout) {
        initMenuItems();
        ListView menuList = (ListView) activity.findViewById(R.id.menu_list);
        drawer = (NavigationView) activity.findViewById(R.id.menu_drawer);
        adapter = new MenuAdapter(activity);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener((adapterView, view, i, l) -> {
            Log.d("kek", "clicked "+i+" : "+menuItems.get(i).name);
            try {
                TabManager.getInstance().add((TabFragment) menuItems.get(i).gettClass().newInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        this.drawerLayout = drawerLayout;
    }

    private void initMenuItems(){
        menuItems.add(new MenuItem<>("Login", android.R.drawable.ic_input_add, LoginFragment.class));
        menuItems.add(new MenuItem<>("News List", android.R.drawable.ic_input_add, NewsListFragment.class));
        menuItems.add(new MenuItem<>("Profile", android.R.drawable.ic_input_add, ProfileFragment.class));
        menuItems.add(new MenuItem<>("QMS", android.R.drawable.ic_input_add, QmsFragment.class));
        menuItems.add(new MenuItem<>("Theme", android.R.drawable.ic_input_add, ThemeFragment.class));
    }
    public void toggleState() {
        if (drawerLayout.isDrawerOpen(drawer))
            drawerLayout.closeDrawer(drawer);
        else
            drawerLayout.openDrawer(drawer);
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
