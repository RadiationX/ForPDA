package forpdateam.ru.forpda.fragments.devdb.device.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.devdb.models.Device;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 09.08.17.
 */

public class CommentsAdapter extends BaseAdapter<Device.Comment, CommentsAdapter.CommentHolder> {
    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_comment_item, parent, false);
        return new CommentHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class CommentHolder extends BaseViewHolder<Device.Comment> {
        public TextView title;
        public TextView date;
        public TextView desc;
        public TextView rating;
        public Button like;
        public Button dislike;

        public CommentHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.item_title);
            date = (TextView) v.findViewById(R.id.item_date);
            desc = (TextView) v.findViewById(R.id.item_desc);
            rating = (TextView) v.findViewById(R.id.item_rating);
            like = (Button) v.findViewById(R.id.item_like_btn);
            dislike = (Button) v.findViewById(R.id.item_dislike_btn);

            like.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getVecDrawable(v.getContext(), R.drawable.ic_thumb_up), null, null, null);
            dislike.setCompoundDrawablesRelativeWithIntrinsicBounds(App.getVecDrawable(v.getContext(), R.drawable.ic_thumb_down), null, null, null);
            title.setOnClickListener(this::onTitleClick);
            rating.setBackground(App.getDrawableAttr(rating.getContext(), R.attr.count_background));
        }

        @Override
        public void bind(Device.Comment item, int position) {
            title.setText(item.getNick());
            date.setText(item.getDate());
            desc.setText(Utils.spannedFromHtml(item.getText()));
            rating.setText(Integer.toString(item.getRating()));
            like.setText(Integer.toString(item.getLikes()));
            dislike.setText(Integer.toString(item.getDislikes()));
            rating.getBackground().setColorFilter(RxApi.DevDb().getColorFilter(item.getRating()));
        }

        private void onTitleClick(View v) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + getItem(getLayoutPosition()).getUserId());
        }
    }
}
