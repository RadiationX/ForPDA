package forpdateam.ru.forpda;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabDrawer {
    private MainActivity activity;
    private TabAdapter adapter;

    public TabDrawer(MainActivity activity) {
        this.activity = activity;
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawer, activity.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        ListView tabsList = (ListView) activity.findViewById(R.id.tabs_list);
        adapter = new TabAdapter(activity, R.layout.tab_drawer_item, activity.tabManager.getFragments());
        tabsList.setAdapter(adapter);
        tabsList.setOnItemClickListener((adapterView, view, i, l) -> activity.tabManager.select(activity.tabManager.get(i)));
    }

    public void notifyTabsChanged() {
        adapter.notifyDataSetChanged();
    }

    public class TabAdapter extends ArrayAdapter {
        final LayoutInflater inflater;
        List<TabFragment> mObjects = null;

        public TabAdapter(Context context, int item_resource, List<TabFragment> objects) {
            super(context, item_resource, objects);
            mObjects = objects;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.tab_drawer_item, parent, false);
                holder = new ViewHolder();
                assert convertView != null;

                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.close = (ImageView) convertView.findViewById(R.id.close);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final TabFragment item = mObjects.get(position);
            if (position == TabManager.getActiveIndex())
                convertView.setBackgroundColor(Color.parseColor("#ff7711"));
            else
                convertView.setBackgroundColor(Color.TRANSPARENT);

            holder.text.setText(item.getTitle());
            holder.close.setOnClickListener(view -> activity.tabManager.remove(item));
            return convertView;
        }

        public class ViewHolder {
            public TextView text;
            public ImageView close;
        }
    }
}
