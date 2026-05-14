package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.theme.Poll
import forpdateam.ru.forpda.entity.remote.theme.PollQuestion
import forpdateam.ru.forpda.entity.remote.theme.PollQuestionItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.entity.remote.theme.ThemePost
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class ThemeParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Topic

    fun parsePage(
        response: String,
        argUrl: String,
        hatOpen: Boolean = false,
        pollOpen: Boolean = false
    ): ThemePage {
        var forumId = 0
        var id = 0
        var title = ""
        var desc = ""
        var favId: Int? = null
        val anchors = patternProvider
            .getParserPattern(scope.scope, scope.scroll_anchor)
            .map(argUrl) { it.require(1) }

        patternProvider
            .getParserPattern(scope.scope, scope.topic_id)
            .requireOnce(response) {
                forumId = it.require(1).toInt()
                id = it.require(2).toInt()
            }

        patternProvider
            .getParserPattern(scope.scope, scope.title)
            .requireOnce(response) {
                title = it.require(1).fromHtml()
                desc = it.require(2).fromHtml()
            }

        patternProvider
            .getParserPattern(scope.scope, scope.fav_id)
            .findOnce(response) {
                favId = it.require(1).toInt()
            }

        val posts = parsePosts(response, id, forumId)
        val canQuote = posts.any { it.post.canQuote }

        val poll = parsePoll(response)

        val pagination = Pagination.parseForum(response)

        return ThemePage(
            id = id,
            title = title,
            desc = desc,
            forumId = forumId,
            isInFavorite = favId != null,
            favId = favId,
            canQuote = canQuote,
            posts = posts,
            pagination = pagination,
            poll = poll,
            html = null,
            url = argUrl,
            isHatOpen = hatOpen,
            isPollOpen = pollOpen,
            scrollY = 0,
            anchors = anchors,
        )
    }

    private fun parsePosts(
        response: String,
        id: Int,
        forumId: Int
    ) = patternProvider
        .getParserPattern(scope.scope, scope.posts)
        .map(response) { matcher ->
            val number = matcher.require(6).toInt()
            val body = matcher.require(21)
            val attachImages = patternProvider
                .getParserPattern(scope.scope, scope.attached_images)
                .map(body) {
                    Pair("https://${it.require(1)}", it.require(2))
                }
            val forumPost = ForumPost(
                topicId = id,
                id = matcher.require(1).toInt(),
                date = matcher.require(5),
                isOnline = matcher.require(7).contains("green"),
                user = ForumUser.required(
                    id = matcher.require(10).toInt(),
                    nick = matcher.require(9).fromHtml(),
                    avatar = matcher.require(8).let {
                        if (it.isNotEmpty()) "https://s.4pda.to/forum/uploads/$it" else null
                    },
                ),
                isCurator = matcher.get(11) != null,
                groupColor = matcher.require(12),
                group = matcher.require(13),
                canMinusRep = matcher.require(14).isNotEmpty(),
                reputation = matcher.require(15),
                canPlusRep = matcher.require(16).isNotEmpty(),
                canReport = matcher.require(17).isNotEmpty(),
                canEdit = matcher.require(18).isNotEmpty(),
                canDelete = matcher.require(19).isNotEmpty(),
                canQuote = matcher.require(20).isNotEmpty(),
                body = body
            )
            ThemePost(
                forumId = forumId,
                number = number,
                attachImages = attachImages,
                post = forumPost
            )
        }

    private fun parsePoll(response: String) = patternProvider
        .getParserPattern(scope.scope, scope.poll_main)
        .mapOnce(response) { pollMatcher ->
            val isResult = pollMatcher.require(0).contains("<img")

            val questions = patternProvider
                .getParserPattern(scope.scope, scope.poll_questions)
                .map(pollMatcher.require(2)) { questionMatcher ->
                    val items = patternProvider
                        .getParserPattern(scope.scope, scope.poll_question_item)
                        .map(questionMatcher.require(2)) {
                            if (isResult) {
                                PollQuestionItem.Result(
                                    title = it.require(5).fromHtml(),
                                    votes = it.require(6).toInt(),
                                    percent = it.require(7).replace(",", ".").toFloat()
                                )
                            } else {
                                PollQuestionItem.Regular(
                                    type = it.require(1),
                                    name = it.require(2).fromHtml(),
                                    value = it.require(3).toInt(),
                                    title = it.require(4).fromHtml(),
                                )
                            }
                        }
                    PollQuestion(
                        title = questionMatcher.require(1).fromHtml(),
                        questionItems = items
                    )
                }

            var voteButton = false
            var showResultsButton = false
            var showPollButton = false
            patternProvider
                .getParserPattern(scope.scope, scope.poll_buttons)
                .findAll(pollMatcher.require(4)) {
                    val value = it.require(1)
                    when {
                        value.contains("Голосовать") -> voteButton = true
                        value.contains("результаты") -> showResultsButton = true
                        value.contains("пункты опроса") -> showPollButton = true
                    }
                }

            Poll(
                title = pollMatcher.require(1).fromHtml(),
                isResult = isResult,
                votesCount = pollMatcher.require(3).toInt(),
                voteButton = voteButton,
                showResultsButton = showResultsButton,
                showPollButton = showPollButton,
                questions = questions
            )
        }

}
