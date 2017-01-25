package forpdateam.ru.forpda.fragments.news;

import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewsListAdapter";

    public static final int ITEM = 0;
    public static final int LOADING = 1;
    private static final int VISIBLE_THRESHOLD = 5;
    private List<NewsModel> list;
    private boolean mIsLoading;
    private OnLoadMoreCallback mOnLoadMoreListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnReloadDataListener mOnReloadDataListener;
    private boolean mIsLoadingFooterAdded = false;

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }

    public interface OnLoadMoreCallback {
        void onLoadMore();
    }

    public interface OnReloadDataListener {
        void onReloadDataClick();
    }

    public NewsListAdapter() {
        this.list = new ArrayList<>();
    }

    void bindRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if (!mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        mIsLoading = true;
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM:
                viewHolder = createNewsViewHolder(parent);
                log(TAG + "onCreateViewHolder -> ITEM");
                break;
            case LOADING:
                viewHolder = createLoadMoreViewHolder(parent);
                log(TAG + "onCreateViewHolder -> PROGRESS");
                break;
            default:
                log(TAG + "NewsModel Adapter " +  "[ERR] type is not supported!!! type is %d: " + viewType);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                bindNewsViewHolder(holder, position);
                log(TAG + "onBindViewHolder -> ITEM");
                break;
            case LOADING:
                bindLoadMoreViewHolder(holder);
                log(TAG + "onBindViewHolder -> PROGRESS");
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == list.size()-1 && mIsLoadingFooterAdded) ? LOADING : ITEM;
    }

    private void add(NewsModel newsModel, boolean more) {
        list.add(newsModel);
        int size = list.size();
        notifyItemRangeChanged(more ? size+1 : size-1, 1);
        notifyDataSetChanged();
    }

    public void addAll(List<NewsModel> results, boolean more) {
        list.addAll(results);
        int size = list.size();
        notifyItemRangeChanged(more ? size+1 : size-1, results.size());
        notifyDataSetChanged();
    }

    public void remove(NewsModel newsModel) {
        int position = list.indexOf(newsModel);
        if (position > -1) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public NewsModel getNews(int position) {
        return list.get(position);
    }

    public void clear() {
        mIsLoadingFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getNews(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addMoreLoadingProgress(){
        mIsLoadingFooterAdded = true;
    }

    public void addMoreNewsLoadingItem(boolean show) {

    }

    public void removeMoreLoadingAndAddNewItems(List<NewsModel> list) {

    }

    public void removeMoreLoadingProgress() {
        mIsLoadingFooterAdded = false;

        int position = list.size() - 1;
        NewsModel newsModel = getNews(position);

        if (newsModel != null) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreCallback onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    public void setOnReloadDataListener(OnReloadDataListener onReloadDataListener) {
        this.mOnReloadDataListener = onReloadDataListener;
    }

    // --------------------------------------------------------------------------------------
    // *************************************NEWS ITEM****************************************
    // --------------------------------------------------------------------------------------

    private RecyclerView.ViewHolder createNewsViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item, parent, false);
        final NewsViewHolder holder = new NewsViewHolder(v);
        holder.card.setOnClickListener(v1 -> {
            int adapterPos = holder.getAdapterPosition();
            if(adapterPos != RecyclerView.NO_POSITION){
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(adapterPos, holder.itemView);
                }
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(adapterPos, holder.itemView);
                }
            }
        });
        return holder;
    }

    private void bindNewsViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        NewsViewHolder holder = (NewsViewHolder) viewHolder;
        final NewsModel newsModel = list.get(position);
        holder.title.setText(newsModel.getTitle());
        holder.description.setText(newsModel.getDescription());
        holder.date.setText(newsModel.getDate());

        ImageLoader.getInstance().displayImage(newsModel.getImgLink(), holder.pic);
    }

    private static class NewsViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView pic;
        TextView title;
        TextView date;
        TextView description;
        
        public NewsViewHolder(View view) {
            super(view);
            card = (CardView) view.findViewById(R.id.news_list_item_root_card);
            pic = (ImageView) view.findViewById(R.id.news_list_item_pic);
            title = (TextView) view.findViewById(R.id.news_list_item_title_tv);
            date = (TextView) view.findViewById(R.id.news_list_item_date_tv);
            description = (TextView) view.findViewById(R.id.news_list_item_description_tv);
        }
    }

    // -------------------------------------------------------------------------------------- //
    // *************************************LOAD MORE**************************************** //
    // -------------------------------------------------------------------------------------- //

    private RecyclerView.ViewHolder createLoadMoreViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_item, parent, false);
        return new MoreLoadViewHolder(v);
    }

    private void bindLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder) {
        MoreLoadViewHolder holder = (MoreLoadViewHolder) viewHolder;
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.progressBar.setIndeterminate(true);
    }

    private static class MoreLoadViewHolder extends RecyclerView.ViewHolder {

        private Button reloadBtn;
        private ImageView error_face_iv;
        private TextView error_tv;
        private ProgressBar progressBar;

        public MoreLoadViewHolder(View item) {
            super(item);
            reloadBtn = (Button) item.findViewById(R.id.retry_btn);
            error_face_iv = (ImageView) item.findViewById(R.id.error_iv);
            error_tv = (TextView) item.findViewById(R.id.error_tv);
            progressBar = (ProgressBar) item.findViewById(R.id.progressBar);
        }
    }
}
