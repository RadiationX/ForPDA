package forpdateam.ru.forpda.fragments.devdb.device.posts;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 09.08.17.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private ArrayList<Device.PostItem> list = new ArrayList<>();
    private int source = 0;

    public void setSource(int source) {
        this.source = source;
    }

    public int getLayout() {
        if (source == PostsFragment.SRC_NEWS) {
            return R.layout.device_post_news_item;
        }
        return R.layout.device_post_forum_item;
    }

    public void addAll(Collection<Device.PostItem> results) {
        addAll(results, true);
    }

    public void addAll(Collection<Device.PostItem> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }


    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getLayout(), parent, false);
        return new PostsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostsAdapter.ViewHolder holder, int position) {
        Device.PostItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getDate());
        if (holder.desc != null) {
            if (item.getDesc() != null) {
                holder.desc.setText(Utils.spannedFromHtml(item.getDesc()));
                holder.desc.setVisibility(View.VISIBLE);
            } else {
                holder.desc.setVisibility(View.GONE);
            }
        }
        if (holder.image != null && item.getImage() != null) {
            ImageLoader.getInstance().displayImage(item.getImage(), holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Device.PostItem getItem(int position) {
        return list.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView date;
        public TextView desc;
        public ImageView image;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            image = (ImageView) v.findViewById(R.id.item_image);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String url;
            if (source == PostsFragment.SRC_NEWS) {
                url = "https://4pda.ru/index.php?p=" + getItem(getLayoutPosition()).getId();
            } else {
                url = "https://4pda.ru/forum/index.php?showtopic=" + getItem(getLayoutPosition()).getId();
            }
            IntentHandler.handle(url);
        }
    }
}
