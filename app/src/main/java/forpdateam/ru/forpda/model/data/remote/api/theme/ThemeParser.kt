package forpdateam.ru.forpda.model.data.remote.api.theme

import android.util.Log
import android.util.Pair
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.theme.*
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Matcher

class ThemeParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Topic

    fun parsePage(response: String, argUrl: String, hatOpen: Boolean = false, pollOpen: Boolean = false): ThemePage = ThemePage().also { page ->
        page.isHatOpen = hatOpen
        page.isPollOpen = pollOpen
        page.url = argUrl

        patternProvider
                .getPattern(scope.scope, scope.scroll_anchor)
                .matcher(argUrl)
                .findAll {
                    page.addAnchor(it.group(1))
                }

        patternProvider
                .getPattern(scope.scope, scope.topic_id)
                .matcher(response)
                .findOnce {
                    page.forumId = it.group(1).toInt()
                    page.id = it.group(2).toInt()
                }

        page.pagination = Pagination.parseForum(response)

        patternProvider
                .getPattern(scope.scope, scope.title)
                .matcher(response)
                .findOnce {
                    page.title = it.group(1).fromHtml()
                    page.desc = it.group(2).fromHtml()
                }

        patternProvider
                .getPattern(scope.scope, scope.already_in_fav)
                .matcher(response)
                .findOnce {
                    page.isInFavorite = true
                    patternProvider
                            .getPattern(scope.scope, scope.fav_id)
                            .matcher(response)
                            .findOnce {
                                page.favId = it.group(1).toInt()
                            }
                }

        var attachMatcher: Matcher? = null
        val posts = patternProvider
                .getPattern(scope.scope, scope.posts)
                .matcher(response)
                .map { matcher ->
                    ThemePost().apply {
                        topicId = page.id
                        forumId = page.forumId
                        id = matcher.group(1).toInt()
                        date = matcher.group(5)
                        number = matcher.group(6).toInt()
                        isOnline = matcher.group(7).contains("green")
                        matcher.group(8).also {
                            avatar = if (!it.isEmpty()) "https://s.4pda.to/forum/uploads/$it" else it
                        }
                        nick = matcher.group(9).fromHtml()
                        userId = matcher.group(10).toInt()
                        isCurator = matcher.group(11) != null
                        groupColor = matcher.group(12)
                        group = matcher.group(13)
                        canMinusRep = !matcher.group(14).isEmpty()
                        reputation = matcher.group(15)
                        canPlusRep = !matcher.group(16).isEmpty()
                        canReport = !matcher.group(17).isEmpty()
                        canEdit = !matcher.group(18).isEmpty()
                        canDelete = !matcher.group(19).isEmpty()
                        page.canQuote = !matcher.group(20).isEmpty()
                        canQuote = page.canQuote
                        body = matcher.group(21)
                        attachMatcher = attachMatcher?.reset(body) ?: patternProvider
                                .getPattern(scope.scope, scope.attached_images)
                                .matcher(body)
                        attachMatcher
                                ?.findAll {
                                    attachImages.add(Pair("https://${it.group(1)}", it.group(2)))
                                }
                    }

                    /*if (isCurator() && getUserId() == ClientHelper.getUserId())
                        page.setCurator(true);*/
                }
        page.posts.addAll(posts)

        patternProvider
                .getPattern(scope.scope, scope.poll_main)
                .matcher(response)
                .findOnce { matcher ->
                    val isResult = matcher.group().contains("<img")

                    val poll = Poll()
                    poll.isResult = isResult
                    poll.title = matcher.group(1).fromHtml()

                    val questions = patternProvider
                            .getPattern(scope.scope, scope.poll_questions)
                            .matcher(matcher.group(2))
                            .map {
                                PollQuestion().apply {
                                    title = it.group(1).fromHtml()
                                    val items = patternProvider
                                            .getPattern(scope.scope, scope.poll_question_item)
                                            .matcher(it.group(2))
                                            .map {
                                                PollQuestionItem().apply {
                                                    if (!isResult) {
                                                        type = it.group(1)
                                                        name = it.group(2).fromHtml()
                                                        value = it.group(3).toInt()
                                                        title = it.group(4).fromHtml()
                                                    } else {
                                                        title = it.group(5).fromHtml()
                                                        votes = it.group(6).toInt()
                                                        percent = java.lang.Float.parseFloat(it.group(7).replace(",", "."))
                                                    }
                                                }
                                            }
                                    this.questionItems.addAll(items)
                                }
                            }
                    poll.questions.addAll(questions)

                    patternProvider
                            .getPattern(scope.scope, scope.poll_buttons)
                            .matcher(matcher.group(4))
                            .findAll {
                                val value = it.group(1)
                                when {
                                    value.contains("Голосовать") -> poll.voteButton = true
                                    value.contains("результаты") -> poll.showResultsButton = true
                                    value.contains("пункты опроса") -> poll.showPollButton = true
                                }
                            }

                    poll.votesCount = matcher.group(3).toInt()
                    page.poll = poll
                }
        return page
    }

}
