package forpdateam.ru.forpda.entity.remote.profile

import android.text.Spanned

import java.util.ArrayList

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

    enum class InfoType {
        REG_DATE, ALERTS, ONLINE_DATE, GENDER, BIRTHDAY, USER_TIME, CITY
    }

    enum class StatType {
        SITE_KARMA, SITE_POSTS, SITE_COMMENTS, FORUM_REPUTATION, FORUM_TOPICS, FORUM_POSTS
    }

    enum class WarningType {
        POSITIVE, NEGATIVE
    }

    fun addInfo(type: InfoType, value: String) {
        val info = Info()
        info.type = type
        info.value = value
        this.info.add(info)
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

    class Info {
        var type: InfoType? = null
        var value: String? = null
    }

    class Contact {
        var type = ContactType.WEBSITE
        var url: String? = null
        var title: String? = null
    }

    class Device {
        var url: String? = null
        var name: String? = null
        var accessory: String? = null
    }

    class Stat {
        var type: StatType? = null
        var url: String? = null
        var value: String? = null
    }

    class Warning {
        var type: WarningType? = null
        var date: String? = null
        var title: String? = null
        var content: Spanned? = null
    }
}
