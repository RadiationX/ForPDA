package forpdateam.ru.forpda.entity.remote.profile

import android.text.Spanned

/**
 * Created by radiationx on 03.08.16.
 */
class ProfileModel {

    var id = 0
    var sign: Spanned? = null
    var about: Spanned? = null
    var avatar: String? = null
    var nick: String? = null
    var status: String? = null
    var group: String? = null
    var note: String? = null
    val contacts = mutableListOf<Contact>()
    val info = mutableListOf<Info>()
    val stats = mutableListOf<Stat>()
    val devices = mutableListOf<Device>()
    val warnings = mutableListOf<Warning>()

    enum class ContactType {
        QMS, WEBSITE, ICQ, TWITTER, VKONTAKTE, GOOGLE_PLUS, FACEBOOK, INSTAGRAM, TELEGRAM, MAIL_RU, JABBER, WINDOWS_LIVE
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

    enum class StatType {
        SITE_KARMA, SITE_POSTS, SITE_COMMENTS, FORUM_REPUTATION, FORUM_TOPICS, FORUM_POSTS
    }

    enum class WarningType {
        POSITIVE, NEGATIVE
    }

    fun addInfo(type: InfoType, value: String) {
        this.info.add(Info(type, value))
    }

    fun addStat(stat: Stat) {
        this.stats.add(stat)
    }

    fun addContact(arg: Contact) {
        contacts.add(arg)
    }

    fun addDevice(arg: Device) {
        devices.add(arg)
    }

    fun addWarning(arg: Warning) {
        warnings.add(arg)
    }

    data class Info(
        val type: InfoType,
        val value: String
    )

    data class Contact(
        val type: ContactType,
        val url: String?,
        val title: String?,
    )

    data class Device(
        val url: String?,
        val name: String?,
        val accessory: String?,
    )

    data class Stat(
        val type: StatType?,
        val url: String?,
        val value: String?,
    )

    data class Warning(
        val type: WarningType?,
        val date: String?,
        val title: String?,
        val content: Spanned?
    )
}
