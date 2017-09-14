package forpdateam.ru.forpda.fragments.notes.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.data.models.notes.NoteItem;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 06.09.17.
 */

public class NotesAdapter extends BaseAdapter<NoteItem, NotesAdapter.NoteHolder> {
    private BaseAdapter.OnItemClickListener<NoteItem> clickListener;

    public void setClickListener(BaseAdapter.OnItemClickListener<NoteItem> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public NotesAdapter.NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteHolder(inflateLayout(parent, NoteHolder.LAYOUT));
    }

    @Override
    public void onBindViewHolder(NotesAdapter.NoteHolder holder, int position) {
        ((NoteHolder) holder).bind(getItem(position), position);
    }

    class NoteHolder extends BaseViewHolder<NoteItem> {
        final static int LAYOUT = R.layout.item_note;
        TextView title;
        TextView date;
        TextView content;

        NoteHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            date = (TextView) itemView.findViewById(R.id.item_date);
            content = (TextView) itemView.findViewById(R.id.item_content);
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(getItem(getLayoutPosition()));
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemLongClick(getItem(getLayoutPosition()));
                    return true;
                }
                return false;
            });
        }

        @Override
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
}