package forpdateam.ru.forpda.ui.fragments.profile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.entity.remote.devdb.Device;
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel;
import forpdateam.ru.forpda.model.repository.temp.TempHelper;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.ui.fragments.devdb.device.comments.CommentsAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

class ContactsAdapter extends BaseAdapter<ProfileModel.Contact, ContactsAdapter.InfoHolder> {

    private InfoHolder.Listener listener;

    public ContactsAdapter(InfoHolder.Listener listener) {
        this.listener = listener;
    }

    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_contact), listener);
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class InfoHolder extends BaseViewHolder<ProfileModel.Contact> {
        private ImageView icon;
        private ProfileModel.Contact currentItem;

        InfoHolder(View itemView, Listener listener) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemView.setOnClickListener(v -> listener.onClick(currentItem));
        }

        @Override
        public void bind(ProfileModel.Contact item) {
            currentItem = item;
            icon.setImageDrawable(App.getVecDrawable(icon.getContext(), TempHelper.INSTANCE.getContactIcon(item.getType())));
            icon.setContentDescription(item.getTitle());
        }

        interface Listener {
            void onClick(ProfileModel.Contact item);
        }
    }
}