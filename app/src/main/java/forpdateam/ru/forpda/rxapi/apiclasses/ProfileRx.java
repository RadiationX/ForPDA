package forpdateam.ru.forpda.rxapi.apiclasses;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ProfileRx {
    public Observable<ProfileModel> getProfile(String url) {
        return Observable.fromCallable(() -> Api.Profile().getProfile(url));
    }

    public Observable<Boolean> saveNote(final String note) {
        return Observable.fromCallable(() -> Api.Profile().saveNote(note));
    }

    @StringRes
    public static int getTypeString(ProfileModel.ContactType type) {
        switch (type) {
            case QMS:
                return R.string.profile_contact_qms;
            case WEBSITE:
                return R.string.profile_contact_site;
            case ICQ:
                return R.string.profile_contact_icq;
            case TWITTER:
                return R.string.profile_contact_twitter;
            case JABBER:
                return R.string.profile_contact_jabber;
            case VKONTAKTE:
                return R.string.profile_contact_vk;
            case GOOGLE_PLUS:
                return R.string.profile_contact_google_plus;
            case FACEBOOK:
                return R.string.profile_contact_facebook;
            case INSTAGRAM:
                return R.string.profile_contact_instagram;
            case MAIL_RU:
                return R.string.profile_contact_mail_ru;
            case TELEGRAM:
                return R.string.profile_contact_telegram;
            case WINDOWS_LIVE:
                return R.string.profile_contact_windows_live;
            default:
                return R.string.undefined;
        }
    }

    @StringRes
    public static int getTypeString(ProfileModel.InfoType type) {
        switch (type) {
            case REG_DATE:
                return R.string.profile_info_reg;
            case ALERTS:
                return R.string.profile_info_alerts;
            case ONLINE_DATE:
                return R.string.profile_info_last_online;
            case GENDER:
                return R.string.profile_info_gender;
            case BIRTHDAY:
                return R.string.profile_info_birthday;
            case USER_TIME:
                return R.string.profile_info_user_time;
            case CITY:
                return R.string.profile_info_city;
            default:
                return R.string.undefined;
        }
    }

    @StringRes
    public static int getTypeString(ProfileModel.StatType type) {
        switch (type) {
            case SITE_KARMA:
                return R.string.profile_stat_site_karma;
            case SITE_POSTS:
                return R.string.profile_stat_site_posts;
            case SITE_COMMENTS:
                return R.string.profile_stat_site_comments;
            case FORUM_REPUTATION:
                return R.string.profile_stat_forum_reputation;
            case FORUM_TOPICS:
                return R.string.profile_stat_forum_topics;
            case FORUM_POSTS:
                return R.string.profile_stat_forum_posts;
            default:
                return R.string.undefined;
        }
    }

    @DrawableRes
    public static int getContactIcon(ProfileModel.ContactType type) {
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
}
