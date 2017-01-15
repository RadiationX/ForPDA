package forpdateam.ru.forpda.fragments.favorites;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import io.realm.RealmList;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private RealmList<FavItem> list;

    public FavoritesAdapter(){
        list = new RealmList<>();
    }

    private FavoritesAdapter.OnItemClickListener itemClickListener;
    private FavoritesAdapter.OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(FavItem favItem);
    }

    public void setOnItemClickListener(final FavoritesAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(FavItem favItem);
    }

    public void setOnLongItemClickListener(final FavoritesAdapter.OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
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

    /*public FavoritesAdapter(List<FavItem> list) {
        this.list = list;
    }
    public FavoritesAdapter() {
        this.list = new RealmList<>();
    }*/

    public void addAll(Collection<FavItem> results) {
        addAll(results, true);
    }

    public void addAll(Collection<FavItem> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }
    public void clear() {
        list.clear();
    }

    @Override
    public FavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new FavoritesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapter.ViewHolder holder, int position) {
        FavItem item = list.get(position);
        Log.d("kek", "bind view holder " + position + " : " + item.getFavId());
        holder.title.setText(item.getTopicTitle());
        holder.title.setTypeface(null, item.isNewMessages() ? Typeface.BOLD : Typeface.NORMAL);
        holder.pinIcon.setVisibility(item.isPin() ? View.VISIBLE : View.GONE);
        holder.lockIcon.setVisibility(item.getInfo().contains("X") ? View.VISIBLE : View.GONE);
        holder.pollIcon.setVisibility(item.getInfo().contains("^") ? View.VISIBLE : View.GONE);
        /*if (item.getInfo().contains("+^"))
            holder.pollIcon.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        else
            holder.pollIcon.clearColorFilter();*/

        holder.lastNick.setText(item.getLastUserNick());
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public FavItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
