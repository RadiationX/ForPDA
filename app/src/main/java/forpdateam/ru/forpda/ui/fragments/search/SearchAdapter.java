package forpdateam.ru.forpda.ui.fragments.search;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.search.SearchItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 02.02.17.
 */

class SearchAdapter extends BaseAdapter<SearchItem, BaseViewHolder<SearchItem>> {
    private static final int TOPIC_LAYOUT = 1;
    private static final int NEWS_LAYOUT = 2;
    private BaseAdapter.OnItemClickListener<SearchItem> itemClickListener;

    public void setOnItemClickListener(BaseAdapter.OnItemClickListener<SearchItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        SearchItem item = getItem(position);
        if (item instanceof SearchItem.News) {
            return NEWS_LAYOUT;
        }
        return TOPIC_LAYOUT;
    }

    @Override
    public BaseViewHolder<SearchItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TOPIC_LAYOUT:
                return new SearchHolder(inflateLayout(parent, R.layout.search_item));
            case NEWS_LAYOUT:
                return new FullHolder(inflateLayout(parent, R.layout.item_news));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<SearchItem> holder, int position) {
        holder.bind(getItem(position), position);
    }

    private class SearchHolder extends BaseViewHolder<SearchItem> implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, nick, date, content;

        SearchHolder(View v) {
            super(v);
            title = v.findViewById(R.id.search_item_title);
            nick = v.findViewById(R.id.search_item_last_nick);
            date = v.findViewById(R.id.search_item_date);
            content = v.findViewById(R.id.search_item_content);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(SearchItem searchItem, int position) {
            SearchItem.Topic item = (SearchItem.Topic) searchItem;
            title.setText(item.getTitle());
            nick.setText(item.getNick());
            date.setText(item.getDate());
            String contentText = item.getDesc();
            content.setText(contentText);
            content.setVisibility(View.VISIBLE);
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

    private class FullHolder extends BaseViewHolder<SearchItem> implements View.OnClickListener, View.OnLongClickListener {
        private final TextView username;
        private final TextView category;
        private final TextView title;
        private final TextView description;
        private final TextView commentsCount;
        private final ImageView commentsIcon;
        private final TextView date;
        private final TextView nTitle;
        private final LinearLayout nContainer;
        private final ImageView cover;
        private final ImageView avatar;


        FullHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.news_full_item_username);
            category = itemView.findViewById(R.id.news_full_item_category);
            title = itemView.findViewById(R.id.news_full_item_title);
            description = itemView.findViewById(R.id.news_full_item_description);
            commentsCount = itemView.findViewById(R.id.news_full_item_comments_count);
            commentsIcon = itemView.findViewById(R.id.news_full_item_comments_icon);
            date = itemView.findViewById(R.id.news_full_item_date);
            nTitle = itemView.findViewById(R.id.news_full_item_news_title);
            nContainer = itemView.findViewById(R.id.news_full_item_new_container);
            cover = itemView.findViewById(R.id.news_full_item_cover);
            avatar = itemView.findViewById(R.id.articleAvatar);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            avatar.setVisibility(View.GONE);
            commentsIcon.setVisibility(View.GONE);
            commentsCount.setVisibility(View.GONE);
        }

        public void bind(SearchItem searchItem, int position) {
            SearchItem.News item = (SearchItem.News) searchItem;
            /*if (news.newNews && nContainer.getVisibility() == View.GONE) {
                nContainer.setVisibility(View.VISIBLE);
            }*/
            username.setText(item.getNick());
            //category.setText(news.category);
            title.setText(item.getTitle());
            description.setText(item.getBody());
            //commentsCount.setText(String.valueOf(item.getCommentsCount()));
            date.setText(item.getDate());
            ImageLoader.getInstance().displayImage(item.getImageUrl(), cover);
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
