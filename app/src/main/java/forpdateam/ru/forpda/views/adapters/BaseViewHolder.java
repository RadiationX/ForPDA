package forpdateam.ru.forpda.views.adapters;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(T item, int section) {
    }

    public void bind(int position) {
    }

    public void bind() {
    }
}
