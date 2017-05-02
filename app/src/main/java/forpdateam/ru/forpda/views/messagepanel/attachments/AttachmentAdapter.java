package forpdateam.ru.forpda.views.messagepanel.attachments;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {
    private List<AttachmentItem> items = new ArrayList<>();
    private List<AttachmentItem> selected = new ArrayList<>();
    private final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 0, 0, 0), PorterDuff.Mode.DST_OUT);
    private OnDataChangeListener onDataChangeListener;
    private AttachmentAdapter.OnItemClickListener itemClickListener;
    private OnSelectedListener onSelectedListener;
    private OnReloadClickListener reloadOnClickListener;


    public AttachmentAdapter() {
    }

    public List<AttachmentItem> getItems() {
        return items;
    }

    public void add(Collection<AttachmentItem> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
        if (onDataChangeListener != null)
            onDataChangeListener.onChange(items.size());
    }

    public void add(AttachmentItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
        if (onDataChangeListener != null)
            onDataChangeListener.onChange(items.size());
    }

    public void clear(){
        items.clear();
        unSelectItems();
        if (onDataChangeListener != null) {
            onDataChangeListener.onChange(items.size());
        }
    }

    public void deleteSelected() {
        for (AttachmentItem item : selected) {
            if (item.getStatus() == AttachmentItem.STATUS_REMOVED) {
                int index = items.indexOf(item);
                items.remove(index);
                notifyItemRemoved(index);
            }
        }
        unSelectItems();
        if (onDataChangeListener != null) {
            onDataChangeListener.onChange(items.size());
        }
    }

    public void unSelectItems() {
        for (AttachmentItem item : selected) {
            if (item != null) {
                if(item.isSelected()) item.toggle();
                notifyItemChanged(items.indexOf(item));
            }
        }
        selected.clear();
        if (onSelectedListener != null)
            onSelectedListener.onSelected(null, -1, 0);
    }

    public void notifyItemLoadResult(AttachmentItem item) {
        notifyItemChanged(items.indexOf(item));
    }

    public void replaceItem(AttachmentItem oldItem, AttachmentItem newItem) {
        items.set(items.lastIndexOf(oldItem), newItem);
        notifyItemChanged(items.indexOf(newItem));
    }

    public void removeItem(AttachmentItem item) {
        int index = items.indexOf(item);
        items.remove(index);
        notifyItemRemoved(index);
        if (onDataChangeListener != null)
            onDataChangeListener.onChange(items.size());
    }

    public List<AttachmentItem> getSelected() {
        return selected;
    }

    @Override
    public AttachmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_panel_attachment_item, parent, false);
        return new AttachmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AttachmentAdapter.ViewHolder holder, final int position) {
        AttachmentItem item = items.get(position);
        switch (item.getLoadState()) {
            case AttachmentItem.STATE_LOADING:
                holder.description.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.reload.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.GONE);
                break;
            case AttachmentItem.STATE_NOT_LOADED:
                holder.description.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.reload.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                break;
            case AttachmentItem.STATE_LOADED:
                holder.description.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
                holder.attributes.setText(item.getFormat().concat(", ").concat(item.getWeight()));
                holder.progressBar.setVisibility(View.GONE);
                holder.reload.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                if (item.getTypeFile() == AttachmentItem.TYPE_IMAGE) {
                    ImageLoader.getInstance().displayImage(item.getImageUrl(), holder.imageView);
                } else {
                    holder.imageView.setImageDrawable(App.getAppDrawable(R.drawable.ic_insert_drive_file_gray_24dp));
                }
                break;
        }
        updateChecked(holder, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(final AttachmentAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public void setReloadOnClickListener(OnReloadClickListener reloadOnClickListener) {
        this.reloadOnClickListener = reloadOnClickListener;
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        this.onDataChangeListener = onDataChangeListener;
    }


    public interface OnItemClickListener {
        void onItemClick(AttachmentItem item);
    }

    public interface OnReloadClickListener {
        void onReloadClick(AttachmentItem item);
    }

    public interface OnSelectedListener {
        void onSelected(AttachmentItem item, int index, int selected);
    }

    public interface OnDataChangeListener {
        void onChange(int count);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        RadioButton radioButton;
        View overlay;
        ProgressBar progressBar;
        ImageButton reload;
        TextView name, attributes;
        public LinearLayout description;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            imageView = (ImageView) view.findViewById(R.id.icon);
            radioButton = (RadioButton) view.findViewById(R.id.radio_button);
            overlay = view.findViewById(R.id.overlay_and_text);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            reload = (ImageButton) view.findViewById(R.id.reload);
            name = (TextView) view.findViewById(R.id.file_name);
            attributes = (TextView) view.findViewById(R.id.file_attributes);
            description = (LinearLayout) view.findViewById(R.id.file_description);

            reload.setOnClickListener(v -> {
                if (reloadOnClickListener != null) {
                    reloadOnClickListener.onReloadClick(items.get(getLayoutPosition()));
                }
            });
        }


        @Override
        public void onClick(View v) {
            /*boolean canUpdateChecked = true;
            for (AttachmentItem item : items) {
                if (item.getLoadState() == AttachmentItem.STATE_LOADING||item.getLoadState() == AttachmentItem.STATE_NOT_LOADED) {
                    canUpdateChecked = false;
                    break;
                }
            }
            if (canUpdateChecked) {

            }*/
            AttachmentItem item = items.get(getLayoutPosition());
            /*if (item.getLoadState() == AttachmentItem.STATE_LOADED) {

            }*/
            item.toggle();
            updateChecked(this, getLayoutPosition());

            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        }
    }

    private void updateChecked(ViewHolder holder, int position) {
        AttachmentItem item = items.get(position);
        if (item.isSelected()) {
            if (!selected.contains(item)) {
                selected.add(item);
            }
        } else {
            selected.remove(item);
        }
        holder.radioButton.setChecked(item.isSelected());
        if (item.getLoadState() == AttachmentItem.STATE_NOT_LOADED) {
            holder.overlay.setVisibility(View.VISIBLE);
            holder.overlay.setBackgroundColor(Color.argb(item.isSelected() ? 96 : 48, 255, 0, 0));
        } else {
            holder.overlay.setBackgroundColor(Color.argb(48, 0, 0, 0));
            holder.overlay.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
        }
        if (onSelectedListener != null)
            onSelectedListener.onSelected(item, position, selected.size());
    }

    public boolean containNotLoaded() {
        for (AttachmentItem item : selected) {
            if (item.getLoadState() != AttachmentItem.STATE_LOADED)
                return true;
        }
        return false;
    }
}
