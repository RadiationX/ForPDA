package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 08.01.17.
 */

public class PanelItemAdapter extends RecyclerView.Adapter<PanelItemAdapter.ViewHolder> {
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
        if (type == TYPE_ASSET) {
            ImageLoader.getInstance().loadImage(urlsToAssets.get(position), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.button.setImageBitmap(loadedImage);
                }
            });
        } else if (type == TYPE_DRAWABLE) {
            holder.button.setImageDrawable(App.getAppDrawable(items.get(position).getIconRes()));
            holder.button.setColorFilter(colorFilter);
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

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            button = (ImageButton) view.findViewById(R.id.item_icon);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(items.get(getLayoutPosition()));
            }
        }
    }
}
