package forpdateam.ru.forpda.fragments.profile;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.views.adapters.BaseAdapter;
import forpdateam.ru.forpda.views.adapters.BaseViewHolder;

/**
 * Created by radiationx on 15.09.17.
 */

public class ContactsAdapter extends BaseAdapter<ProfileModel.Contact, ContactsAdapter.InfoHolder> {
    @Override
    public InfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_contact));
    }

    @Override
    public void onBindViewHolder(InfoHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @DrawableRes
    private int getIconRes(ProfileModel.ContactType type) {
        switch (type) {
            case WEBSITE:
                return R.drawable.contact_site;
            case ICQ:
                return R.drawable.contact_icq;
            case TWITTER:
                return R.drawable.contact_twitter;
            case JABBER:
                return R.drawable.contact_jabber;
            case VKONTAKTE:
                return R.drawable.contact_vk;
            case GOOGLE_PLUS:
                return R.drawable.contact_google_plus;
            case FACEBOOK:
                return R.drawable.contact_facebook;
            case INSTAGRAM:
                return R.drawable.contact_instagram;
            case MAIL_RU:
                return R.drawable.contact_mail_ru;
            case TELEGRAM:
                return R.drawable.contact_telegram;
            /*case WINDOWS_LIVE:
                return R.drawable.contact_site;*/
            default:
                return R.drawable.contact_site;
        }
    }

    class InfoHolder extends BaseViewHolder<ProfileModel.Contact> {
        private ImageView icon;

        InfoHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
        }

        @Override
        public void bind(ProfileModel.Contact item) {
            icon.setImageDrawable(App.getVecDrawable(icon.getContext(), getIconRes(item.getType())));
        }
    }
}