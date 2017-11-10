package forpdateam.ru.forpda.ui.views.drawers.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;
import forpdateam.ru.forpda.ui.views.drawers.MenuItems;

/**
 * Created by radiationx on 02.05.17.
 */

public class MenuAdapter extends BaseAdapter<MenuItems.MenuItem, MenuAdapter.MenuItemHolder> {
    private int color = Color.argb(48, 128, 128, 128);

    private BaseAdapter.OnItemClickListener<MenuItems.MenuItem> itemClickListener;

    public void setItemClickListener(BaseAdapter.OnItemClickListener<MenuItems.MenuItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_menu_item, parent, false);
        return new MenuItemHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuItemHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class MenuItemHolder extends BaseViewHolder<MenuItems.MenuItem> implements View.OnClickListener {
        public TextView text;
        public TextView count;
        public ImageView icon;

        public MenuItemHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.drawer_item_title);
            count = (TextView) v.findViewById(R.id.drawer_item_count);
            icon = (ImageView) v.findViewById(R.id.drawer_item_icon);
            v.setOnClickListener(this);
        }

        @Override
        public void bind(MenuItems.MenuItem item, int position) {
            if (item.getNotifyCount() > 0) {
                count.setVisibility(View.VISIBLE);
                count.setText(Integer.toString(item.getNotifyCount()));
            } else {
                count.setVisibility(View.GONE);
            }

            if (item.isActive()) {
                itemView.setBackgroundColor(color);
                text.setTextColor(App.getColorFromAttr(itemView.getContext(), R.attr.drawer_item_text_selected));
                icon.setColorFilter(App.getColorFromAttr(itemView.getContext(), R.attr.colorAccent));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
                text.setTextColor(App.getColorFromAttr(itemView.getContext(), R.attr.drawer_item_text));
                icon.clearColorFilter();
            }

            icon.setImageDrawable(App.getVecDrawable(itemView.getContext(), item.getIconRes()));
            text.setText(item.getTitle());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }
    }

}