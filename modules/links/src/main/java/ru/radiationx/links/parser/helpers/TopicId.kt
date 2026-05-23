package ru.radiationx.links.parser.helpers

import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

internal enum class TopicIdCase(val value: String) {
    ShowTopic("showtopic"),
    Search("topics"),
    SearchArray("topics[]")
}

internal fun String.parseTopicId(): TopicId? {
    return toIntOrNull()?.let { TopicId(it) }
}

internal fun LinkUrl.parseTopicId(case: TopicIdCase): TopicId? {
    return query(case.value)?.parseTopicId()
}

internal fun LinkUrlBuilder.query(topicId: TopicId, case: TopicIdCase) {
    query(case.value, topicId.id)
}
