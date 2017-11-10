package forpdateam.ru.forpda.ui.fragments.qms.adapters;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesAdapter extends BaseAdapter<IQmsTheme, QmsThemesAdapter.ThemeHolder> {
    private BaseAdapter.OnItemClickListener<IQmsTheme> itemClickListener;

    public void setOnItemClickListener(final BaseAdapter.OnItemClickListener<IQmsTheme> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public ThemeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.qms_theme_item);
        return new ThemeHolder(v);
    }

    @Override
    public void onBindViewHolder(ThemeHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class ThemeHolder extends BaseViewHolder<IQmsTheme> implements View.OnClickListener, View.OnLongClickListener {
        public TextView name;
        public TextView count;

        public ThemeHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.qms_theme_name);
            count = (TextView) v.findViewById(R.id.qms_theme_count);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(IQmsTheme item, int position) {
            name.setText(item.getName());
            name.setTypeface(item.getCountNew() > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            if (item.getCountNew() == 0) {
                count.setVisibility(View.GONE);
            } else {
                count.setText(Integer.toString(item.getCountNew()));
                count.setVisibility(View.VISIBLE);
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
