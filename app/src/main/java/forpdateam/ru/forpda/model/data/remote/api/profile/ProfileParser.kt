package forpdateam.ru.forpda.model.data.remote.api.profile

import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Pattern

class ProfileParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Profile

    fun parse(response: String, argUrl: String): ProfileModel = ProfileModel().also { profile ->
        Pattern.compile("showuser=(\\d+)").matcher(argUrl).findOnce { matcher ->
            profile.id = matcher.group(1).toInt()
        }
        patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .findOnce { mainMatcher ->
                profile.avatar = mainMatcher.group(1)?.trim()
                profile.nick = mainMatcher.group(2)?.trim().fromHtml()
                profile.status = mainMatcher.group(3)?.trim()
                profile.group = mainMatcher.group(4)?.trim()

                patternProvider
                    .getPattern(scope.scope, scope.info)
                    .matcher(mainMatcher.group(5))
                    .findAll { matcher ->
                        val field = matcher.group(1).trimEnd(':')
                        val value = matcher.group(2).trim().fromHtml()!!
                        val type = when {
                            field.contains("Рег") -> ProfileModel.InfoType.RegDate
                            field.contains("Последнее") -> ProfileModel.InfoType.OnlineDate
                            field.contains("Предупреждения") -> ProfileModel.InfoType.Alerts
                            else -> ProfileModel.InfoType.Raw(field)
                        }
                        profile.addInfo(type, value)
                    }

                profile.sign = mainMatcher.group(6)?.trim()?.let {
                    if (it == "Нет подписи") null else it.fromHtmlToColored()
                }

                patternProvider
                    .getPattern(scope.scope, scope.personal)
                    .matcher(mainMatcher.group(7))
                    .findAll { matcher ->
                        val field = matcher.group(1)!!.trim().trimEnd(':')
                        val value = matcher.group(2)?.trim()

                        if (value.isNullOrEmpty()) {
                            profile.addInfo(ProfileModel.InfoType.Gender, field.trim())
                        } else {
                            val type = when {
                                field.contains("Дата") -> ProfileModel.InfoType.Birthday
                                field.contains("Время") -> ProfileModel.InfoType.UserTime
                                field.contains("Город") -> ProfileModel.InfoType.City
                                else -> ProfileModel.InfoType.Raw(field)
                            }
                            profile.addInfo(type, value)
                        }
                    }

                patternProvider
                    .getPattern(scope.scope, scope.contacts)
                    .matcher(mainMatcher.group(8))
                    .findAll { matcher ->
                        val title = matcher.group(2).trim()
                        profile.addContact(
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
                        )
                    }

                patternProvider
                    .getPattern(scope.scope, scope.devices)
                    .matcher(mainMatcher.group(9))
                    .findAll { matcher ->
                        profile.addDevice(
                            ProfileModel.Device(
                                url = matcher.group(1).trim(),
                                name = matcher.group(2).trim(),
                                accessory = matcher.group(3).trim()
                            )
                        )
                    }


                patternProvider
                    .getPattern(scope.scope, scope.site_stats)
                    .matcher(mainMatcher.group(10))
                    .findAll { matcher ->
                        val field = matcher.group(1)
                        profile.addStat(
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
                        )
                    }

                patternProvider
                    .getPattern(scope.scope, scope.forum_stats)
                    .matcher(mainMatcher.group(11))
                    .findAll { matcher ->
                        val field = matcher.group(1)

                        profile.addStat(
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
                        )
                    }

                patternProvider
                    .getPattern(scope.scope, scope.note)
                    .matcher(response)
                    .findOnce { matcher ->
                        profile.note =
                            matcher.group(1).replace("\n".toRegex(), "<br></br>").fromHtml()
                    }

                patternProvider
                    .getPattern(scope.scope, scope.about)
                    .matcher(response)
                    .findOnce { matcher ->
                        profile.about = matcher.group(1)?.trim().fromHtmlToSpanned()
                    }

                patternProvider
                    .getPattern(scope.scope, scope.warnings)
                    .matcher(response)
                    .findAll { matcher ->
                        profile.addWarning(
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
                        )
                    }
            }
        return profile
    }
}
