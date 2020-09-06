package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

class DevicesAdapter extends BaseAdapter<ProfileModel.Device, DevicesAdapter.InfoHolder> {

    private InfoHolder.Listener listener;

    public DevicesAdapter(InfoHolder.Listener listener) {
        this.listener = listener;
    }

    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_device), listener);
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class InfoHolder extends BaseViewHolder<ProfileModel.Device> {
        private TextView title;
        private ProfileModel.Device currentItem;

        InfoHolder(View itemView, Listener listener) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            itemView.setOnClickListener(v -> listener.onClick(currentItem));
        }

        @Override
        public void bind(ProfileModel.Device item) {
            currentItem = item;
            title.setText(String.format("%s %s", item.getName(), item.getAccessory()));
        }

        interface Listener {
            void onClick(ProfileModel.Device item);
        }
    }
}