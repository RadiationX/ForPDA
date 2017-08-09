package forpdateam.ru.forpda.fragments.devdb.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.ndevdb.models.Brands;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandsAdapter extends SectionedRecyclerViewAdapter<BrandsAdapter.ViewHolder> {
    private List<Pair<String, List<Brands.Item>>> sections = new ArrayList<>();
    private int titleColorNew, titleColor;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        titleColor = App.getColorFromAttr(recyclerView.getContext(), R.attr.second_text_color);
        titleColorNew = App.getColorFromAttr(recyclerView.getContext(), R.attr.default_text_color);
    }

    public void addSection(Pair<String, List<Brands.Item>> item) {
        sections.add(item);
    }

    public void clear() {
        for (Pair<String, List<Brands.Item>> pair : sections)
            pair.second.clear();
        sections.clear();
    }

    private BrandsAdapter.OnItemClickListener itemClickListener;
    private BrandsAdapter.OnItemClickListener longItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Brands.Item item);
    }

    public void setOnItemClickListener(final BrandsAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final BrandsAdapter.OnItemClickListener longItemClickListener) {
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
    public BrandsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = VIEW_TYPE_ITEM;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.brands_item_section;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.brands_item;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new BrandsAdapter.ViewHolder(v);
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        return super.getItemViewType(section, relativePosition, absolutePosition);
    }

    @Override
    public void onBindHeaderViewHolder(BrandsAdapter.ViewHolder holder, int section) {
        // Setup header view.
        /*if (sections.size() == 1) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }*/
        if (holder.topDivider != null) {
            holder.topDivider.setVisibility(section == 0 ? View.GONE : View.VISIBLE);
        }
        holder.title.setText(sections.get(section).first);
    }

    @Override
    public void onBindViewHolder(BrandsAdapter.ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        Brands.Item item = sections.get(section).second.get(relativePosition);
        holder.title.setText(item.getTitle());
        holder.count.setText(Integer.toString(item.getCount()));

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView title, count;
        public View topDivider;

        public ViewHolder(View v) {
            super(v);
            topDivider = v.findViewById(R.id.item_top_divider);
            title = (TextView) v.findViewById(R.id.item_title);
            count = (TextView) v.findViewById(R.id.item_count);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                int position[] = BrandsAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    itemClickListener.onItemClick(sections.get(position[0]).second.get(position[1]));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longItemClickListener != null) {
                int position[] = BrandsAdapter.this.getPosition(getLayoutPosition());
                if (position[0] != -1) {
                    longItemClickListener.onItemClick(sections.get(position[0]).second.get(position[1]));
                }
                return true;
            }
            return false;
        }
    }
}
