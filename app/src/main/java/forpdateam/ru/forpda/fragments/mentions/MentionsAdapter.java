package forpdateam.ru.forpda.fragments.mentions;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.mentions.models.MentionItem;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsAdapter extends RecyclerView.Adapter<MentionsAdapter.ViewHolder> {
    private List<MentionItem> list = new ArrayList<>();

    public void addAll(Collection<MentionItem> results) {
        list.clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    private MentionsAdapter.OnItemClickListener itemClickListener;
    private MentionsAdapter.OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MentionItem favItem);
    }

    public void setOnItemClickListener(final MentionsAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final MentionsAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    @Override
    public MentionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_item, parent, false);
        return new MentionsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MentionsAdapter.ViewHolder holder, int position) {
        MentionItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.title.setTypeface(item.getState() == MentionItem.STATE_UNREAD ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        holder.lastNick.setText(item.getNick());
        holder.date.setText(item.getDate());
        if (holder.desc.getVisibility() == View.VISIBLE) {
            holder.desc.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, lastNick, date, desc;
        public ImageView pinIcon, lockIcon, pollIcon;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.topic_item_title);
            desc = (TextView) v.findViewById(R.id.topic_item_desc);
            lastNick = (TextView) v.findViewById(R.id.topic_item_last_nick);
            date = (TextView) v.findViewById(R.id.topic_item_date);
            pinIcon = (ImageView) v.findViewById(R.id.topic_item_pin_icon);
            lockIcon = (ImageView) v.findViewById(R.id.topic_item_lock_icon);
            pollIcon = (ImageView) v.findViewById(R.id.topic_item_poll_icon);

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

    public MentionItem getItem(int position) {
        return list.get(position);
    }
}
