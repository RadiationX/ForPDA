package forpdateam.ru.forpda.ui.fragments.devdb.device.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Device;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 09.08.17.
 */

public class CommentsAdapter extends BaseAdapter<Device.Comment, CommentsAdapter.CommentHolder> {

    private CommentHolder.Listener listener;

    public CommentsAdapter(CommentHolder.Listener listener) {
        this.listener = listener;
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_comment_item, parent, false);
        return new CommentHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public static class CommentHolder extends BaseViewHolder<Device.Comment> {
        private TextView title;
        private TextView date;
        private TextView desc;
        private TextView rating;
        private Button like;
        private Button dislike;
        private Device.Comment currentItem;

        public CommentHolder(View v, Listener listener) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            rating = (TextView) v.findViewById(R.id.item_rating);
            like = (Button) v.findViewById(R.id.item_like_btn);
            dislike = (Button) v.findViewById(R.id.item_dislike_btn);

            like.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getVecDrawable(v.getContext(), R.drawable.ic_thumb_up), null, null, null);
            dislike.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getVecDrawable(v.getContext(), R.drawable.ic_thumb_down), null, null, null);
            title.setOnClickListener((view) -> listener.onClick(currentItem));
            rating.setBackground(App.getDrawableAttr(rating.getContext(), R.attr.count_background));
        }

        @Override
        public void bind(Device.Comment item, int position) {
            currentItem = item;
            title.setText(item.getNick());
            date.setText(item.getDate());
            desc.setText(ApiUtils.spannedFromHtml(item.getText()));
            rating.setText(Integer.toString(item.getRating()));
            like.setText(Integer.toString(item.getLikes()));
            dislike.setText(Integer.toString(item.getDislikes()));
            rating.getBackground().setColorFilter(DevDbHelper.INSTANCE.getColorFilter(item.getRating()));
        }

        interface Listener {
            void onClick(Device.Comment item);
        }
    }
}
