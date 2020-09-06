package forpdateam.ru.forpda.ui.fragments.mentions;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 21.01.17.
 */

class MentionsAdapter extends BaseAdapter<MentionItem, MentionsAdapter.MentionHolder> {
    private int titleColorNew, titleColor;
    private BaseAdapter.OnItemClickListener<MentionItem> itemClickListener;

    public void setOnItemClickListener(BaseAdapter.OnItemClickListener<MentionItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        titleColor = App.getColorFromAttr(recyclerView.getContext(), R.attr.second_text_color);
        titleColorNew = App.getColorFromAttr(recyclerView.getContext(), R.attr.default_text_color);
    }

    @Override
    public MentionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.topic_item);
        return new MentionHolder(v);
    }

    @Override
    public void onBindViewHolder(MentionHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class MentionHolder extends BaseViewHolder<MentionItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView forumIcon, lockIcon, pollIcon;

        MentionHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.topic_item_title);
            desc = (TextView) v.findViewById(R.id.topic_item_desc);
            lastNick = (TextView) v.findViewById(R.id.topic_item_last_nick);
            date = (TextView) v.findViewById(R.id.topic_item_date);
            forumIcon = (ImageView) v.findViewById(R.id.topic_item_forum_icon);
            lockIcon = (ImageView) v.findViewById(R.id.topic_item_lock_icon);
            pollIcon = (ImageView) v.findViewById(R.id.topic_item_poll_icon);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(MentionItem item, int position) {
            title.setText(item.getTitle());
            title.setTypeface(item.getState() == MentionItem.Companion.getSTATE_UNREAD() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            title.setTextColor(item.getState() == MentionItem.Companion.getSTATE_UNREAD() ? titleColorNew : titleColor);
            lastNick.setText(item.getNick());
            date.setText(item.getDate());
            if (desc.getVisibility() == View.VISIBLE) {
                desc.setVisibility(View.GONE);
            }
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
