package forpdateam.ru.forpda.views.drawers.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.fragments.TabFragment;

/**
 * Created by radiationx on 02.05.17.
 */

public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {
    private int color = Color.argb(48, 128, 128, 128);

    private TabAdapter.OnItemClickListener itemClickListener;
    private TabAdapter.OnItemClickListener closeClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setCloseClickListener(OnItemClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    public TabFragment getItem(int position){
        return TabManager.getInstance().get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_tab_item, parent, false);
        return new TabAdapter.ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return TabManager.getInstance().getSize();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TabFragment fragment = TabManager.getInstance().get(position);
        if (position == TabManager.getActiveIndex())
            holder.itemView.setBackgroundColor(color);
        else
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        holder.text.setText(fragment.getTabTitle());
        holder.close.setColorFilter(App.getContext().getResources().getColor(R.color.black));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView text;
        public ImageView close;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.text);
            close = (ImageView) v.findViewById(R.id.close);

            v.setOnClickListener(this);
            close.setOnClickListener(v1 -> {
                if (closeClickListener != null) {
                    closeClickListener.onItemClick(getItem(getLayoutPosition()), getLayoutPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()), getLayoutPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(TabFragment tabFragment, int position);
    }
}