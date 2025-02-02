package forpdateam.ru.forpda.entity.remote.topics

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 01.03.17.
 */
data class TopicsData(
    val id: Int,
    val title: String,
    val canCreateTopic: Boolean,
    val topicItems: List<TopicItem.Topic>,
    val announceItems: List<TopicItem.Announce>,
    val forumItems: List<TopicItem.Forum>,
    val pagination: Pagination
)
