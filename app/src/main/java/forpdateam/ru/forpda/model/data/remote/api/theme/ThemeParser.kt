package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.user.ForumPostUser
import forpdateam.ru.forpda.entity.remote.theme.Poll
import forpdateam.ru.forpda.entity.remote.theme.PollQuestion
import forpdateam.ru.forpda.entity.remote.theme.PollQuestionItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.entity.remote.theme.ThemePost
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import javax.inject.Inject

class ThemeParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val paginationParser: PaginationParser,
    private val linkTransformer: LinkTransformer
) : BaseParser() {

    private val scope = ParserPatterns.Topic

    fun parsePage(
        response: String,
        redirectUrl: String,
        hatOpen: Boolean = false,
        pollOpen: Boolean = false
    ): ThemePage {
        var forumId: ForumId? = null
        var topicId: TopicId? = null
        var title = ""
        var desc = ""
        var favId: FavoriteId? = null
        val pageUrl = linkTransformer.parse(redirectUrl)

        require(pageUrl is Link.Board.Topic.ShowTopic.Page) {
            "Required page url but given ${pageUrl?.let { it::class.simpleName }}"
        }

        patternProvider
            .getRegexParser(scope.scope, scope.topic_id)
            .requireOnce(response) {
                forumId = ForumId(it.require(1).toInt())
                topicId = TopicId(it.require(2).toInt())
            }
        requireNotNull(topicId) { "topicId" }
        requireNotNull(forumId) { "forumId" }

        patternProvider
            .getRegexParser(scope.scope, scope.title)
            .requireOnce(response) {
                title = it.require(1).fromHtml()
                desc = it.require(2).fromHtml()
            }

        patternProvider
            .getRegexParser(scope.scope, scope.fav_id)
            .findOnce(response) {
                favId = FavoriteId(it.require(1).toInt())
            }

        val posts = parsePosts(response, topicId, forumId)
        val canQuote = posts.any { it.post.canQuote }

        val poll = parsePoll(response)

        val pagination = paginationParser.parseForum(response)

        return ThemePage(
            id = topicId,
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
            link = pageUrl,
            isHatOpen = hatOpen,
            isPollOpen = pollOpen,
            scrollY = 0,
            anchors = listOfNotNull(pageUrl.anchor),
        )
    }

    private fun parsePosts(
        response: String,
        topicId: TopicId,
        forumId: ForumId
    ) = patternProvider
        .getRegexParser(scope.scope, scope.posts)
        .map(response) { matcher ->
            val number = matcher.require(6).toInt()
            val body = matcher.require(21)
            val attachImages = parseAttachedImages(body)
            val forumPost = ForumPost(
                topicId = topicId,
                id = PostId(matcher.require(1).toInt()),
                date = matcher.require(5),
                isOnline = matcher.require(7).contains("green"),
                user = ForumPostUser(
                    id = UserId(matcher.require(10).toInt()),
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
        .getRegexParser(scope.scope, scope.poll_main)
        .mapOnce(response) { pollMatcher ->
            val isResult = pollMatcher.require(0).contains("<img")

            val questions = patternProvider
                .getRegexParser(scope.scope, scope.poll_questions)
                .map(pollMatcher.require(2)) { questionMatcher ->
                    val items = patternProvider
                        .getRegexParser(scope.scope, scope.poll_question_item)
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
                .getRegexParser(scope.scope, scope.poll_buttons)
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

    fun parseAttachedImages(text: String): List<Pair<String, String>> {
        return patternProvider
            .getRegexParser(scope.scope, scope.attached_images)
            .map(text) {
                Pair("https://${it.require(1)}", it.require(2))
            }
    }

    fun parseReportPostError(response: String): String? {
        return patternProvider
            .getRegexParser(scope.scope, scope.report_post_error)
            .mapOnce(response) { it.require(1) }
    }

    fun parseVotePostResult(response: String): Int? {
        val codeResult = patternProvider
            .getRegexParser(scope.scope, scope.vote_post_result)
            .mapOnce(response) { it.require(1).toInt() }
        if (codeResult != null) {
            return codeResult
        }
        val alreadyVoted = patternProvider
            .getRegexParser(scope.scope, scope.vote_post_already_voted)
            .mapOnce(response) { true }
        if (alreadyVoted != null) {
            return 0
        }
        return null
    }

    fun checkDeletePostSuccess(response: String): Boolean {
        return patternProvider
            .getRegexParser(scope.scope, scope.delete_post_success)
            .mapOnce(response) { true }
            ?: false
    }

}
