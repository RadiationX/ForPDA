package forpdateam.ru.forpda.fragments.topics;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;

/**
 * Created by radiationx on 01.03.17.
 */

public class TopicsAdapter extends SectionedRecyclerViewAdapter<TopicsAdapter.ViewHolder> {
    private final static int VIEW_TYPE_ANNOUNCE = 0;
    private List<Pair<String, List<TopicItem>>> sections = new ArrayList<>();
    private TopicsAdapter.OnItemClickListener itemClickListener;
    private TopicsAdapter.OnItemClickListener longItemClickListener;

    public void addItems(Pair<String, List<TopicItem>> item) {
        sections.add(item);
    }

    public void clear() {
        for (Pair<String, List<TopicItem>> pair : sections)
            pair.second.clear();
        sections.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(TopicItem theme);
    }

    public void setOnItemClickListener(final TopicsAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final TopicsAdapter.OnItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    private int[] getPosition(int layPos) {
        int result[] = new int[]{-1, -1};
        int sumPrevSections = 0;
        for (int i = 0; i < getSectionCount(); i++) {
            result[0] = i;
            result[1] = layPos - i - sumPrevSections - 1;
            sumPrevSections += getItemCount(i);
            if (sumPrevSections + i >= layPos) break;
        }
        if (result[1] < 0) {
            result[0] = -1;
            result[1] = -1;
        }
        return result;
    }

    @Override
    public int getSectionCount() {
        return sections.size(); // number of sections.
    }

    @Override
    public int getItemCount(int section) {
        return sections.get(section).second.size(); // number of items in section (section index is parameter).
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = VIEW_TYPE_ITEM;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.topic_item_section;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.topic_item;
                break;
            case VIEW_TYPE_ANNOUNCE:
                layout = R.layout.topic_item_announce;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new forpdateam.ru.forpda.fragments.topics.TopicsAdapter.ViewHolder(v);
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        TopicItem item = sections.get(section).second.get(relativePosition);
        if (item.isAnnounce()||item.isForum())
            return VIEW_TYPE_ANNOUNCE; // VIEW_TYPE_HEADER is -2, VIEW_TYPE_ITEM is -1. You can return 0 or greater.
        return super.getItemViewType(section, relativePosition, absolutePosition);
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder holder, int section) {
        // Setup header view.
        /*if (sections.size() == 1) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }*/
        holder.title.setText(sections.get(section).first);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int section, int relativePosition, int absolutePosition) {

        TopicItem item = sections.get(section).second.get(relativePosition);
        holder.title.setText(item.getTitle());
        if (getItemViewType(section, relativePosition, absolutePosition) != VIEW_TYPE_ANNOUNCE) {
            holder.title.setTypeface((item.getParams() & TopicItem.NEW_POST) != 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            if (false && item.getDesc() != null) {
                holder.desc.setVisibility(View.VISIBLE);
                holder.desc.setText(item.getDesc());
            } else {
                holder.desc.setVisibility(View.GONE);
            }
            //holder.pinIcon.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);
            holder.lockIcon.setVisibility((item.getParams() & TopicItem.CLOSED) != 0 ? View.VISIBLE : View.GONE);
            holder.pollIcon.setVisibility((item.getParams() & TopicItem.POLL) != 0 ? View.VISIBLE : View.GONE);
            holder.lastNick.setText(item.getLastUserNick());
            holder.date.setText(item.getDate());
        }


        // Setup non-header view.
        // 'section' is section index.
        // 'relativePosition' is index in this section.
        // 'absolutePosition' is index out of all non-header items.
        // See sample project for a visual of how these indices work.
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView title, lastNick, date, desc;
        ImageView pinIcon, lockIcon, pollIcon;

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
                int position[] = TopicsAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    itemClickListener.onItemClick(sections.get(position[0]).second.get(position[1]));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                int position[] = TopicsAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    longItemClickListener.onItemClick(sections.get(position[0]).second.get(position[1]));
                }
                return true;
            }
            return false;
        }
    }


}
