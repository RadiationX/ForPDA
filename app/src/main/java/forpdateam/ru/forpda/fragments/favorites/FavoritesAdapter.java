package forpdateam.ru.forpda.fragments.favorites;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.views.adapters.BaseSectionedAdapter;
import forpdateam.ru.forpda.views.adapters.BaseSectionedViewHolder;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesAdapter extends BaseSectionedAdapter<IFavItem, BaseSectionedViewHolder> {
    private boolean showDot = Preferences.Lists.Topic.isShowDot();
    private int titleColorNew, titleColor;
    private BaseSectionedAdapter.OnItemClickListener<IFavItem> itemClickListener;

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
    }

    public boolean isShowDot() {
        return showDot;
    }

    public void setOnItemClickListener(BaseSectionedAdapter.OnItemClickListener<IFavItem> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        titleColor = App.getColorFromAttr(recyclerView.getContext(), R.attr.second_text_color);
        titleColorNew = App.getColorFromAttr(recyclerView.getContext(), R.attr.default_text_color);
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
        IFavItem item = getItem(section, relativePosition);
        ((ItemHolder) holder).bind(item, section, relativePosition, absolutePosition);
    }

    private class HeaderHolder extends BaseSectionedViewHolder<IFavItem> {
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

    private class ItemHolder extends BaseSectionedViewHolder<IFavItem> implements View.OnClickListener, View.OnLongClickListener {
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
        public void bind(IFavItem item, int section, int relativePosition, int absolutePosition) {
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
                IFavItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                IFavItem item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemLongClick(item);
                    return true;
                }
            }
            return false;
        }
    }
}
