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
        if (item.getImageUrl() != null) {
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
            title = (TextView) v.findViewById(R.id.search_item_title);
            nick = (TextView) v.findViewById(R.id.search_item_last_nick);
            date = (TextView) v.findViewById(R.id.search_item_date);
            content = (TextView) v.findViewById(R.id.search_item_content);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(SearchItem item, int position) {
            title.setText(item.getTitle());
            nick.setText(item.getNick());
            date.setText(item.getDate());
            String contentText = null;
            if (item.getBody() != null && !item.getBody().isEmpty()) {
                contentText = item.getBody();
            }
            if (contentText == null) {
                if (item.getDesc() != null && !item.getDesc().isEmpty()) {
                    contentText = item.getDesc();
                }
            }
            if (contentText != null) {
                content.setText(contentText);
                content.setVisibility(View.VISIBLE);
            } else {
                content.setVisibility(View.GONE);
            }
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
        private TextView username;
        private TextView category;
        private TextView title;
        private TextView description;
        private TextView commentsCount;
        private ImageView commentsIcon;
        private TextView date;
        private TextView nTitle;
        private LinearLayout nContainer;
        private ImageView cover;
        private ImageView avatar;


        FullHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.news_full_item_username);
            category = (TextView) itemView.findViewById(R.id.news_full_item_category);
            title = (TextView) itemView.findViewById(R.id.news_full_item_title);
            description = (TextView) itemView.findViewById(R.id.news_full_item_description);
            commentsCount = (TextView) itemView.findViewById(R.id.news_full_item_comments_count);
            commentsIcon = (ImageView) itemView.findViewById(R.id.news_full_item_comments_icon);
            date = (TextView) itemView.findViewById(R.id.news_full_item_date);
            nTitle = (TextView) itemView.findViewById(R.id.news_full_item_news_title);
            nContainer = (LinearLayout) itemView.findViewById(R.id.news_full_item_new_container);
            cover = (ImageView) itemView.findViewById(R.id.news_full_item_cover);
            avatar = (ImageView) itemView.findViewById(R.id.articleAvatar);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            avatar.setVisibility(View.GONE);
            commentsIcon.setVisibility(View.GONE);
            commentsCount.setVisibility(View.GONE);
        }

        public void bind(SearchItem item, int position) {
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
