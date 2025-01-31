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
            ProfileModel.ContactType.WebSite -> R.string.profile_contact_site
            ProfileModel.ContactType.ICQ -> R.string.profile_contact_icq
            ProfileModel.ContactType.Twitter -> R.string.profile_contact_twitter
            ProfileModel.ContactType.Jabber -> R.string.profile_contact_jabber
            ProfileModel.ContactType.VKontakte -> R.string.profile_contact_vk
            ProfileModel.ContactType.GooglePlus -> R.string.profile_contact_google_plus
            ProfileModel.ContactType.Facebook -> R.string.profile_contact_facebook
            ProfileModel.ContactType.Instagram -> R.string.profile_contact_instagram
            ProfileModel.ContactType.MailRu -> R.string.profile_contact_mail_ru
            ProfileModel.ContactType.Telegram -> R.string.profile_contact_telegram
            ProfileModel.ContactType.WindowsLive -> R.string.profile_contact_windows_live
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

    fun getTypeString(context: Context, type: ProfileModel.StatType): String {
        return when (type) {
            ProfileModel.StatType.SiteKarma -> context.getString(R.string.profile_stat_site_karma)
            ProfileModel.StatType.SitePosts -> context.getString(R.string.profile_stat_site_posts)
            ProfileModel.StatType.SiteComments -> context.getString(R.string.profile_stat_site_comments)
            ProfileModel.StatType.ForumReputation -> context.getString(R.string.profile_stat_forum_reputation)
            ProfileModel.StatType.ForumTopics -> context.getString(R.string.profile_stat_forum_topics)
            ProfileModel.StatType.ForumPosts -> context.getString(R.string.profile_stat_forum_posts)
            is ProfileModel.StatType.Raw -> type.value
        }
    }

    @DrawableRes
    fun getContactIcon(type: ProfileModel.ContactType): Int {
        return when (type) {
            ProfileModel.ContactType.QMS -> R.drawable.contact_qms
            ProfileModel.ContactType.WebSite -> R.drawable.contact_site
            ProfileModel.ContactType.ICQ -> R.drawable.contact_icq
            ProfileModel.ContactType.Twitter -> R.drawable.contact_twitter
            ProfileModel.ContactType.Jabber -> R.drawable.contact_jabber
            ProfileModel.ContactType.VKontakte -> R.drawable.contact_vk
            ProfileModel.ContactType.GooglePlus -> R.drawable.contact_google_plus
            ProfileModel.ContactType.Facebook -> R.drawable.contact_facebook
            ProfileModel.ContactType.Instagram -> R.drawable.contact_instagram
            ProfileModel.ContactType.MailRu -> R.drawable.contact_mail_ru
            ProfileModel.ContactType.Telegram -> R.drawable.contact_telegram
            ProfileModel.ContactType.WindowsLive -> R.drawable.contact_site
        }
    }
}