package forpdateam.ru.forpda.model.data.remote.api.topcis

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.topics.TopicFlags
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class TopicsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Topics

    fun parse(response: String, argId: Int): TopicsData {
        var id = argId
        var title: String
        patternProvider
            .getPattern(scope.scope, scope.title)
            .matcher(response)
            .requireOnce {
                id = it.group(1).toInt()
                title = it.group(2).fromHtml()!!
            }

        val canCreateTopic = patternProvider
            .getPattern(scope.scope, scope.can_new_topic)
            .matcher(response)
            .mapOnce { true }
            ?: false

        val announces = patternProvider
            .getPattern(scope.scope, scope.announce)
            .matcher(response)
            .map { matcher ->
                TopicItem.Announce(
                    title = matcher.group(2).fromHtml()!!,
                    url = "https://4pda.to" + matcher.group(1).replace("&amp;", "&", false)
                )
            }

        val topicItems = patternProvider
            .getPattern(scope.scope, scope.topics)
            .matcher(response)
            .map { matcher ->
                val flagsGroup = matcher.group(2)
                val flags = TopicFlags(
                    isPinned = matcher.group(3) != null,
                    isNew = flagsGroup?.contains("+") == true,
                    isPoll = flagsGroup?.contains("^") == true,
                    isClosed = flagsGroup?.contains("Х") == true,
                )
                TopicItem.Topic(
                    id = matcher.group(1).toInt(),
                    flags = flags,
                    title = matcher.group(4).fromHtml()!!,
                    desc = matcher.group(5)?.fromHtml(),
                    author = User(
                        id = matcher.group(6).toInt(),
                        nick = matcher.group(7).fromHtml()!!
                    ),
                    lastUser = User(
                        id = matcher.group(8).toInt(),
                        nick = matcher.group(9).fromHtml()!!
                    ),
                    date = matcher.group(10),
                    curator = matcher.group(11)?.let {
                        User(
                            id = it.toInt(),
                            nick = matcher.group(12).fromHtml()!!
                        )
                    }
                )
            }


        val forums = patternProvider
            .getPattern(scope.scope, scope.forum)
            .matcher(response)
            .map { matcher ->
                TopicItem.Forum(
                    id = matcher.group(1).toInt(),
                    title = matcher.group(2).fromHtml()!!
                )
            }

        val pagination = Pagination.parseForum(response)
        return TopicsData(
            id = id,
            title = title,
            canCreateTopic = canCreateTopic,
            topicItems = topicItems,
            announceItems = announces,
            forumItems = forums,
            pagination = pagination
        )
    }

}