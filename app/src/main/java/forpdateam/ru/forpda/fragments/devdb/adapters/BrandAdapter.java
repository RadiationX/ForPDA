package forpdateam.ru.forpda.fragments.devdb.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.ndevdb.models.Brand;

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
        /*holder.price.setVisibility(item.getPrice() == null ? View.GONE : View.VISIBLE);
        if (item.getPrice() != null) {
            holder.price.setText(item.getPrice());
        }*/
        ImageLoader.getInstance().displayImage(item.getImageSrc(), holder.image, new SimpleImageLoadingListener() {

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
        //public TextView price;
        public ImageView image;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            image = (ImageView) v.findViewById(R.id.item_image);
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
