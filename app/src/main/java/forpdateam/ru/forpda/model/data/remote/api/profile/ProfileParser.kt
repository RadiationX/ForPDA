package forpdateam.ru.forpda.model.data.remote.api.profile

import android.text.Spanned
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import java.util.regex.Pattern
import javax.inject.Inject

class ProfileParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Profile

    fun parse(response: String, argUrl: String): ProfileModel {
        val profile = patternProvider
            .getRegexParser(scope.scope, scope.main)
            .mapOnce(response) { mainMatcher ->
                val id = Pattern.compile("showuser=(\\d+)").matcher(argUrl).requireOnce { matcher ->
                    matcher.group(1)!!.toInt()
                }
                val sign = mainMatcher.require(6).trim().let {
                    if (it == "Нет подписи") null else it.fromHtmlToColored()
                }
                val info = mutableListOf<ProfileModel.Info>()
                val stats = mutableListOf<ProfileModel.Stat>()
                info.addAll(parseMainInfo(mainMatcher.require(5)))
                info.addAll(parsePersonalInfo(mainMatcher.require(7)))
                stats.addAll(parseSiteStats(mainMatcher.require(10)))
                stats.addAll(parseForumStats(mainMatcher.require(11)))
                ProfileModel(
                    user = ForumUser.required(
                        id = id,
                        avatar = mainMatcher.require(1).trim(),
                        nick = mainMatcher.require(2).trim().fromHtml()
                    ),
                    status = mainMatcher.get(3)?.trim(),
                    group = mainMatcher.require(4).trim(),
                    info = info,
                    sign = sign,
                    contacts = parseContacts(mainMatcher.require(8)),
                    devices = parseDevices(mainMatcher.require(9)),
                    stats = stats,
                    note = parseNote(response),
                    about = parseAbout(response),
                    warnings = parseWarnings(response),
                )
            }
        return requireNotNull(profile) {
            "Can't parse profile"
        }
    }

    private fun parseMainInfo(source: String): List<ProfileModel.Info> = patternProvider
        .getRegexParser(scope.scope, scope.info)
        .map(source) { matcher ->
            val field = matcher.require(1).trimEnd(':')
            val value = matcher.require(2).trim().fromHtml()
            val type = when {
                field.contains("Рег") -> ProfileModel.InfoType.RegDate
                field.contains("Последнее") -> ProfileModel.InfoType.OnlineDate
                field.contains("Предупреждения") -> ProfileModel.InfoType.Alerts
                else -> ProfileModel.InfoType.Raw(field)
            }
            ProfileModel.Info(type, value)
        }

    private fun parsePersonalInfo(source: String): List<ProfileModel.Info> = patternProvider
        .getRegexParser(scope.scope, scope.personal)
        .map(source) { matcher ->
            val field = matcher.require(1).trim().trimEnd(':')
            val value = matcher.get(2)?.trim()

            if (value.isNullOrEmpty()) {
                ProfileModel.Info(ProfileModel.InfoType.Gender, field)
            } else {
                val type = when {
                    field.contains("Дата") -> ProfileModel.InfoType.Birthday
                    field.contains("Время") -> ProfileModel.InfoType.UserTime
                    field.contains("Город") -> ProfileModel.InfoType.City
                    else -> ProfileModel.InfoType.Raw(field)
                }
                ProfileModel.Info(type, value)
            }
        }

    private fun parseContacts(source: String): List<ProfileModel.Contact> = patternProvider
        .getRegexParser(scope.scope, scope.contacts)
        .map(source) { matcher ->
            val title = matcher.require(2).trim()
            ProfileModel.Contact(
                url = matcher.require(1).trim(),
                title = title,
                type = when (title) {
                    "QMS" -> ProfileModel.ContactType.QMS
                    "Вебсайт" -> ProfileModel.ContactType.WebSite
                    "ICQ" -> ProfileModel.ContactType.ICQ
                    "Twitter" -> ProfileModel.ContactType.Twitter
                    "Вконтакте" -> ProfileModel.ContactType.VKontakte
                    "Google+" -> ProfileModel.ContactType.GooglePlus
                    "Facebook" -> ProfileModel.ContactType.Facebook
                    "Instagram" -> ProfileModel.ContactType.Instagram
                    "Jabber" -> ProfileModel.ContactType.Jabber
                    "Telegram" -> ProfileModel.ContactType.Telegram
                    "Mail.ru" -> ProfileModel.ContactType.MailRu
                    "Windows Live" -> ProfileModel.ContactType.WindowsLive
                    else -> ProfileModel.ContactType.WebSite
                }
            )
        }

    private fun parseDevices(source: String): List<ProfileModel.Device> = patternProvider
        .getRegexParser(scope.scope, scope.devices)
        .map(source) { matcher ->
            ProfileModel.Device(
                url = matcher.require(1).trim(),
                name = matcher.require(2).trim(),
                accessory = matcher.require(3).trim()
            )
        }

    private fun parseSiteStats(source: String): List<ProfileModel.Stat> = patternProvider
        .getRegexParser(scope.scope, scope.site_stats)
        .map(source) { matcher ->
            val field = matcher.require(1)
            ProfileModel.Stat(
                url = matcher.get(2),
                value = matcher.require(3),
                type = when {
                    field.contains("Карма") -> ProfileModel.StatType.SiteKarma
                    field.contains("Постов") -> ProfileModel.StatType.SitePosts
                    field.contains("Комментов") -> ProfileModel.StatType.SiteComments
                    else -> ProfileModel.StatType.Raw(field)
                }
            )
        }

    private fun parseForumStats(source: String): List<ProfileModel.Stat> = patternProvider
        .getRegexParser(scope.scope, scope.forum_stats)
        .map(source) { matcher ->
            val field = matcher.require(1)

            ProfileModel.Stat(
                url = matcher.get(3),
                value = matcher.get(4) ?: matcher.require(2),
                type = when {
                    field.contains("Репу") -> ProfileModel.StatType.ForumReputation
                    field.contains("Тем") -> ProfileModel.StatType.ForumTopics
                    field.contains("Постов") -> ProfileModel.StatType.ForumPosts
                    else -> ProfileModel.StatType.Raw(field)
                }
            )
        }

    private fun parseNote(source: String): String? = patternProvider
        .getRegexParser(scope.scope, scope.note)
        .mapOnce(source) { matcher ->
            matcher.require(1).replace("\n".toRegex(), "<br></br>").fromHtml()
        }

    private fun parseAbout(source: String): Spanned? = patternProvider
        .getRegexParser(scope.scope, scope.about)
        .mapOnce(source) { matcher ->
            matcher.get(1)?.trim()?.fromHtmlToSpanned()
        }

    private fun parseWarnings(source: String): List<ProfileModel.Warning> = patternProvider
        .getRegexParser(scope.scope, scope.warnings)
        .map(source) { matcher ->
            ProfileModel.Warning(
                type = when (matcher.require(1)) {
                    "pos" -> ProfileModel.WarningType.Positive
                    "neg" -> ProfileModel.WarningType.Negative
                    else -> ProfileModel.WarningType.Unknown
                },
                date = matcher.require(2),
                title = matcher.require(3).fromHtml(),
                content = matcher.require(4).fromHtmlToSpanned()
            )
        }
}
