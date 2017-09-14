package forpdateam.ru.forpda.fragments.qms.adapters;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsAdapter extends BaseAdapter<IQmsContact, QmsContactsAdapter.ContactHolder> {
    private BaseAdapter.OnItemClickListener<IQmsContact> itemClickListener;

    public void setOnItemClickListener(final BaseAdapter.OnItemClickListener<IQmsContact> mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.qms_contact_item);
        return new ContactHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class ContactHolder extends BaseViewHolder<IQmsContact> implements View.OnClickListener, View.OnLongClickListener {
        public ImageView avatar;
        public TextView nick;
        public TextView count;

        public ContactHolder(View v) {
            super(v);
            avatar = (ImageView) v.findViewById(R.id.qms_contact_avatar);
            nick = (TextView) v.findViewById(R.id.qms_contact_nick);
            count = (TextView) v.findViewById(R.id.qms_contact_count);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void bind(IQmsContact item, int position) {
            nick.setText(item.getNick());
            ImageLoader.getInstance().displayImage(item.getAvatar(), avatar);
            nick.setTypeface(item.getCount() > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            if (item.getCount() == 0) {
                count.setVisibility(View.GONE);
            } else {
                count.setText(Integer.toString(item.getCount()));
                count.setVisibility(View.VISIBLE);
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
