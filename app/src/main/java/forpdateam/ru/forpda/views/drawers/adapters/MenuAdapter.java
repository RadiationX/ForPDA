package forpdateam.ru.forpda.views.drawers.adapters;

import android.graphics.Color;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.views.drawers.MenuItems;

/**
 * Created by radiationx on 02.05.17.
 */

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    List<MenuItems.MenuItem> items;
    private int color = Color.argb(48, 128, 128, 128);

    public MenuAdapter(List<MenuItems.MenuItem> items) {
        this.items = items;
    }

    private MenuAdapter.OnItemClickListener itemClickListener;

    public void setItemClickListener(MenuAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MenuItems.MenuItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_menu_item, parent, false);
        return new MenuAdapter.ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(MenuAdapter.ViewHolder holder, int position) {
        MenuItems.MenuItem item = getItem(position);
        assert item != null;

        if (item.getNotifyCount() > 0) {
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(Integer.toString(item.getNotifyCount()));
        } else {
            holder.count.setVisibility(View.GONE);
        }

        if (item.isActive()) {
            holder.itemView.setBackgroundColor(color);
            holder.text.setTextColor(App.getContext().getResources().getColor(R.color.black));
            holder.icon.setColorFilter(App.getContext().getResources().getColor(R.color.black));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.text.setTextColor(App.getContext().getResources().getColor(R.color.text_color));
            holder.icon.clearColorFilter();
        }

        holder.icon.setImageDrawable(AppCompatResources.getDrawable(App.getContext(), item.getIconRes()));
        holder.text.setText(item.getTitle());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView text;
        public TextView count;
        public ImageView icon;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.text);
            count = (TextView) v.findViewById(R.id.count);
            icon = (ImageView) v.findViewById(R.id.icon);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()), getLayoutPosition());
            }
        }
    }
    public interface OnItemClickListener {
        void onItemClick(MenuItems.MenuItem menuItem, int position);
    }
}