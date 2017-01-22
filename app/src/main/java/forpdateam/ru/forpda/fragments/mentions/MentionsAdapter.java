package forpdateam.ru.forpda.fragments.mentions;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.api.mentions.Mentions;
import forpdateam.ru.forpda.api.mentions.models.MentionItem;
import forpdateam.ru.forpda.fragments.favorites.FavoritesAdapter;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsAdapter extends RecyclerView.Adapter<MentionsAdapter.ViewHolder> {
    private List<MentionItem> list = null;

    public MentionsAdapter(List<MentionItem> list) {
        this.list = list;
    }

    private MentionsAdapter.OnItemClickListener itemClickListener;
    private MentionsAdapter.OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MentionItem favItem);
    }

    public void setOnItemClickListener(final MentionsAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(MentionItem favItem);
    }

    public void setOnLongItemClickListener(final MentionsAdapter.OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    @Override
    public MentionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new MentionsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MentionsAdapter.ViewHolder holder, int position) {
        MentionItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.title.setTypeface(null, item.getState() == MentionItem.STATE_UNREAD ? Typeface.BOLD : Typeface.NORMAL);
        holder.lastNick.setText(item.getNick());
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, lastNick, date;
        public ImageView pinIcon, lockIcon, pollIcon;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.fav_item_title);
            lastNick = (TextView) v.findViewById(R.id.fav_item_last_nick);
            date = (TextView) v.findViewById(R.id.fav_item_date);
            pinIcon = (ImageView) v.findViewById(R.id.fav_item_pin_icon);
            lockIcon = (ImageView) v.findViewById(R.id.fav_item_lock_icon);
            pollIcon = (ImageView) v.findViewById(R.id.fav_item_poll_icon);

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
    public MentionItem getItem(int position) {
        return list.get(position);
    }
}
