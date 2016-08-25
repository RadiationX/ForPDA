package forpdateam.ru.forpda.fragments.qms;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsContact;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsAdapter extends RecyclerView.Adapter<QmsContactsAdapter.ViewHolder> {

    private ArrayList<QmsContact> qmsContacts = new ArrayList<>();

    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, QmsContactsAdapter adapter);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onItemClick(View view, int position, QmsContactsAdapter adapter);
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
                itemClickListener.onItemClick(view, getLayoutPosition(), QmsContactsAdapter.this);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                longItemClickListener.onItemClick(view, getLayoutPosition(), QmsContactsAdapter.this);
                return true;
            }
            return false;
        }
    }

    public QmsContactsAdapter(ArrayList<QmsContact> qmsContacts) {
        this.qmsContacts = qmsContacts;
    }


    @Override
    public QmsContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.qms_contact_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QmsContact item = qmsContacts.get(position);

        holder.nick.setText(item.getNick());
        ImageLoader.getInstance().displayImage(item.getAvatar(), holder.avatar);
        if (item.getCount() == null) {
            holder.count.setVisibility(View.GONE);
        } else {
            holder.count.setText(item.getCount());
            holder.count.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return qmsContacts.size();
    }

    public QmsContact getItem(int position) {
        return qmsContacts.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
