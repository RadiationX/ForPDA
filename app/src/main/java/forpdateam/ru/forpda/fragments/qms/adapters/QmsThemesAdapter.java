package forpdateam.ru.forpda.fragments.qms.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesAdapter extends RecyclerView.Adapter<QmsThemesAdapter.ViewHolder> {
    private List<IQmsTheme> list = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnItemClickListener longItemClickListener;

    public void addAll(Collection<? extends IQmsTheme> results) {
        addAll(results, true);
    }

    public void addAll(Collection<? extends IQmsTheme> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(IQmsTheme theme);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final OnItemClickListener longItemClickListener) {
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
    public QmsThemesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.qms_theme_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IQmsTheme item = list.get(position);

        holder.name.setText(item.getName());
        holder.name.setTypeface(item.getCountNew() > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        if (item.getCountNew() == 0) {
            holder.count.setVisibility(View.GONE);
        } else {
            holder.count.setText(Integer.toString(item.getCountNew()));
            holder.count.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public IQmsTheme getItem(int position) {
        return list.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
