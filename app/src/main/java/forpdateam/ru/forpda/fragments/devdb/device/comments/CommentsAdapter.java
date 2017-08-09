package forpdateam.ru.forpda.fragments.devdb.device.comments;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 09.08.17.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private ArrayList<Device.Comment> list = new ArrayList<>();
    private SparseArray<ColorFilter> colorFilter = new SparseArray<>();

    public CommentsAdapter() {
        colorFilter.put(1, new PorterDuffColorFilter(Color.parseColor("#850113"), PorterDuff.Mode.SRC_IN));
        colorFilter.put(2, new PorterDuffColorFilter(Color.parseColor("#d50000"), PorterDuff.Mode.SRC_IN));
        colorFilter.put(3, new PorterDuffColorFilter(Color.parseColor("#ffac00"), PorterDuff.Mode.SRC_IN));
        colorFilter.put(4, new PorterDuffColorFilter(Color.parseColor("#99cc00"), PorterDuff.Mode.SRC_IN));
        colorFilter.put(5, new PorterDuffColorFilter(Color.parseColor("#339900"), PorterDuff.Mode.SRC_IN));
    }

    public void addAll(Collection<Device.Comment> results) {
        addAll(results, true);
    }

    public void addAll(Collection<Device.Comment> results, boolean clearList) {
        if (clearList)
            clear();
        list.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_comment_item, parent, false);
        return new CommentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.ViewHolder holder, int position) {
        Device.Comment item = list.get(position);
        holder.title.setText(item.getNick());
        holder.date.setText(item.getDate());
        holder.desc.setText(Utils.spannedFromHtml(item.getText()));
        holder.rating.setText(Integer.toString(item.getRating()));
        holder.like.setText(Integer.toString(item.getLikes()));
        holder.dislike.setText(Integer.toString(item.getDislikes()));
        holder.rating.getBackground().setColorFilter(colorFilter.get(item.getRatingColorCode()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Device.Comment getItem(int position) {
        return list.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public TextView desc;
        public TextView rating;
        public Button like;
        public Button dislike;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            rating = (TextView) v.findViewById(R.id.item_rating);
            like = (Button) v.findViewById(R.id.item_like_btn);
            dislike = (Button) v.findViewById(R.id.item_dislike_btn);

            like.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getAppDrawable(v.getContext(), R.drawable.ic_thumb_up), null, null, null);
            dislike.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getAppDrawable(v.getContext(), R.drawable.ic_thumb_down), null, null, null);
            title.setOnClickListener(this::onTitleClick);
        }

        private void onTitleClick(View v) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + getItem(getLayoutPosition()).getUserId());
        }
    }
}
