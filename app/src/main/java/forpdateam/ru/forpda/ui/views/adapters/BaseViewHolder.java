package forpdateam.ru.forpda.ui.views.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(T item, int section) {
    }

    public void bind(T item) {
    }

    public void bind(int position) {
    }

    public void bind() {
    }
}
