package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.theme.Poll
import forpdateam.ru.forpda.entity.remote.theme.PollQuestion
import forpdateam.ru.forpda.entity.remote.theme.PollQuestionItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.entity.remote.theme.ThemePost
import forpdateam.ru.forpda.extensions.findAll
import forpdateam.ru.forpda.extensions.findOnce
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
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
        var isInFavorite = false
        var favId = 0
        val anchors = patternProvider
            .getPattern(scope.scope, scope.scroll_anchor)
            .matcher(argUrl)
            .map { it.group(1)!! }

        patternProvider
            .getPattern(scope.scope, scope.topic_id)
            .matcher(response)
            .requireOnce {
                forumId = it.group(1).toInt()
                id = it.group(2).toInt()
            }

        patternProvider
            .getPattern(scope.scope, scope.title)
            .matcher(response)
            .requireOnce {
                title = it.group(1).fromHtml()!!
                desc = it.group(2).fromHtml()!!
            }

        patternProvider
            .getPattern(scope.scope, scope.already_in_fav)
            .matcher(response)
            .findOnce {
                isInFavorite = true
                patternProvider
                    .getPattern(scope.scope, scope.fav_id)
                    .matcher(response)
                    .findOnce {
                        favId = it.group(1).toInt()
                    }
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
            favId = favId,
            isInFavorite = isInFavorite,
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
        .getPattern(scope.scope, scope.posts)
        .matcher(response)
        .map { matcher ->
            val number = matcher.group(6).toInt()
            val body = matcher.group(21)
            val attachImages = patternProvider
                .getPattern(scope.scope, scope.attached_images)
                .matcher(body)
                .map { Pair("https://${it.group(1)}", it.group(2)) }
            val forumPost = ForumPost(
                topicId = id,
                id = matcher.group(1).toInt(),
                date = matcher.group(5),
                isOnline = matcher.group(7).contains("green"),
                user = ForumUser.required(
                    id = matcher.group(10).toInt(),
                    nick = matcher.group(9).fromHtml(),
                    avatar = matcher.group(8)!!.let {
                        if (it.isNotEmpty()) "https://s.4pda.to/forum/uploads/$it" else null
                    },
                ),
                isCurator = matcher.group(11) != null,
                groupColor = matcher.group(12) ?: "black",
                group = matcher.group(13),
                canMinusRep = matcher.group(14).isNotEmpty(),
                reputation = matcher.group(15),
                canPlusRep = matcher.group(16).isNotEmpty(),
                canReport = matcher.group(17).isNotEmpty(),
                canEdit = matcher.group(18).isNotEmpty(),
                canDelete = matcher.group(19).isNotEmpty(),
                canQuote = matcher.group(20).isNotEmpty(),
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
        .getPattern(scope.scope, scope.poll_main)
        .matcher(response)
        .mapOnce { matcher ->
            val isResult = matcher.group().contains("<img")

            val questions = patternProvider
                .getPattern(scope.scope, scope.poll_questions)
                .matcher(matcher.group(2))
                .map {
                    val items = patternProvider
                        .getPattern(scope.scope, scope.poll_question_item)
                        .matcher(it.group(2))
                        .map {
                            if (isResult) {
                                PollQuestionItem.Result(
                                    title = it.group(5).fromHtml()!!,
                                    votes = it.group(6).toInt(),
                                    percent = it.group(7).replace(",", ".").toFloat()
                                )
                            } else {
                                PollQuestionItem.Regular(
                                    type = it.group(1),
                                    name = it.group(2).fromHtml()!!,
                                    value = it.group(3).toInt(),
                                    title = it.group(4).fromHtml()!!,
                                )
                            }
                        }
                    PollQuestion(
                        title = it.group(1).fromHtml()!!,
                        questionItems = items
                    )
                }

            var voteButton = false
            var showResultsButton = false
            var showPollButton = false
            patternProvider
                .getPattern(scope.scope, scope.poll_buttons)
                .matcher(matcher.group(4))
                .findAll {
                    val value = it.group(1)
                    when {
                        value.contains("Голосовать") -> voteButton = true
                        value.contains("результаты") -> showResultsButton = true
                        value.contains("пункты опроса") -> showPollButton = true
                    }
                }

            Poll(
                title = matcher.group(1).fromHtml(),
                isResult = isResult,
                votesCount = matcher.group(3).toInt(),
                voteButton = voteButton,
                showResultsButton = showResultsButton,
                showPollButton = showPollButton,
                questions = questions
            )
        }

}
