package forpdateam.ru.forpda;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
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

    public TabDrawer(MainActivity activity, DrawerLayout drawerLayout) {
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
        private int color = Color.argb(128, 255, 128, 65);

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

            TabFragment item = TabManager.getInstance().get(position);
            if (position == TabManager.getActiveIndex())
                convertView.setBackgroundColor(color);
            else
                convertView.setBackgroundColor(Color.TRANSPARENT);

            holder.text.setText(item.getTitle());
            holder.close.setOnClickListener(view -> TabManager.getInstance().remove(item));
            return convertView;
        }

        public class ViewHolder {
            public TextView text;
            public ImageView close;
        }
    }
}
