package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

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
        private final TextView title;
        private final TextView date;
        private final TextView content;

        WarningHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            date = itemView.findViewById(R.id.item_date);
            content = itemView.findViewById(R.id.item_content);
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