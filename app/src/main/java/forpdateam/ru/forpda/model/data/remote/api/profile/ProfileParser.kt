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
                        val field = matcher.group(1)
                        if (field.contains("Рег")) {
                            profile.addInfo(
                                ProfileModel.InfoType.RegDate,
                                matcher.group(2)?.trim().fromHtml().orEmpty()
                            )
                        } else if (field.contains("Последнее")) {
                            profile.addInfo(
                                ProfileModel.InfoType.OnlineDate,
                                matcher.group(2)?.trim().fromHtml().orEmpty()
                            )
                        }
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
                        val title = matcher.group(2)?.trim()
                        profile.addContact(
                            ProfileModel.Contact(
                                url = matcher.group(1)?.trim(),
                                title = title,
                                type = when (title) {
                                    "QMS" -> ProfileModel.ContactType.QMS
                                    "Вебсайт" -> ProfileModel.ContactType.WEBSITE
                                    "ICQ" -> ProfileModel.ContactType.ICQ
                                    "Twitter" -> ProfileModel.ContactType.TWITTER
                                    "Вконтакте" -> ProfileModel.ContactType.VKONTAKTE
                                    "Google+" -> ProfileModel.ContactType.GOOGLE_PLUS
                                    "Facebook" -> ProfileModel.ContactType.FACEBOOK
                                    "Instagram" -> ProfileModel.ContactType.INSTAGRAM
                                    "Jabber" -> ProfileModel.ContactType.JABBER
                                    "Telegram" -> ProfileModel.ContactType.TELEGRAM
                                    "Mail.ru" -> ProfileModel.ContactType.MAIL_RU
                                    "Windows Live" -> ProfileModel.ContactType.WINDOWS_LIVE
                                    else -> ProfileModel.ContactType.WEBSITE
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
                                url = matcher.group(1)?.trim(),
                                name = matcher.group(2)?.trim(),
                                accessory = matcher.group(3)?.trim()
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
                                    field.contains("Карма") -> ProfileModel.StatType.SITE_KARMA
                                    field.contains("Постов") -> ProfileModel.StatType.SITE_POSTS
                                    field.contains("Комментов") -> ProfileModel.StatType.SITE_COMMENTS
                                    else -> null
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
                                    field.contains("Репу") -> ProfileModel.StatType.FORUM_REPUTATION
                                    field.contains("Тем") -> ProfileModel.StatType.FORUM_TOPICS
                                    field.contains("Постов") -> ProfileModel.StatType.FORUM_POSTS
                                    else -> null
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
                                    "pos" -> ProfileModel.WarningType.POSITIVE
                                    "neg" -> ProfileModel.WarningType.NEGATIVE
                                    else -> null
                                },
                                date = matcher.group(2),
                                title = matcher.group(3).fromHtml(),
                                content = matcher.group(4).fromHtmlToSpanned()
                            )
                        )
                    }
            }
        return profile
    }
}
