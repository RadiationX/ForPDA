package forpdateam.ru.forpda.fragments.news.main.timeline;

import android.support.v4.view.ViewCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.data.news.entity.News;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int COMPAT_LAYOUT = 1;
    private static final int FULL_LAYOUT = 2;

    private ArrayList<News> items = new ArrayList<>();
    private NewsListAdapter.ItemClickListener mItemClickListener;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FULL_LAYOUT) {
            return new FullHolder(getItemLayout(parent, R.layout.news_main_full_item_layout));
        } else if (viewType == COMPAT_LAYOUT) {
            return new CompatHolder(getItemLayout(parent, R.layout.news_main_compat_item_layout));
        }
        throw new IllegalArgumentException("Еблан что ле? Чего ты тут заслал, мудила. Смотри внимательней. Сучка!");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (COMPAT_LAYOUT == getItemViewType(position)) {
            ((CompatHolder) holder).bind(items.get(position), position);
        } else if (FULL_LAYOUT == getItemViewType(position)) {
            ((FullHolder) holder).bind(items.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return FULL_LAYOUT;
    }

    public void setOnClickListener(NewsListAdapter.ItemClickListener onClickListener) {
        this.mItemClickListener = onClickListener;
    }

    public News getItem(int position) {
        return items.get(position);
    }

    public void insertData(List<News> list) {
        final NewsDiffCallback diffCallback = new NewsDiffCallback(items, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.items.clear();
        this.items.addAll(list);
        this.items.addAll(0, list);
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateData(List<News> list) {
        final NewsDiffCallback diffCallback = new NewsDiffCallback(items, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.items.clear();
        this.items.addAll(list);
        this.items.addAll(0, list);
        diffResult.dispatchUpdatesTo(this);
    }

    public void clear() {
        items.clear();
    }

    private View getItemLayout(ViewGroup parent,int id) {
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
        }

        public void bind(News item, int position) {
            clickContainer.setOnClickListener(v -> {
                ViewCompat.setTransitionName(cover, String.valueOf(position) + "_image");
                mItemClickListener.itemClick(cover, position);
            });
            title.setText(item.title);
            description.setText(item.description);
            ImageLoader.getInstance().displayImage(item.imgUrl, cover);
            username.setText(item.author);
            date.setText(item.date);
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
        }

        public void bind(News news, int position) {
            if (news.newNews && nContainer.getVisibility() == View.GONE) {
                nContainer.setVisibility(View.VISIBLE);
            }
            username.setText(news.author);
            category.setText(news.category);
            title.setText(news.title);
            description.setText(news.description);
            commentsCount.setText(news.commentsCount);
            date.setText(news.date);
            ImageLoader.getInstance().displayImage(news.imgUrl, cover);
            root.setOnClickListener(v -> {

                mItemClickListener.itemClick(cover, position);
            });
        }
    }

    public interface ItemClickListener {
        void itemClick(View view, int position);
    }
}
