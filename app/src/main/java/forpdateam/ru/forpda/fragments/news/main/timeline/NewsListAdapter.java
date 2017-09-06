package forpdateam.ru.forpda.fragments.news.main.timeline;

import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.models.NewsItem;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int COMPAT_LAYOUT = 1;
    private static final int FULL_LAYOUT = 2;
    private static final int LOAD_MORE_LAYOUT = 3;

    private ArrayList<NewsItem> items = new ArrayList<>();
    private NewsListAdapter.ItemClickListener mItemClickListener;

    private boolean showBtn = false;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FULL_LAYOUT) {
            return new FullHolder(inflateLayout(parent, R.layout.news_main_full_item_layout));
        } else if (viewType == COMPAT_LAYOUT) {
            return new CompatHolder(inflateLayout(parent, R.layout.news_main_compat_item_layout));
        } else if (viewType == LOAD_MORE_LAYOUT) {
            return new LoadMoreHolder(inflateLayout(parent, R.layout.news_list_load_more_layout));
        }
        throw new IllegalArgumentException("Еблан что ле? Чего ты тут заслал, мудила. Смотри внимательней. Сучка!");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (COMPAT_LAYOUT == getItemViewType(position)) {
            ((CompatHolder) holder).bind(items.get(position), position);
        } else if (FULL_LAYOUT == getItemViewType(position)) {
            ((FullHolder) holder).bind(items.get(position), position);
        } else if (LOAD_MORE_LAYOUT == getItemViewType(position)) {
            ((LoadMoreHolder) holder).bind();
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) {
            return LOAD_MORE_LAYOUT;
        }
        return FULL_LAYOUT;
    }

    public void setOnClickListener(NewsListAdapter.ItemClickListener onClickListener) {
        this.mItemClickListener = onClickListener;
    }

    public NewsItem getItem(int position) {
        return items.get(position);
    }

    public void addAll(List<NewsItem> list) {
        this.items.addAll(list);
        showBtn = true;
    }


    public void clear() {
        this.items.clear();
    }

    private View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    private class CompatHolder extends RecyclerView.ViewHolder {

        private LinearLayout clickContainer;
        private TextView title;
        private TextView description;
        private ImageView cover;
        private CircleImageView avatar;
        private TextView username;
        private TextView date;
        private ImageButton save;
        private ImageButton share;

        public CompatHolder(View itemView) {
            super(itemView);
            clickContainer = (LinearLayout) itemView.findViewById(R.id.news_list_item_click_container);
            title = (TextView) itemView.findViewById(R.id.news_list_item_title);
            description = (TextView) itemView.findViewById(R.id.news_list_item_description);
            cover = (ImageView) itemView.findViewById(R.id.news_list_item_cover);
            avatar = (CircleImageView) itemView.findViewById(R.id.news_list_item_user_avatar);
            username = (TextView) itemView.findViewById(R.id.news_list_item_username);
            date = (TextView) itemView.findViewById(R.id.news_list_item_date);
            save = (ImageButton) itemView.findViewById(R.id.news_list_item_save);
            share = (ImageButton) itemView.findViewById(R.id.news_list_item_share);
            clickContainer.setOnClickListener(v -> {
                ViewCompat.setTransitionName(cover, String.valueOf(getLayoutPosition()) + "_image");
                mItemClickListener.onItemClick(cover, getItem(getLayoutPosition()), getLayoutPosition());
            });
        }

        public void bind(NewsItem item, int position) {

            title.setText(item.getTitle());
            description.setText(item.getDescription());
            ImageLoader.getInstance().displayImage(item.getImgUrl(), cover);
            username.setText(item.getAuthor());
            date.setText(item.getDate());
        }
    }

    public class FullHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private TextView category;
        private TextView title;
        private TextView description;
        private TextView commentsCount;
        private TextView date;
        private TextView nTitle;
        private LinearLayout nContainer;
        private CircleImageView cover;
        private CardView root;


        public FullHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.news_full_item_username);
            category = (TextView) itemView.findViewById(R.id.news_full_item_category);
            title = (TextView) itemView.findViewById(R.id.news_full_item_title);
            description = (TextView) itemView.findViewById(R.id.news_full_item_description);
            commentsCount = (TextView) itemView.findViewById(R.id.news_full_item_comments_count);
            date = (TextView) itemView.findViewById(R.id.news_full_item_date);
            nTitle = (TextView) itemView.findViewById(R.id.news_full_item_news_title);
            nContainer = (LinearLayout) itemView.findViewById(R.id.news_full_item_new_container);
            cover = (CircleImageView) itemView.findViewById(R.id.news_full_item_cover);
            root = (CardView) itemView.findViewById(R.id.news_full_item_root);
            itemView.setOnClickListener(v -> mItemClickListener.onItemClick(cover, getItem(getLayoutPosition()), getLayoutPosition()));
            itemView.setOnLongClickListener(v -> mItemClickListener.onLongItemClick(v, getItem(getLayoutPosition()), getLayoutPosition()));
        }

        public void bind(NewsItem news, int position) {
            /*if (news.newNews && nContainer.getVisibility() == View.GONE) {
                nContainer.setVisibility(View.VISIBLE);
            }*/
            username.setText(news.getAuthor());
            //category.setText(news.category);
            title.setText(news.getTitle());
            description.setText(news.getDescription());
            commentsCount.setText(Integer.toString(news.getCommentsCount()));
            date.setText(news.getDate());
            ImageLoader.getInstance().displayImage(news.getImgUrl(), cover);
        }
    }

    public class LoadMoreHolder extends RecyclerView.ViewHolder {

        private LinearLayout container;
        private Button btn;

        public LoadMoreHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.nl_lm_container);
            btn = (Button) itemView.findViewById(R.id.nl_lm_btn);
        }


        public void bind() {
            if (showBtn && container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.GONE);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> {
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
                });
            }
        }

    }

    public interface ItemClickListener {
        boolean onLongItemClick(View view, NewsItem item, int position);

        void onItemClick(View view, NewsItem item, int position);

        void onLoadMoreClick();
    }
}
