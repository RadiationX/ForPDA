package forpdateam.ru.forpda.fragments.devdb.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.models.Brands;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.views.adapters.BaseSectionedAdapter;
import forpdateam.ru.forpda.views.adapters.BaseSectionedViewHolder;

/**
 * Created by radiationx on 08.08.17.
 */

public class BrandsAdapter extends BaseSectionedAdapter<Brands.Item, BaseSectionedViewHolder> {
    private BrandsAdapter.OnItemClickListener<Brands.Item> itemClickListener;

    public void setOnItemClickListener(BrandsAdapter.OnItemClickListener<Brands.Item> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public BaseSectionedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(inflateLayout(parent, R.layout.brands_item_section));
            case VIEW_TYPE_ITEM:
                return new ItemHolder(inflateLayout(parent, R.layout.brands_item));
        }
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(BaseSectionedViewHolder holder, int section, boolean expanded) {
        ((HeaderHolder) holder).bind(section);
    }

    @Override
    public void onBindViewHolder(BaseSectionedViewHolder holder, int section, int relativePosition, int absolutePosition) {
        Brands.Item item = getItem(section, relativePosition);
        ((ItemHolder) holder).bind(item);
    }

    private class HeaderHolder extends BaseSectionedViewHolder<TopicItem> {
        TextView title;
        View topDivider;

        HeaderHolder(View v) {
            super(v);
            topDivider = v.findViewById(R.id.item_top_divider);
            title = (TextView) v.findViewById(R.id.item_title);
        }

        @Override
        public void bind(int section) {
            if (topDivider != null) {
                topDivider.setVisibility(section == 0 ? View.GONE : View.VISIBLE);
            }
            title.setText(sections.get(section).first);
        }
    }

    private class ItemHolder extends BaseSectionedViewHolder<Brands.Item> implements View.OnClickListener, View.OnLongClickListener {
        TextView title, count;
        View topDivider;

        ItemHolder(View v) {
            super(v);
            topDivider = v.findViewById(R.id.item_top_divider);
            title = (TextView) v.findViewById(R.id.item_title);
            count = (TextView) v.findViewById(R.id.item_count);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(Brands.Item item, int section, int relativePosition, int absolutePosition) {
            title.setText(item.getTitle());
            count.setText(Integer.toString(item.getCount()));
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                Brands.Item item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemClick(item);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (itemClickListener != null) {
                Brands.Item item = getItem(getLayoutPosition());
                if (item != null) {
                    itemClickListener.onItemLongClick(item);
                }
                return true;
            }
            return false;
        }
    }
}
