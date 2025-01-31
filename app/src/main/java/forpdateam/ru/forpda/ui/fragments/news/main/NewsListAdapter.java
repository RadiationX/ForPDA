package forpdateam.ru.forpda.ui.fragments.news.main;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.news.NewsItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsListAdapter extends BaseAdapter<NewsItem, BaseViewHolder<?>> {
    private static final int COMPAT_LAYOUT = 1;
    private static final int FULL_LAYOUT = 2;
    private static final int LOAD_MORE_LAYOUT = 3;
    private boolean showBtn = false;
    private NewsListAdapter.ItemClickListener mItemClickListener;

    public void setOnClickListener(NewsListAdapter.ItemClickListener onClickListener) {
        this.mItemClickListener = onClickListener;
    }

    @Override
    public void addAll(Collection<? extends NewsItem> items, boolean clearList) {
        super.addAll(items, clearList);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FULL_LAYOUT:
                return new FullHolder(inflateLayout(parent, R.layout.item_news));
            case COMPAT_LAYOUT:
                return new FullHolder(inflateLayout(parent, R.layout.news_item_compat));
            case LOAD_MORE_LAYOUT:
                return new LoadMoreHolder(inflateLayout(parent, R.layout.news_item_load_more));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (COMPAT_LAYOUT == getItemViewType(position)) {
            holder.bind(getItem(position), position);
        } else if (FULL_LAYOUT == getItemViewType(position)) {
            holder.bind(getItem(position), position);
        } else if (LOAD_MORE_LAYOUT == getItemViewType(position)) {
            holder.bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) {
            return LOAD_MORE_LAYOUT;
        }
        return FULL_LAYOUT;
    }

    public void insertMore(List<? extends NewsItem> list) {
        int lastSize = items.size();
        this.items.addAll(list);
        notifyItemRangeInserted(lastSize, list.size());
        showBtn = true;
    }

    public void updateItems(List<? extends NewsItem> items) {
        for (NewsItem item : items) {
            int index = this.items.indexOf(item);
            if (index != -1) {
                notifyItemChanged(index);
            }
        }
    }

    private class CompatHolder extends BaseViewHolder<NewsItem> {
        private final LinearLayout clickContainer;
        private final TextView title;
        private final TextView description;
        private final ImageView cover;
        private final CircleImageView avatar;
        private final TextView username;
        private final TextView date;
        private final ImageButton save;
        private final ImageButton share;

        CompatHolder(View itemView) {
            super(itemView);
            clickContainer = itemView.findViewById(R.id.news_list_item_click_container);
            title = itemView.findViewById(R.id.news_list_item_title);
            description = itemView.findViewById(R.id.news_list_item_description);
            cover = itemView.findViewById(R.id.news_list_item_cover);
            avatar = itemView.findViewById(R.id.news_list_item_user_avatar);
            username = itemView.findViewById(R.id.news_list_item_username);
            date = itemView.findViewById(R.id.news_list_item_date);
            save = itemView.findViewById(R.id.news_list_item_save);
            share = itemView.findViewById(R.id.news_list_item_share);
            clickContainer.setOnClickListener(v -> {
                ViewCompat.setTransitionName(cover, getLayoutPosition() + "_image");
                mItemClickListener.onItemClick(cover, getItem(getLayoutPosition()), getLayoutPosition());
            });
        }

        @Override
        public void bind(NewsItem item, int position) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            ImageLoader.getInstance().displayImage(item.getImgUrl(), cover);
            username.setText(item.getAuthor());
            date.setText(item.getDate());
        }
    }

    private class FullHolder extends BaseViewHolder<NewsItem> {
        private final TextView username;
        private final TextView category;
        private final TextView title;
        private final TextView description;
        private final TextView commentsCount;
        private final TextView date;
        private final ImageView cover;
        private final ImageView avatar;


        FullHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.news_full_item_username);
            category = itemView.findViewById(R.id.news_full_item_category);
            title = itemView.findViewById(R.id.news_full_item_title);
            description = itemView.findViewById(R.id.news_full_item_description);
            commentsCount = itemView.findViewById(R.id.news_full_item_comments_count);
            date = itemView.findViewById(R.id.news_full_item_date);
            cover = itemView.findViewById(R.id.news_full_item_cover);
            avatar = itemView.findViewById(R.id.articleAvatar);
            itemView.setOnClickListener(v -> mItemClickListener.onItemClick(cover, getItem(getLayoutPosition()), getLayoutPosition()));
            itemView.setOnLongClickListener(v -> mItemClickListener.onLongItemClick(v, getItem(getLayoutPosition()), getLayoutPosition()));
            username.setOnClickListener(v -> mItemClickListener.onNickClick(username, getItem(getLayoutPosition()), getLayoutPosition()));

        }

        public void bind(NewsItem news, int position) {
            /*if (news.newNews && nContainer.getVisibility() == View.GONE) {
                nContainer.setVisibility(View.VISIBLE);
            }*/
            username.setText(news.getAuthor());
            //category.setText(news.category);
            title.setText(news.getTitle());
            description.setText(news.getDescription());
            commentsCount.setText(String.valueOf(news.getCommentsCount()));
            date.setText(news.getDate());
            ImageLoader.getInstance().displayImage(news.getImgUrl(), cover);
            if (news.getAvatar() == null) {
                ImageLoader.getInstance().displayImage("assets://av.png", avatar);
            } else {
                ImageLoader.getInstance().displayImage(news.getAvatar().getValue(), avatar);
            }
        }
    }

    private class LoadMoreHolder extends BaseViewHolder {
        private final LinearLayout container;
        private final Button btn;

        LoadMoreHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.nl_lm_container);
            btn = itemView.findViewById(R.id.nl_lm_btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("VSEMY PIZDOS", "RABOTAET");
                }
            });
        }

        @Override
        public void bind(int position) {
            if (showBtn && container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.GONE);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> {
                    Log.d("EBA", "CLICK");
                    btn.setVisibility(View.GONE);
                    showBtn = false;
                    container.setVisibility(View.VISIBLE);
                    mItemClickListener.onLoadMoreClick();
                });
            } else {
                btn.setOnClickListener(v -> {
                    btn.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    mItemClickListener.onLoadMoreClick();
                    Log.d("EBA", "CLICK 1");
                });
            }
        }

    }

    public interface ItemClickListener {
        boolean onLongItemClick(View view, NewsItem item, int position);

        void onItemClick(View view, NewsItem item, int position);

        void onNickClick(View view, NewsItem item, int position);

        void onLoadMoreClick();
    }
}
