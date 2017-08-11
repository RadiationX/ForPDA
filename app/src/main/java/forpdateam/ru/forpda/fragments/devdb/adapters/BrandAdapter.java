package forpdateam.ru.forpda.fragments.devdb.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.models.Brand;
import forpdateam.ru.forpda.rxapi.RxApi;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {

    private List<Brand.DeviceItem> list = new ArrayList<>();

    private BrandAdapter.OnItemClickListener itemClickListener;
    private BrandAdapter.OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Brand.DeviceItem deviceItem);
    }

    public void addAll(Collection<? extends Brand.DeviceItem> results) {
        addAll(results, true);
    }

    public void addAll(Collection<? extends Brand.DeviceItem> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    public void setOnItemClickListener(final BrandAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final BrandAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }


    @Override
    public BrandAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.brand_item, parent, false);
        return new BrandAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BrandAdapter.ViewHolder holder, int position) {
        Brand.DeviceItem item = list.get(position);
        holder.title.setText(item.getTitle());
        if (item.getRating() > 0) {
            holder.rating.setText(Integer.toString(item.getRating()));
            holder.rating.getBackground().setColorFilter(RxApi.DevDb().getColorFilter(item.getRating()));
            holder.rating.setVisibility(View.VISIBLE);
        } else {
            holder.rating.setVisibility(View.GONE);
        }
        /*holder.price.setVisibility(item.getPrice() == null ? View.GONE : View.VISIBLE);
        if (item.getPrice() != null) {
            holder.price.setText(item.getPrice());
        }*/
        //holder.image.setTag(holder.progressBar);
        ImageLoader.getInstance().displayImage(item.getImageSrc(), holder.image, new SimpleImageLoadingListener() {
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
    public int getItemCount() {
        return list.size();
    }

    public Brand.DeviceItem getItem(int position) {
        return list.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title;
        public TextView rating;
        //public TextView price;
        public ImageView image;
        public ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            rating = (TextView) v.findViewById(R.id.item_rating);
            image = (ImageView) v.findViewById(R.id.item_image);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
            image.setTag(progressBar);
            rating.setBackgroundResource(App.getDrawableResAttr(v.getContext(), R.attr.count_background));
            //price = (TextView) v.findViewById(R.id.item_price);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                longItemClickListener.onItemClick(getItem(getLayoutPosition()));
                return true;
            }
            return false;
        }
    }

}
