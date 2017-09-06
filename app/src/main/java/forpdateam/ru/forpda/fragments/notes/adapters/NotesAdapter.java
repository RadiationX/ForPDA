package forpdateam.ru.forpda.fragments.notes.adapters;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.data.models.notes.NoteItem;

/**
 * Created by radiationx on 06.09.17.
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<NoteItem> items = new ArrayList<>();
    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addAll(Collection<NoteItem> items) {
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
        public final static int LAYOUT = R.layout.item_note;
        private TextView title;
        private TextView date;
        private TextView content;

        public DefaultHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            date = (TextView) itemView.findViewById(R.id.item_date);
            content = (TextView) itemView.findViewById(R.id.item_content);
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

        public void bind(NoteItem item, int position) {
            title.setText(item.getTitle());
            if (item.getContent() == null || item.getContent().length() == 0) {
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
                content.setText(item.getContent());
            }
            //date.setText(item.getDate());
        }
    }

    public interface ClickListener {
        void onItemClick(NoteItem item, int position);

        boolean onLongItemClick(NoteItem item, int position);
    }
}