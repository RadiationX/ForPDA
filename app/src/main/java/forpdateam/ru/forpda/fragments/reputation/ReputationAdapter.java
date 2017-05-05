package forpdateam.ru.forpda.fragments.reputation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.reputation.models.RepItem;

/**
 * Created by radiationx on 20.03.17.
 */


public class ReputationAdapter extends RecyclerView.Adapter<ReputationAdapter.ViewHolder> {

    private List<RepItem> list = new ArrayList<>();

    private ReputationAdapter.OnItemClickListener itemClickListener;
    private ReputationAdapter.OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(RepItem contact);
    }

    public void addAll(Collection<RepItem> results) {
        addAll(results, true);
    }

    public void addAll(Collection<RepItem> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    public void setOnItemClickListener(final ReputationAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final ReputationAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView image;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.rep_item_title);
            desc = (TextView) v.findViewById(R.id.rep_item_desc);
            lastNick = (TextView) v.findViewById(R.id.rep_item_last_nick);
            date = (TextView) v.findViewById(R.id.rep_item_date);
            image = (ImageView) v.findViewById(R.id.rep_item_image);
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
    public ReputationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reputation_item, parent, false);
        return new ReputationAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReputationAdapter.ViewHolder holder, int position) {
        RepItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.lastNick.setText(item.getUserNick());
        holder.date.setText(item.getDate());
        if (item.getSourceUrl() == null) {
            holder.desc.setVisibility(View.GONE);
        } else {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(item.getSourceTitle());
        }
        ImageLoader.getInstance().displayImage(item.getImage(), holder.image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public RepItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
