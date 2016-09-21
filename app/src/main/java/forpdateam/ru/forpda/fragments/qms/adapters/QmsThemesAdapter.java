package forpdateam.ru.forpda.fragments.qms.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesAdapter extends RecyclerView.Adapter<QmsThemesAdapter.ViewHolder> {

    private List<QmsTheme> qmsContacts;

    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, QmsThemesAdapter adapter);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onItemClick(View view, int position, QmsThemesAdapter adapter);
    }

    public void setOnLongItemClickListener(final OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView name;
        public TextView count;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.qms_theme_name);
            count = (TextView) v.findViewById(R.id.qms_theme_count);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getLayoutPosition(), QmsThemesAdapter.this);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                longItemClickListener.onItemClick(view, getLayoutPosition(), QmsThemesAdapter.this);
                return true;
            }
            return false;
        }
    }

    public QmsThemesAdapter(List<QmsTheme> qmsContacts) {
        this.qmsContacts = qmsContacts;
    }


    @Override
    public QmsThemesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.qms_theme_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QmsTheme item = qmsContacts.get(position);

        holder.name.setText(item.getName());
        if (item.getCountNew() == null) {
            holder.count.setVisibility(View.GONE);
        } else {
            holder.count.setText(item.getCountNew());
            holder.count.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return qmsContacts.size();
    }

    public QmsTheme getItem(int position) {
        return qmsContacts.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
