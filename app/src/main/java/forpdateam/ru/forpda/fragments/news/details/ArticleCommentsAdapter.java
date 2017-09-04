package forpdateam.ru.forpda.fragments.news.details;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.models.Comment;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleCommentsAdapter extends RecyclerView.Adapter<ArticleCommentsAdapter.ViewHolder> {
    private ArrayList<Comment> list = new ArrayList<>();
    private ColorFilter likedColorFilter;
    private ColorFilter dislikedColorFilter;
    private ClickListener clickListener;

    public ClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addAll(Collection<Comment> results) {
        addAll(results, true);
    }

    public void addAll(Collection<Comment> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        likedColorFilter = new PorterDuffColorFilter(App.getColorFromAttr(recyclerView.getContext(), R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
        dislikedColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(recyclerView.getContext(), R.color.dislike_color), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public ArticleCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_comment_item, parent, false);
        return new ArticleCommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArticleCommentsAdapter.ViewHolder holder, int position) {
        Comment item = list.get(position);
        Comment.Karma karma = item.getKarma();
        //Log.d("SUKA", "ADAPTER ITEM " + item.getId() + " : " + karma);
        holder.content.setText(item.getContent());
        if (item.isDeleted()) {
            holder.itemView.setClickable(false);
            if (holder.likeImage.getVisibility() != View.GONE) {
                holder.likeImage.setVisibility(View.GONE);
            }
            if (holder.likeCount.getVisibility() != View.GONE) {
                holder.likeCount.setVisibility(View.GONE);
            }
            if (holder.nick.getVisibility() != View.GONE) {
                holder.nick.setVisibility(View.GONE);
            }
            if (holder.date.getVisibility() != View.GONE) {
                holder.date.setVisibility(View.GONE);
            }
        } else {
            holder.itemView.setClickable(true);
            if (holder.likeImage.getVisibility() != View.VISIBLE) {
                holder.likeImage.setVisibility(View.VISIBLE);
            }
            if (holder.likeCount.getVisibility() != View.VISIBLE) {
                holder.likeCount.setVisibility(View.VISIBLE);
            }

            if (holder.nick.getVisibility() != View.VISIBLE) {
                holder.nick.setVisibility(View.VISIBLE);
            }
            if (holder.date.getVisibility() != View.VISIBLE) {
                holder.date.setVisibility(View.VISIBLE);
            }

            holder.nick.setText(item.getUserNick());
            holder.date.setText(item.getDate());

            if (karma.getCount() == 0) {
                if (holder.likeCount.getVisibility() != View.GONE) {
                    holder.likeCount.setVisibility(View.GONE);
                }
            } else {
                if (holder.likeCount.getVisibility() != View.VISIBLE) {
                    holder.likeCount.setVisibility(View.VISIBLE);
                }
                holder.likeCount.setText(Integer.toString(karma.getCount()));
            }

            switch (karma.getStatus()) {
                case Comment.Karma.LIKED: {
                    holder.likeImage.setImageDrawable(holder.heart);
                    holder.likeImage.setColorFilter(likedColorFilter);
                    holder.likeImage.setClickable(false);
                    break;
                }
                case Comment.Karma.DISLIKED: {
                    holder.likeImage.setImageDrawable(holder.heart_outline);
                    holder.likeImage.setColorFilter(dislikedColorFilter);
                    holder.likeImage.setClickable(false);
                    break;
                }
                case Comment.Karma.NOT_LIKED: {
                    holder.likeImage.setImageDrawable(holder.heart_outline);
                    holder.likeImage.clearColorFilter();
                    holder.likeImage.setClickable(true);
                    break;
                }
            }
        }


        holder.itemView.setPadding(App.px12 * item.getLevel(), 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Comment getItem(int position) {
        return list.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView content;
        public TextView nick;
        public TextView date;
        public TextView likeCount;
        public ImageView likeImage;
        private Drawable heart;
        private Drawable heart_outline;

        public ViewHolder(View v) {
            super(v);
            content = (TextView) v.findViewById(R.id.comment_content);
            nick = (TextView) v.findViewById(R.id.comment_nick);
            date = (TextView) v.findViewById(R.id.comment_date);
            likeCount = (TextView) v.findViewById(R.id.comment_like_count);
            likeImage = (ImageView) v.findViewById(R.id.comment_like_image);
            heart = App.getAppDrawable(v.getContext(), R.drawable.ic_heart);
            heart_outline = App.getAppDrawable(v.getContext(), R.drawable.ic_heart_outline);
            nick.setOnClickListener(v1 -> {
                if (clickListener != null) {
                    clickListener.onNickClick(getItem(getLayoutPosition()), getLayoutPosition());
                }
            });
            likeImage.setOnClickListener(v1 -> {
                if (clickListener != null) {
                    clickListener.onLikeClick(getItem(getLayoutPosition()), getLayoutPosition());
                }
            });
            itemView.setOnClickListener(v1 -> {
                if (clickListener != null) {
                    clickListener.onReplyClick(getItem(getLayoutPosition()), getLayoutPosition());
                }
            });
        }
    }

    public interface ClickListener {
        void onNickClick(Comment comment, int position);

        void onLikeClick(Comment comment, int position);

        void onReplyClick(Comment comment, int position);
    }
}
