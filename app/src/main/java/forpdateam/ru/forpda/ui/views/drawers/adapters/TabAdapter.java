package forpdateam.ru.forpda.ui.views.drawers.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 02.05.17.
 */

public class TabAdapter extends BaseAdapter<TabFragment, TabAdapter.TabHolder> {
    private int color = Color.argb(24, 128, 128, 128);

    private BaseAdapter.OnItemClickListener<TabFragment> itemClickListener;
    private BaseAdapter.OnItemClickListener<TabFragment> closeClickListener;

    private String currentFragmentTag = null;

    public void setItemClickListener(BaseAdapter.OnItemClickListener<TabFragment> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setCloseClickListener(BaseAdapter.OnItemClickListener<TabFragment> closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    public void setCurrentFragmentTag(String tag) {
        currentFragmentTag = tag;
    }

    public void removeAt(int index) {
        items.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public TabHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.drawer_tab_item);
        return new TabHolder(v);
    }

    @Override
    public void onBindViewHolder(TabHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class TabHolder extends BaseViewHolder<TabFragment> implements View.OnClickListener {
        public TextView text;
        public ImageView close;
        public ViewGroup wrapper;
        private TabFragment currentItem;

        TabHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.drawer_item_title);
            close = (ImageView) v.findViewById(R.id.drawer_item_close);
            wrapper = v.findViewById(R.id.drawer_item_wrapper);

            v.setOnClickListener(this);
            close.setOnClickListener(v1 -> {
                if (closeClickListener != null) {
                    closeClickListener.onItemClick(currentItem);
                }
            });
        }

        @Override
        public void bind(TabFragment item, int position) {
            currentItem = item;
            boolean isActive = item.getTag() != null && item.getTag().equals(currentFragmentTag);
            Log.d("lalala", "TabAdapter bind " + item + " : " + isActive + " : " + position);

            if (isActive)
                wrapper.setBackgroundColor(color);
            else
                wrapper.setBackgroundColor(Color.TRANSPARENT);

            text.setText(item.getTabTitle());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentItem);
            }
        }
    }
}