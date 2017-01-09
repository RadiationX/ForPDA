package forpdateam.ru.forpda.messagepanel.attachments;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;

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


    public AttachmentAdapter() {
    }

    public void add(AttachmentItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
        if (onDataChangeListener != null)
            onDataChangeListener.onChange(items.size());
    }

    public void removeSelected() {
        for (AttachmentItem item : selected)
            items.remove(item);
        selected.clear();
        notifyDataSetChanged();
        if (onDataChangeListener != null)
            onDataChangeListener.onChange(items.size());
        if (onSelectedListener != null)
            onSelectedListener.onSelected(null, -1, 0);
    }

    @Override
    public AttachmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new AttachmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AttachmentAdapter.ViewHolder holder, final int position) {
        AttachmentItem item = items.get(position);
        updateChecked(holder, position);
        ImageLoader.getInstance().displayImage("assets://test_image.png", holder.imageView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(final AttachmentAdapter.OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
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

    public interface OnSelectedListener {
        void onSelected(AttachmentItem item, int index, int selected);
    }

    public interface OnDataChangeListener {
        void onChange(int count);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public RadioButton radioButton;
        public View overlay;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            imageView = (ImageView) view.findViewById(R.id.icon);
            radioButton = (RadioButton) view.findViewById(R.id.radio_button);
            overlay = view.findViewById(R.id.overlay);
        }


        @Override
        public void onClick(View v) {
            items.get(getLayoutPosition()).toggle();
            updateChecked(this, getLayoutPosition());
            if (itemClickListener != null) {
                itemClickListener.onItemClick(items.get(getLayoutPosition()));
            }
        }
    }

    private void updateChecked(ViewHolder holder, int position) {
        AttachmentItem item = items.get(position);
        if (item.getSelected()) {
            selected.add(item);
        } else {
            selected.remove(item);
        }
        holder.radioButton.setChecked(item.getSelected());
        holder.overlay.setVisibility(item.getSelected() ? View.VISIBLE : View.GONE);
        if (onSelectedListener != null)
            onSelectedListener.onSelected(item, position, selected.size());
    }
}
