package forpdateam.ru.forpda.fragments.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 02.02.17.
 */

class SearchAdapter extends BaseAdapter<SearchItem, SearchAdapter.SearchHolder> {
    private BaseAdapter.OnItemClickListener<SearchItem> itemClickListener;

    public void setOnItemClickListener(BaseAdapter.OnItemClickListener<SearchItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class SearchHolder extends BaseViewHolder<SearchItem> implements View.OnClickListener, View.OnLongClickListener {
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

}
