package forpdateam.ru.forpda.ui.fragments.topics;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.topics.TopicItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedViewHolder;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsAdapter extends BaseSectionedAdapter<TopicItem, BaseSectionedViewHolder> {
    private final static int VIEW_TYPE_ANNOUNCE = 0;
    private TopicsAdapter.OnItemClickListener<TopicItem> itemClickListener;
    private int titleColorNew, titleColor;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        titleColor = App.getColorFromAttr(recyclerView.getContext(), R.attr.second_text_color);
        titleColorNew = App.getColorFromAttr(recyclerView.getContext(), R.attr.default_text_color);
    }

    public void setOnItemClickListener(TopicsAdapter.OnItemClickListener<TopicItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(inflateLayout(parent, R.layout.topic_item_section));
            case VIEW_TYPE_ITEM:
                return new ItemHolder(inflateLayout(parent, R.layout.topic_item));
            case VIEW_TYPE_ANNOUNCE:
                return new AnnounceHolder(inflateLayout(parent, R.layout.topic_item_announce));
        }
        return null;
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        TopicItem item = getItem(section, relativePosition);
        if (item.isAnnounce() || item.isForum())
            return VIEW_TYPE_ANNOUNCE;
        return super.getItemViewType(section, relativePosition, absolutePosition);
    }

    @Override
    public void onBindHeaderViewHolder(BaseSectionedViewHolder holder, int section, boolean expanded) {
        holder.bind(section);
    }

    @Override
    public void onBindViewHolder(BaseSectionedViewHolder holder, int section, int relativePosition, int absolutePosition) {
        TopicItem item = getItem(section, relativePosition);
        int viewType = getItemViewType(section, relativePosition, absolutePosition);
        if (viewType == VIEW_TYPE_ANNOUNCE) {
            ((AnnounceHolder) holder).bind(item);
        } else {
            ((ItemHolder) holder).bind(item);
        }
    }

    private class HeaderHolder extends BaseSectionedViewHolder<TopicItem> {
        TextView title;
        View topDivider;

        HeaderHolder(View v) {
            super(v);
            topDivider = v.findViewById(R.id.topic_item_top_divider);
            title = (TextView) v.findViewById(R.id.topic_item_title);
        }

        @Override
        public void bind(int section) {
            if (topDivider != null) {
                topDivider.setVisibility(section == 0 ? View.GONE : View.VISIBLE);
            }
            title.setText(sections.get(section).first);
        }
    }

    private class AnnounceHolder extends BaseSectionedViewHolder<TopicItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title;

        AnnounceHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.topic_item_title);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(TopicItem item) {
            title.setText(item.getTitle());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                TopicItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                TopicItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemLongClick(item);
                }
                return true;
            }
            return false;
        }
    }

    private class ItemHolder extends BaseSectionedViewHolder<TopicItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView forumIcon, lockIcon, pollIcon;
        View topDivider;

        ItemHolder(View v) {
            super(v);
            topDivider = v.findViewById(R.id.topic_item_top_divider);
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
        public void bind(TopicItem item) {
            title.setText(item.getTitle());
            title.setTypeface(item.isNew() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            title.setTextColor(item.isNew() ? titleColorNew : titleColor);
            if (false && item.getDesc() != null) {
                desc.setVisibility(View.VISIBLE);
                desc.setText(item.getDesc());
            } else {
                desc.setVisibility(View.GONE);
            }
            //forumIcon.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);
            lockIcon.setVisibility(item.isClosed() ? View.VISIBLE : View.GONE);
            pollIcon.setVisibility(item.isPoll() ? View.VISIBLE : View.GONE);
            lastNick.setText(item.getLastUserNick());
            date.setText(item.getDate());
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                TopicItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                TopicItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemLongClick(item);
                    return true;
                }
            }
            return false;
        }
    }
}
