package forpdateam.ru.forpda.fragments.history.adapters;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.bdobjects.HistoryItemBd;

/**
 * Created by radiationx on 06.09.17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<HistoryItemBd> items = new ArrayList<>();
    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addAll(Collection<HistoryItemBd> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    private View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DefaultHolder(inflateLayout(parent, DefaultHolder.LAYOUT));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((DefaultHolder) holder).bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private class DefaultHolder extends RecyclerView.ViewHolder {
        public final static int LAYOUT = R.layout.item_history;
        private TextView title;
        private TextView date;

        public DefaultHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            date = (TextView) itemView.findViewById(R.id.item_date);
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(items.get(getLayoutPosition()), getLayoutPosition());
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    return clickListener.onLongItemClick(items.get(getLayoutPosition()), getLayoutPosition());
                }
                return false;
            });
        }

        public void bind(HistoryItemBd item, int position) {
            title.setText(item.getTitle());
            date.setText(item.getDate());
        }
    }

    public interface ClickListener {
        void onItemClick(HistoryItemBd item, int position);

        boolean onLongItemClick(HistoryItemBd item, int position);
    }
}
