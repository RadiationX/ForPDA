package forpdateam.ru.forpda.fragments.profile;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

class WarningsAdapter extends BaseAdapter<ProfileModel.Warning, WarningsAdapter.WarningHolder> {
    @Override
    public WarningHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WarningHolder(inflateLayout(parent, R.layout.profile_sub_item_warning));
    }

    @Override
    public void onBindViewHolder(WarningHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class WarningHolder extends BaseViewHolder<ProfileModel.Warning> {
        private TextView title;
        private TextView date;
        private TextView content;

        WarningHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            date = (TextView) itemView.findViewById(R.id.item_date);
            content = (TextView) itemView.findViewById(R.id.item_content);
        }

        @Override
        public void bind(ProfileModel.Warning item) {
            title.setText(item.getTitle());
            date.setText(item.getDate());
            content.setText(item.getContent());
            switch (item.getType()) {
                case POSITIVE:
                    title.setTextColor(ContextCompat.getColor(title.getContext(), R.color.md_green_400));
                    break;
                case NEGATIVE:
                    title.setTextColor(ContextCompat.getColor(title.getContext(), R.color.md_red_400));
                    break;
            }
        }
    }
}