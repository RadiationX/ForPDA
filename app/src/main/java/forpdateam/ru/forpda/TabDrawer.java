package forpdateam.ru.forpda;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import forpdateam.ru.forpda.fragments.TabFragment;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabDrawer {
    private TabAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private MainActivity mainActivity;

    public TabDrawer(MainActivity activity, DrawerLayout drawerLayout) {
        mainActivity = activity;
        ListView tabsList = (ListView) activity.findViewById(R.id.tabs_list);
        drawer = (NavigationView) activity.findViewById(R.id.tab_drawer);
        adapter = new TabAdapter(activity);
        tabsList.setAdapter(adapter);
        tabsList.setOnItemClickListener((adapterView, view, i, l) -> {
            TabManager.getInstance().select(TabManager.getInstance().get(i));
            close();
        });
        this.drawerLayout = drawerLayout;
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

    public void notifyTabsChanged() {
        adapter.notifyDataSetChanged();
    }

    public class TabAdapter extends ArrayAdapter<TabFragment> {
        private final static int item_res = R.layout.drawer_tab_item;
        private final LayoutInflater inflater;
        private int color = Color.argb(48, 128, 128, 128);

        public TabAdapter(Context context) {
            super(context, item_res, TabManager.getInstance().getFragments());
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return TabManager.getInstance().getSize();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(item_res, parent, false);
                holder = new ViewHolder();
                assert convertView != null;
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.close = (ImageView) convertView.findViewById(R.id.close);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TabFragment fragment = TabManager.getInstance().get(position);
            if (position == TabManager.getActiveIndex())
                convertView.setBackgroundColor(color);
            else
                convertView.setBackgroundColor(Color.TRANSPARENT);

            holder.text.setText(fragment.getTabTitle());
            holder.close.setOnClickListener(view -> {
                TabManager.getInstance().remove(fragment);
                if (TabManager.getInstance().getSize() < 1) {
                    mainActivity.finish();
                }
            });
            holder.close.setColorFilter(App.getContext().getResources().getColor(R.color.black));
            return convertView;
        }

        public class ViewHolder {
            public TextView text;
            public ImageView close;
        }
    }

    public void setStatusBarHeight(int height) {
        drawer.setPadding(0, height, 0, 0);
    }
}
