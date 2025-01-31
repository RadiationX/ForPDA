package forpdateam.ru.forpda.model.repository.temp

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by radiationx on 30.03.18.
 */
object TempHelper {

    fun getDisableStr(b: Boolean): String {
        return if (b) "disabled" else ""
    }


    /* QMS */


    fun transformMessageSrc(messagesSrcIn: String): String {
        var messagesSrc = messagesSrcIn
        messagesSrc = messagesSrc.replace("\n".toRegex(), "").replace("'".toRegex(), "&apos;")
        messagesSrc = JSONObject.quote(messagesSrc)
        messagesSrc = messagesSrc.substring(1, messagesSrc.length - 1)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("src", messagesSrc)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return messagesSrc
    }

    @StringRes
    fun getTypeString(type: ProfileModel.ContactType): Int {
        return when (type) {
            ProfileModel.ContactType.QMS -> R.string.profile_contact_qms
            ProfileModel.ContactType.WEBSITE -> R.string.profile_contact_site
            ProfileModel.ContactType.ICQ -> R.string.profile_contact_icq
            ProfileModel.ContactType.TWITTER -> R.string.profile_contact_twitter
            ProfileModel.ContactType.JABBER -> R.string.profile_contact_jabber
            ProfileModel.ContactType.VKONTAKTE -> R.string.profile_contact_vk
            ProfileModel.ContactType.GOOGLE_PLUS -> R.string.profile_contact_google_plus
            ProfileModel.ContactType.FACEBOOK -> R.string.profile_contact_facebook
            ProfileModel.ContactType.INSTAGRAM -> R.string.profile_contact_instagram
            ProfileModel.ContactType.MAIL_RU -> R.string.profile_contact_mail_ru
            ProfileModel.ContactType.TELEGRAM -> R.string.profile_contact_telegram
            ProfileModel.ContactType.WINDOWS_LIVE -> R.string.profile_contact_windows_live
        }
    }

    fun getTypeString(context: Context, type: ProfileModel.InfoType): String {
        return when (type) {
            ProfileModel.InfoType.RegDate -> context.getString(R.string.profile_info_reg)
            ProfileModel.InfoType.Alerts -> context.getString(R.string.profile_info_alerts)
            ProfileModel.InfoType.OnlineDate -> context.getString(R.string.profile_info_last_online)
            ProfileModel.InfoType.Gender -> context.getString(R.string.profile_info_gender)
            ProfileModel.InfoType.Birthday -> context.getString(R.string.profile_info_birthday)
            ProfileModel.InfoType.UserTime -> context.getString(R.string.profile_info_user_time)
            ProfileModel.InfoType.City -> context.getString(R.string.profile_info_city)
            is ProfileModel.InfoType.Raw -> type.value
        }
    }

    @StringRes
    fun getTypeString(type: ProfileModel.StatType): Int {
        return when (type) {
            ProfileModel.StatType.SITE_KARMA -> R.string.profile_stat_site_karma
            ProfileModel.StatType.SITE_POSTS -> R.string.profile_stat_site_posts
            ProfileModel.StatType.SITE_COMMENTS -> R.string.profile_stat_site_comments
            ProfileModel.StatType.FORUM_REPUTATION -> R.string.profile_stat_forum_reputation
            ProfileModel.StatType.FORUM_TOPICS -> R.string.profile_stat_forum_topics
            ProfileModel.StatType.FORUM_POSTS -> R.string.profile_stat_forum_posts
        }
    }

    @DrawableRes
    fun getContactIcon(type: ProfileModel.ContactType): Int {
        return when (type) {
            ProfileModel.ContactType.QMS -> R.drawable.contact_qms
            ProfileModel.ContactType.WEBSITE -> R.drawable.contact_site
            ProfileModel.ContactType.ICQ -> R.drawable.contact_icq
            ProfileModel.ContactType.TWITTER -> R.drawable.contact_twitter
            ProfileModel.ContactType.JABBER -> R.drawable.contact_jabber
            ProfileModel.ContactType.VKONTAKTE -> R.drawable.contact_vk
            ProfileModel.ContactType.GOOGLE_PLUS -> R.drawable.contact_google_plus
            ProfileModel.ContactType.FACEBOOK -> R.drawable.contact_facebook
            ProfileModel.ContactType.INSTAGRAM -> R.drawable.contact_instagram
            ProfileModel.ContactType.MAIL_RU -> R.drawable.contact_mail_ru
            ProfileModel.ContactType.TELEGRAM -> R.drawable.contact_telegram
            ProfileModel.ContactType.WINDOWS_LIVE -> R.drawable.contact_site
        }
    }
}