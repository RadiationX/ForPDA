package forpdateam.ru.forpda.model.data.remote.api.topcis

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class TopicsParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Topics

    fun parse(response: String, argId: Int): TopicsData = TopicsData().also { data ->
        patternProvider
                .getPattern(scope.scope, scope.title)
                .matcher(response)
                .also { matcher ->
                    if (matcher.find()) {
                        data.id = matcher.group(1).toInt()
                        data.title = matcher.group(2).fromHtml()
                    } else {
                        data.id = argId
                    }
                }

        patternProvider
                .getPattern(scope.scope, scope.can_new_topic)
                .matcher(response)
                .findOnce { matcher ->
                    data.setCanCreateTopic(matcher.find())
                }

        patternProvider
                .getPattern(scope.scope, scope.announce)
                .matcher(response)
                .findAll { matcher ->
                    data.addAnnounceItem(TopicItem().apply {
                        isAnnounce = true
                        announceUrl = "https://4pda.to" + matcher.group(1).replace("&amp;", "&", false)
                        title = matcher.group(2).fromHtml()
                    })
                }

        patternProvider
                .getPattern(scope.scope, scope.topics)
                .matcher(response)
                .findAll { matcher ->
                    val item = TopicItem().apply {
                        id = matcher.group(1).toInt()
                        matcher.group(2)?.also {
                            isNew = it.contains("+")
                            isPoll = it.contains("^")
                            isClosed = it.contains("Х")
                        }

                        isPinned = matcher.group(3) != null
                        title = matcher.group(4).fromHtml()
                        matcher.group(5)?.also {
                            desc = it.fromHtml()
                        }

                        authorId = matcher.group(6).toInt()
                        authorNick = matcher.group(7).fromHtml()
                        lastUserId = matcher.group(8).toInt()
                        lastUserNick = matcher.group(9).fromHtml()
                        date = matcher.group(10)
                        matcher.group(11)?.also {
                            curatorId = it.toInt()
                            curatorNick = matcher.group(12).fromHtml()
                        }
                    }
                    if (item.isPinned) {
                        data.addPinnedItem(item)
                    } else {
                        data.addTopicItem(item)
                    }
                }

        patternProvider
                .getPattern(scope.scope, scope.forum)
                .matcher(response)
                .findAll { matcher ->
                    data.addForumItem(TopicItem().apply {
                        id = matcher.group(1).toInt()
                        title = matcher.group(2).fromHtml()
                        isForum = true
                    })
                }

        data.pagination = Pagination.parseForum(response)
    }

}