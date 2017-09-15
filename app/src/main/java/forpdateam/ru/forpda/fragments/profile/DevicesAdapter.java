package forpdateam.ru.forpda.fragments.profile;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.devdb.DeviceFragment;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

class DevicesAdapter extends BaseAdapter<ProfileModel.Device, DevicesAdapter.InfoHolder> {
    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_device));
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class InfoHolder extends BaseViewHolder<ProfileModel.Device> {
        private TextView title;

        InfoHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString(DeviceFragment.ARG_DEVICE_ID, getItem(getLayoutPosition()).getId());
                TabManager.getInstance().add(DeviceFragment.class, args);
            });
        }

        @Override
        public void bind(ProfileModel.Device item) {
            title.setText(String.format("%s %s", item.getName(), item.getAccessory()));
        }
    }
}