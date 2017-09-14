package forpdateam.ru.forpda.fragments.devdb.device.posts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.devdb.models.Device;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 09.08.17.
 */

public class PostsAdapter extends BaseAdapter<Device.PostItem, PostsAdapter.PostHolder> {
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

    @Override
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, getLayout());;
        return new PostHolder(v);
    }

    @Override
    public void onBindViewHolder(PostHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class PostHolder extends BaseViewHolder<Device.PostItem> implements View.OnClickListener {
        public TextView title;
        public TextView date;
        public TextView desc;
        public ImageView image;

        PostHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            image = (ImageView) v.findViewById(R.id.item_image);
            v.setOnClickListener(this);
        }

        @Override
        public void bind(Device.PostItem item, int position) {
            title.setText(item.getTitle());
            date.setText(item.getDate());
            if (desc != null) {
                if (item.getDesc() != null) {
                    desc.setText(Utils.spannedFromHtml(item.getDesc()));
                    desc.setVisibility(View.VISIBLE);
                } else {
                    desc.setVisibility(View.GONE);
                }
            }
            if (image != null && item.getImage() != null) {
                ImageLoader.getInstance().displayImage(item.getImage(), image);
            }
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
