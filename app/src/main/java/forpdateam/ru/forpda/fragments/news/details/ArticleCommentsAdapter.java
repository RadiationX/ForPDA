package forpdateam.ru.forpda.fragments.news.details;

import android.support.v7.widget.RecyclerView;
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

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleCommentsAdapter extends RecyclerView.Adapter<ArticleCommentsAdapter.ViewHolder> {
    private ArrayList<Comment> list = new ArrayList<>();

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
    public ArticleCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_comment_item, parent, false);
        return new ArticleCommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArticleCommentsAdapter.ViewHolder holder, int position) {
        Comment item = list.get(position);
        holder.content.setText(item.getContent());
        holder.nick.setText(item.getUserNick());
        holder.date.setText(item.getDate());
        holder.likeCount.setVisibility(View.GONE);
        holder.likeImage.setVisibility(View.GONE);
        holder.itemView.setPadding(App.px8 * item.getLevel(), 0, 0, 0);
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

        public ViewHolder(View v) {
            super(v);
            content = (TextView) v.findViewById(R.id.comment_content);
            nick = (TextView) v.findViewById(R.id.comment_nick);
            date = (TextView) v.findViewById(R.id.comment_date);
            likeCount = (TextView) v.findViewById(R.id.comment_like_count);
            likeImage = (ImageView) v.findViewById(R.id.comment_like_image);
        }
    }
}
