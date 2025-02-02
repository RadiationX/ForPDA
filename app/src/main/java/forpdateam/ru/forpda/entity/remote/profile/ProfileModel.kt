package forpdateam.ru.forpda.entity.remote.profile

import android.text.Spanned
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

/**
 * Created by radiationx on 03.08.16.
 */
data class ProfileModel(
    val user: ForumUser,
    val sign: Spanned?,
    val about: Spanned?,
    val status: String?,
    val group: String,
    val note: String?,
    val contacts: List<Contact>,
    val info: List<Info>,
    val stats: List<Stat>,
    val devices: List<Device>,
    val warnings: List<Warning>,
) {


    enum class ContactType {
        QMS,
        WebSite,
        ICQ,
        Twitter,
        VKontakte,
        GooglePlus,
        Facebook,
        Instagram,
        Telegram,
        MailRu,
        Jabber,
        WindowsLive
    }

    sealed interface InfoType {
        object RegDate : InfoType
        object Alerts : InfoType
        object OnlineDate : InfoType
        object Gender : InfoType
        object Birthday : InfoType
        object UserTime : InfoType
        object City : InfoType
        data class Raw(val value: String) : InfoType
    }

    sealed interface StatType {
        object SiteKarma : StatType
        object SitePosts : StatType
        object SiteComments : StatType
        object ForumReputation : StatType
        object ForumTopics : StatType
        object ForumPosts : StatType
        data class Raw(val value: String) : StatType
    }

    enum class WarningType {
        Positive, Negative, Unknown
    }

    data class Info(
        val type: InfoType,
        val value: String
    )

    data class Contact(
        val type: ContactType,
        val url: String,
        val title: String,
    )

    data class Device(
        val url: String,
        val name: String,
        val accessory: String,
    )

    data class Stat(
        val type: StatType,
        val url: String?,
        val value: String,
    )

    data class Warning(
        val type: WarningType,
        val date: String,
        val title: String,
        val content: Spanned
    )
}
