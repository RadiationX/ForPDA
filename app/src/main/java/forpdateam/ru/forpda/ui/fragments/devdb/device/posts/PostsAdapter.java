package forpdateam.ru.forpda.ui.fragments.devdb.device.posts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Device;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 09.08.17.
 */

public class PostsAdapter extends BaseAdapter<Device.PostItem, PostsAdapter.PostHolder> {
    private int source = 0;

    private PostHolder.Listener listener;

    public PostsAdapter(PostHolder.Listener listener) {
        this.listener = listener;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getLayout() {
        if (source == PostsFragment.SRC_NEWS) {
            return R.layout.device_post_news_item;
        }
        return R.layout.device_post_forum_item;
    }

    @Override
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, getLayout());
        return new PostHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(PostHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public static class PostHolder extends BaseViewHolder<Device.PostItem> {
        private TextView title;
        private TextView date;
        private TextView desc;
        private ImageView image;
        private Device.PostItem currentItem;

        PostHolder(View v, Listener listener) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            image = (ImageView) v.findViewById(R.id.item_image);
            v.setOnClickListener((v1 -> listener.onClick(currentItem)));
        }

        @Override
        public void bind(Device.PostItem item, int position) {
            currentItem = item;
            title.setText(item.getTitle());
            date.setText(item.getDate());
            if (desc != null) {
                if (item.getDesc() != null) {
                    desc.setText(ApiUtils.spannedFromHtml(item.getDesc()));
                    desc.setVisibility(View.VISIBLE);
                } else {
                    desc.setVisibility(View.GONE);
                }
            }
            if (image != null && item.getImage() != null) {
                ImageLoader.getInstance().displayImage(item.getImage(), image);
            }
        }

        interface Listener {
            void onClick(Device.PostItem item);
        }
    }
}
