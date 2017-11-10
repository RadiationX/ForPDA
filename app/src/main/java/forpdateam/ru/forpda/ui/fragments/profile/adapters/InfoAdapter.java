package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.apirx.apiclasses.ProfileRx;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 14.09.17.
 */

class InfoAdapter extends BaseAdapter<ProfileModel.Info, InfoAdapter.InfoHolder> {
    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_info));
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class InfoHolder extends BaseViewHolder<ProfileModel.Info> {
        private TextView title;
        private TextView value;

        InfoHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            value = (TextView) itemView.findViewById(R.id.item_value);
        }

        @Override
        public void bind(ProfileModel.Info item) {
            title.setText(ProfileRx.getTypeString(item.getType()));
            value.setText(item.getValue());
        }
    }
}