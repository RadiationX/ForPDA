package forpdateam.ru.forpda.views.messagepanel.advanced.adapters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.views.messagepanel.advanced.ButtonData;

/**
 * Created by radiationx on 08.01.17.
 */

public class PanelItemAdapter extends RecyclerView.Adapter<PanelItemAdapter.ViewHolder> implements ItemDragCallback.ItemTouchHelperAdapter {
    public final static int TYPE_ASSET = 0;
    public final static int TYPE_DRAWABLE = 1;
    private final ColorFilter colorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
    private List<ButtonData> items;
    private List<String> urlsToAssets;
    private int type = -1;

    public PanelItemAdapter(List<ButtonData> items, List<String> urlsToAssets, int type) {
        this.items = items;
        this.urlsToAssets = urlsToAssets;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_panel_advanced_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ButtonData item = items.get(position);
        if (type == TYPE_ASSET) {
            ImageLoader.getInstance().loadImage(urlsToAssets.get(position), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.button.setImageBitmap(loadedImage);
                }
            });
        } else if (type == TYPE_DRAWABLE) {
            holder.button.setImageDrawable(App.getAppDrawable(item.getIconRes()));
            holder.button.setColorFilter(colorFilter);
        }
        if (item.getTitle() == null) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setText(item.getTitle());
            holder.title.setVisibility(View.VISIBLE);
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ButtonData item);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageButton button;
        public TextView title;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            button = (ImageButton) view.findViewById(R.id.item_icon);
            title = (TextView) view.findViewById(R.id.item_title);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(items.get(getLayoutPosition()));
            }
        }
    }

    @Override
    public void onItemDismiss(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        Log.e("SUKA", "onItemMove");
    }

}
