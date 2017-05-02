package forpdateam.ru.forpda.fragments.favorites;

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

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesAdapter extends SectionedRecyclerViewAdapter<FavoritesAdapter.ViewHolder> {
    private List<Pair<String, List<IFavItem>>> sections = new ArrayList<>();
    private boolean showDot = App.getInstance().getPreferences().getBoolean("lists.topic.show_dot", false);

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
    }

    public boolean isShowDot() {
        return showDot;
    }

    public void addSection(Pair<String, List<IFavItem>> item) {
        sections.add(item);
    }

    public void clear() {
        for (Pair<String, List<IFavItem>> pair : sections)
            pair.second.clear();
        sections.clear();
    }

    private FavoritesAdapter.OnItemClickListener itemClickListener;
    private FavoritesAdapter.OnLongItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(IFavItem IFavItem);
    }

    public void setOnItemClickListener(final FavoritesAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(IFavItem IFavItem);
    }

    public void setOnLongItemClickListener(final FavoritesAdapter.OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    //NEW---------------------------------------------------------------------------------------------------------


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
    public FavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = VIEW_TYPE_ITEM;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.topic_item_section;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.topic_item;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new FavoritesAdapter.ViewHolder(v);
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        return super.getItemViewType(section, relativePosition, absolutePosition);
    }

    @Override
    public void onBindHeaderViewHolder(FavoritesAdapter.ViewHolder holder, int section) {
        // Setup header view.
        /*if (sections.size() == 1) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }*/
        holder.title.setText(sections.get(section).first);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapter.ViewHolder holder, int section, int relativePosition, int absolutePosition) {

        IFavItem item = sections.get(section).second.get(relativePosition);
        holder.title.setText(item.getTopicTitle());
        holder.title.setTypeface(item.isNewMessages() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        holder.dot.setVisibility(showDot && item.isNewMessages() ? View.VISIBLE : View.GONE);
        //holder.pinIcon.setVisibility(item.isPin() ? View.VISIBLE : View.GONE);
        holder.lockIcon.setVisibility(item.getInfo().contains("X") ? View.VISIBLE : View.GONE);
        holder.pollIcon.setVisibility(item.getInfo().contains("^") ? View.VISIBLE : View.GONE);
        /*if (item.getInfo().contains("+^"))
            holder.pollIcon.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        else
            holder.pollIcon.clearColorFilter();*/

        holder.lastNick.setText(item.getLastUserNick());
        holder.date.setText(item.getDate());
        if (holder.desc.getVisibility() == View.VISIBLE) {
            holder.desc.setVisibility(View.GONE);
        }


        // Setup non-header view.
        // 'section' is section index.
        // 'relativePosition' is index in this section.
        // 'absolutePosition' is index out of all non-header items.
        // See sample project for a visual of how these indices work.
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, lastNick, date, desc;
        public ImageView pinIcon, lockIcon, pollIcon;
        public View dot;

        public ViewHolder(View v) {
            super(v);
            dot = v.findViewById(R.id.topic_item_dot);
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
                int position[] = FavoritesAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    itemClickListener.onItemClick(sections.get(position[0]).second.get(position[1]));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                int position[] = FavoritesAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    longItemClickListener.onLongItemClick(sections.get(position[0]).second.get(position[1]));
                }
                return true;
            }
            return false;
        }
    }
}
