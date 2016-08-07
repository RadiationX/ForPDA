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
    private TabAdapter adapter;

    public TabDrawer(MainActivity activity) {
        adapter = new TabAdapter(activity, R.layout.tab_drawer_item, TabManager.getInstance().getFragments());
        ListView tabsList = (ListView) activity.findViewById(R.id.tabs_list);
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawer, activity.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        tabsList.setAdapter(adapter);
        tabsList.setOnItemClickListener((adapterView, view, i, l) -> TabManager.getInstance().select(TabManager.getInstance().get(i)));
    }

    public void notifyTabsChanged() {
        adapter.notifyDataSetChanged();
    }

    public class TabAdapter extends ArrayAdapter {
        private final LayoutInflater inflater;
        private List<TabFragment> mObjects = null;
        private int color = Color.argb(128, 255, 128, 65);

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
