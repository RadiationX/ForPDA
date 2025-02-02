package forpdateam.ru.forpda.model.data.remote.api.profile

import android.text.Spanned
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Pattern

class ProfileParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Profile

    fun parse(response: String, argUrl: String): ProfileModel {
        val profile = patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .mapOnce { mainMatcher ->
                val id = Pattern.compile("showuser=(\\d+)").matcher(argUrl).requireOnce { matcher ->
                    matcher.group(1).toInt()
                }
                val sign = mainMatcher.group(6).trim().let {
                    if (it == "Нет подписи") null else it.fromHtmlToColored()
                }
                val info = mutableListOf<ProfileModel.Info>()
                val stats = mutableListOf<ProfileModel.Stat>()
                info.addAll(parseMainInfo(mainMatcher.group(5)))
                info.addAll(parsePersonalInfo(mainMatcher.group(7)))
                stats.addAll(parseSiteStats(mainMatcher.group(10)))
                stats.addAll(parseForumStats(mainMatcher.group(11)))
                ProfileModel(
                    user = ForumUser.required(
                        id = id,
                        avatar = mainMatcher.group(1).trim(),
                        nick = mainMatcher.group(2).trim().fromHtml()!!
                    ),
                    status = mainMatcher.group(3)?.trim(),
                    group = mainMatcher.group(4).trim(),
                    info = info,
                    sign = sign,
                    contacts = parseContacts(mainMatcher.group(8)),
                    devices = parseDevices(mainMatcher.group(9)),
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
        .getPattern(scope.scope, scope.info)
        .matcher(source)
        .map { matcher ->
            val field = matcher.group(1).trimEnd(':')
            val value = matcher.group(2).trim().fromHtml()!!
            val type = when {
                field.contains("Рег") -> ProfileModel.InfoType.RegDate
                field.contains("Последнее") -> ProfileModel.InfoType.OnlineDate
                field.contains("Предупреждения") -> ProfileModel.InfoType.Alerts
                else -> ProfileModel.InfoType.Raw(field)
            }
            ProfileModel.Info(type, value)
        }

    private fun parsePersonalInfo(source: String): List<ProfileModel.Info> = patternProvider
        .getPattern(scope.scope, scope.personal)
        .matcher(source)
        .map { matcher ->
            val field = matcher.group(1)!!.trim().trimEnd(':')
            val value = matcher.group(2)?.trim()

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
        .getPattern(scope.scope, scope.contacts)
        .matcher(source)
        .map { matcher ->
            val title = matcher.group(2).trim()
            ProfileModel.Contact(
                url = matcher.group(1).trim(),
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
        .getPattern(scope.scope, scope.devices)
        .matcher(source)
        .map { matcher ->
            ProfileModel.Device(
                url = matcher.group(1).trim(),
                name = matcher.group(2).trim(),
                accessory = matcher.group(3).trim()
            )
        }

    private fun parseSiteStats(source: String): List<ProfileModel.Stat> = patternProvider
        .getPattern(scope.scope, scope.site_stats)
        .matcher(source)
        .map { matcher ->
            val field = matcher.group(1)
            ProfileModel.Stat(
                url = matcher.group(2),
                value = matcher.group(3),
                type = when {
                    field.contains("Карма") -> ProfileModel.StatType.SiteKarma
                    field.contains("Постов") -> ProfileModel.StatType.SitePosts
                    field.contains("Комментов") -> ProfileModel.StatType.SiteComments
                    else -> ProfileModel.StatType.Raw(field)
                }
            )
        }

    private fun parseForumStats(source: String): List<ProfileModel.Stat> = patternProvider
        .getPattern(scope.scope, scope.forum_stats)
        .matcher(source)
        .map { matcher ->
            val field = matcher.group(1)

            ProfileModel.Stat(
                url = matcher.group(3),
                value = matcher.group(4) ?: matcher.group(2),
                type = when {
                    field.contains("Репу") -> ProfileModel.StatType.ForumReputation
                    field.contains("Тем") -> ProfileModel.StatType.ForumTopics
                    field.contains("Постов") -> ProfileModel.StatType.ForumPosts
                    else -> ProfileModel.StatType.Raw(field)
                }
            )
        }

    private fun parseNote(source: String): String? = patternProvider
        .getPattern(scope.scope, scope.note)
        .matcher(source)
        .mapOnce { matcher ->
            matcher.group(1).replace("\n".toRegex(), "<br></br>").fromHtml()
        }

    private fun parseAbout(source: String): Spanned? = patternProvider
        .getPattern(scope.scope, scope.about)
        .matcher(source)
        .mapOnce { matcher ->
            matcher.group(1)?.trim().fromHtmlToSpanned()
        }

    private fun parseWarnings(source: String): List<ProfileModel.Warning> = patternProvider
        .getPattern(scope.scope, scope.warnings)
        .matcher(source)
        .map { matcher ->
            ProfileModel.Warning(
                type = when (matcher.group(1)) {
                    "pos" -> ProfileModel.WarningType.Positive
                    "neg" -> ProfileModel.WarningType.Negative
                    else -> ProfileModel.WarningType.Unknown
                },
                date = matcher.group(2),
                title = matcher.group(3).fromHtml()!!,
                content = matcher.group(4).fromHtmlToSpanned()!!
            )
        }
}
