package forpdateam.ru.forpda.ui.fragments.devdb.brand;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Brand;
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 08.08.17.
 */

public class DevicesAdapter extends BaseAdapter<Brand.DeviceItem, DevicesAdapter.DeviceItemHolder> {
    private BaseAdapter.OnItemClickListener<Brand.DeviceItem> itemClickListener;

    public void setItemClickListener(OnItemClickListener<Brand.DeviceItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public DevicesAdapter.DeviceItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.brand_item);
        return new DevicesAdapter.DeviceItemHolder(v);
    }

    @Override
    public void onBindViewHolder(DevicesAdapter.DeviceItemHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class DeviceItemHolder extends BaseViewHolder<Brand.DeviceItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title;
        TextView rating;
        ImageView image;
        ProgressBar progressBar;

        DeviceItemHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            rating = (TextView) v.findViewById(R.id.item_rating);
            image = (ImageView) v.findViewById(R.id.item_image);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
            image.setTag(progressBar);
            rating.setBackground(App.getDrawableAttr(rating.getContext(), R.attr.count_background));
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(Brand.DeviceItem item, int position) {
            title.setText(item.getTitle());
            if (item.getRating() > 0) {
                rating.setText(Integer.toString(item.getRating()));
                rating.getBackground().setColorFilter(DevDbHelper.INSTANCE.getColorFilter(item.getRating()));
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(item.getImageSrc(), image, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    ProgressBar progressBar = (ProgressBar) view.getTag();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    ProgressBar progressBar = (ProgressBar) view.getTag();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ProgressBar progressBar = (ProgressBar) view.getTag();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    ProgressBar progressBar = (ProgressBar) view.getTag();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemLongClick(getItem(getLayoutPosition()));
                return true;
            }
            return false;
        }
    }

}
