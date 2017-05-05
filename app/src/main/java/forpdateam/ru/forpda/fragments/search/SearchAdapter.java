package forpdateam.ru.forpda.fragments.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.search.models.SearchItem;

/**
 * Created by radiationx on 02.02.17.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<SearchItem> list = new ArrayList<>();

    private OnItemClickListener itemClickListener;
    private OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(SearchItem item);
    }

    public void addAll(Collection<SearchItem> results) {
        addAll(results, true);
    }

    public void addAll(Collection<SearchItem> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    public void setOnItemClickListener(final SearchAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final SearchAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, nick, date, content;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.search_item_title);
            nick = (TextView) v.findViewById(R.id.search_item_last_nick);
            date = (TextView) v.findViewById(R.id.search_item_date);
            content = (TextView) v.findViewById(R.id.search_item_content);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getItem(getLayoutPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                longItemClickListener.onItemClick(getItem(getLayoutPosition()));
                return true;
            }
            return false;
        }
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, int position) {
        SearchItem item = list.get(position);

        holder.title.setText(item.getTitle());
        holder.nick.setText(item.getNick());
        holder.date.setText(item.getDate());
        if (item.getBody() != null && !item.getBody().isEmpty()) {
            holder.content.setText(item.getBody());
            if (holder.content.getVisibility() != View.VISIBLE)
                holder.content.setVisibility(View.VISIBLE);
        } else {
            if (holder.content.getVisibility() != View.GONE)
                holder.content.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public SearchItem getItem(int position) {
        return list.get(position);
    }
}
