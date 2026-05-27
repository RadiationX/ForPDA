package forpdateam.ru.forpda.model.data.remote.api.topcis

import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.topics.TopicFlags
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class TopicsParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val paginationParser: PaginationParser
) : BaseParser() {

    private val scope = ParserPatterns.Topics

    fun parse(response: String, argId: ForumId): TopicsData {
        var id = argId
        var title: String? = null
        patternProvider
            .getRegexParser(scope.scope, scope.title)
            .requireOnce(response) {
                id = ForumId(it.require(1).toInt())
                title = it.require(2).fromHtml()
            }

        val canCreateTopic = patternProvider
            .getRegexParser(scope.scope, scope.can_new_topic)
            .mapOnce(response) { true }
            ?: false

        val announces = patternProvider
            .getRegexParser(scope.scope, scope.announce)
            .map(response) { matcher ->
                TopicItem.Announce(
                    title = matcher.require(2).fromHtml(),
                    url = "https://4pda.to" + matcher.require(1).fromHtml()
                )
            }

        val topicItems = patternProvider
            .getRegexParser(scope.scope, scope.topics)
            .map(response) { matcher ->
                val flagsGroup = matcher.get(2)
                val flags = TopicFlags(
                    isPinned = matcher.get(3) != null,
                    isNew = flagsGroup?.contains("+") == true,
                    isPoll = flagsGroup?.contains("^") == true,
                    isClosed = flagsGroup?.contains("Х") == true,
                )
                TopicItem.Topic(
                    id = TopicId(matcher.require(1).toInt()),
                    flags = flags,
                    title = matcher.require(4).fromHtml(),
                    desc = matcher.get(5)?.fromHtml(),
                    author = User(
                        id = UserId(matcher.require(6).toInt()),
                        nick = matcher.require(7).fromHtml()
                    ),
                    lastUser = User(
                        id = UserId(matcher.require(8).toInt()),
                        nick = matcher.require(9).fromHtml()
                    ),
                    date = matcher.require(10),
                    curator = matcher.get(11)?.let {
                        User(
                            id = UserId(it.toInt()),
                            nick = matcher.require(12).fromHtml()
                        )
                    }
                )
            }


        val forums = patternProvider
            .getRegexParser(scope.scope, scope.forum)
            .map(response) { matcher ->
                TopicItem.Forum(
                    id = ForumId(matcher.require(1).toInt()),
                    title = matcher.require(2).fromHtml()
                )
            }

        val pagination = paginationParser.parseForum(response)
        return TopicsData(
            id = id,
            title = requireNotNull(title) { "title" },
            canCreateTopic = canCreateTopic,
            topicItems = topicItems,
            announceItems = announces,
            forumItems = forums,
            pagination = pagination
        )
    }

}