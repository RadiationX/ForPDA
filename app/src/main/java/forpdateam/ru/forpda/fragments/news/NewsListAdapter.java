package forpdateam.ru.forpda.fragments.news;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM = 0;
    public static final int LOADING = 1;
    private RealmList<NewsModel> list;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private boolean mIsLoadingFooterAdded = false;

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }

    public NewsListAdapter() {
        this.list = new RealmList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM:
                viewHolder = createNewsViewHolder(parent);
                break;
            case LOADING:
//                viewHolder = createLoadingViewHolder(parent);
                break;
            default:
                Log.e("NewsModel Adapter", "[ERR] type is not supported!!! type is %d: " + viewType);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                bindNewsViewHolder(holder, position);
                break;
            case LOADING:
//                bindLoadingViewHolder(viewHolder);
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

    private void add(NewsModel newsModel) {
        list.add(newsModel);
        notifyItemInserted(list.size()-1);
    }

    public void addAll(RealmResults<NewsModel> results) {
        Stream.of(results).forEach(this::add);
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

    public void addLoading(){
        mIsLoadingFooterAdded = true;
        add(new NewsModel());
    }

    public void removeLoading() {
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
}
