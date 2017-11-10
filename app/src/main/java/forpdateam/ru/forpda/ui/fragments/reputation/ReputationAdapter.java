package forpdateam.ru.forpda.ui.fragments.reputation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 20.03.17.
 */


public class ReputationAdapter extends BaseAdapter<RepItem, ReputationAdapter.ReputationHolder> {
    private BaseAdapter.OnItemClickListener<RepItem> itemClickListener;

    public void setOnItemClickListener(final BaseAdapter.OnItemClickListener<RepItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public ReputationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.reputation_item);
        return new ReputationHolder(v);
    }

    @Override
    public void onBindViewHolder(ReputationHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class ReputationHolder extends BaseViewHolder<RepItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView image;

        public ReputationHolder(View v) {
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
        public void bind(RepItem item, int position) {
            title.setText(item.getTitle());
            lastNick.setText(item.getUserNick());
            date.setText(item.getDate());
            if (item.getSourceUrl() == null) {
                desc.setVisibility(View.GONE);
            } else {
                desc.setVisibility(View.VISIBLE);
                desc.setText(item.getSourceTitle());
            }
            ImageLoader.getInstance().displayImage(item.getImage(), image);
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
