package forpdateam.ru.forpda.views.adapters;

import android.view.View;

import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseSectionedViewHolder<T> extends SectionedViewHolder {
    public BaseSectionedViewHolder(View itemView) {
        super(itemView);
    }


    public void bind(T item, int section, int relativePosition, int absolutePosition) {
    }

    public void bind(T item, int section) {
    }

    public void bind(T item) {
    }

    public void bind(int section) {
    }

    public void bind() {
    }
}
