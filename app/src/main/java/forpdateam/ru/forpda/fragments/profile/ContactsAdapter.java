package forpdateam.ru.forpda.fragments.profile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.rxapi.apiclasses.ProfileRx;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

class ContactsAdapter extends BaseAdapter<ProfileModel.Contact, ContactsAdapter.InfoHolder> {
    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_contact));
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class InfoHolder extends BaseViewHolder<ProfileModel.Contact> {
        private ImageView icon;

        InfoHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemView.setOnClickListener(v -> IntentHandler.handle(getItem(getLayoutPosition()).getUrl()));
        }

        @Override
        public void bind(ProfileModel.Contact item) {
            icon.setImageDrawable(App.getVecDrawable(icon.getContext(), ProfileRx.getContactIcon(item.getType())));
            icon.setContentDescription(item.getTitle());
        }
    }
}