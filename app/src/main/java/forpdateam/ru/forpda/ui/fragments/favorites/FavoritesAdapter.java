package forpdateam.ru.forpda.ui.fragments.favorites;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.entity.remote.favorites.FavItem;
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedViewHolder;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesAdapter extends BaseSectionedAdapter<FavItem, BaseSectionedViewHolder> {
    private boolean showDot = false;
    private boolean unreadTop = false;
    private int titleColorNew, titleColor;
    private String titleUnreadPinned, titleUnread, titlePinned, titleTopics;
    private BaseSectionedAdapter.OnItemClickListener<FavItem> itemClickListener;
    @Nullable
    private List<FavItem> currentItems = null;

    public void bindItems(@NotNull List<FavItem> newItems) {
        currentItems = newItems;
        ArrayList<FavItem> pinnedUnread = new ArrayList<>();
        ArrayList<FavItem> itemsUnread = new ArrayList<>();
        ArrayList<FavItem> pinned = new ArrayList<>();
        ArrayList<FavItem> otherItems = new ArrayList<>();
        for (FavItem item : newItems) {
            if (item.isPin()) {
                if (unreadTop && item.isNew()) {
                    pinnedUnread.add(item);
                } else {
                    pinned.add(item);
                }
            } else {
                if (unreadTop && item.isNew()) {
                    itemsUnread.add(item);
                } else {
                    otherItems.add(item);
                }
            }
        }

        clear();
        if (!pinnedUnread.isEmpty()) {
            addSection(titleUnreadPinned, pinnedUnread);
        }
        if (!itemsUnread.isEmpty()) {
            addSection(titleUnread, itemsUnread);
        }
        if (!pinned.isEmpty()) {
            addSection(titlePinned, pinned);
        }
        addSection(titleTopics, otherItems);
        notifyDataSetChanged();
    }

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
        if (currentItems != null) {
            bindItems(currentItems);
        }
    }

    public void setUnreadTop(boolean unreadTop){
        this.unreadTop = unreadTop;
        if (currentItems != null) {
            bindItems(currentItems);
        }
    }

    public void setOnItemClickListener(BaseSectionedAdapter.OnItemClickListener<FavItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Context context = recyclerView.getContext();
        titleColor = App.getColorFromAttr(context, R.attr.second_text_color);
        titleColorNew = App.getColorFromAttr(context, R.attr.default_text_color);
        titleUnreadPinned = context.getString(R.string.fav_unreaded_pinned);
        titleUnread = context.getString(R.string.fav_unreaded);
        titlePinned = context.getString(R.string.fav_pinned);
        titleTopics = context.getString(R.string.fav_themes);
    }

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(inflateLayout(parent, R.layout.topic_item_section));
            case VIEW_TYPE_ITEM:
                return new ItemHolder(inflateLayout(parent, R.layout.topic_item));
        }
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(BaseSectionedViewHolder holder, int section, boolean expanded) {
        holder.bind(section);
    }

    @Override
    public void onBindViewHolder(BaseSectionedViewHolder holder, int section, int relativePosition, int absolutePosition) {
        FavItem item = getItem(section, relativePosition);
        ((ItemHolder) holder).bind(item, section, relativePosition, absolutePosition);
    }

    private class HeaderHolder extends BaseSectionedViewHolder<FavItem> {
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

    private class ItemHolder extends BaseSectionedViewHolder<FavItem> implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView forumIcon, lockIcon, pollIcon;
        View dot, topDivider;

        ItemHolder(View v) {
            super(v);
            dot = v.findViewById(R.id.topic_item_dot);
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
        public void bind(FavItem item, int section, int relativePosition, int absolutePosition) {
            title.setText(item.getTopicTitle());

            title.setTypeface(item.isNew() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            title.setTextColor(item.isNew() ? titleColorNew : titleColor);
            dot.setVisibility(showDot && item.isNew() ? View.VISIBLE : View.GONE);

            forumIcon.setVisibility(item.isForum() ? View.VISIBLE : View.GONE);

            if (item.isForum()) {
                lockIcon.setVisibility(View.GONE);
                pollIcon.setVisibility(View.GONE);
            } else {
                lockIcon.setVisibility(item.isClosed() ? View.VISIBLE : View.GONE);
                pollIcon.setVisibility(item.isPoll() ? View.VISIBLE : View.GONE);
            }

            lastNick.setText(item.getLastUserNick());
            date.setText(item.getDate());
            if (desc.getVisibility() == View.VISIBLE) {
                desc.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                FavItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                FavItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemLongClick(item);
                    return true;
                }
            }
            return false;
        }
    }
}
