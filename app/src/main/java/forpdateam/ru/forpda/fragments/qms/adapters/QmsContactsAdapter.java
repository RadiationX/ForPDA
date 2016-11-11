package forpdateam.ru.forpda.fragments.qms.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collection;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import io.realm.RealmList;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsAdapter extends RecyclerView.Adapter<QmsContactsAdapter.ViewHolder> {

    private RealmList<QmsContact> list = new RealmList<>();

    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(QmsContact contact);
    }

    public void addAll(Collection<QmsContact> results) {
        addAll(results, true);
    }

    public void addAll(Collection<QmsContact> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(QmsContact contact);
    }

    public void setOnLongItemClickListener(final OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView avatar;
        public TextView nick;
        public TextView count;

        public ViewHolder(View v) {
            super(v);
            avatar = (ImageView) v.findViewById(R.id.qms_contact_avatar);
            nick = (TextView) v.findViewById(R.id.qms_contact_nick);
            count = (TextView) v.findViewById(R.id.qms_contact_count);
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
                longItemClickListener.onLongItemClick(getItem(getLayoutPosition()));
                return true;
            }
            return false;
        }
    }

    @Override
    public QmsContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.qms_contact_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QmsContact item = list.get(position);

        holder.nick.setText(item.getNick());
        ImageLoader.getInstance().displayImage(item.getAvatar(), holder.avatar);
        if (item.getCount() == 0) {
            holder.count.setVisibility(View.GONE);
        } else {
            holder.count.setText(Integer.toString(item.getCount()));
            holder.count.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public QmsContact getItem(int position) {
        return list.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
