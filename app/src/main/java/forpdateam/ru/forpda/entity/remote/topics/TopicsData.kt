package forpdateam.ru.forpda.entity.remote.topics

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 01.03.17.
 */
class TopicsData {
    private var canCreateTopic = false
    var id: Int = 0
    var title: String? = null
    val topicItems: MutableList<TopicItem> = ArrayList()
    val pinnedItems: MutableList<TopicItem> = ArrayList()
    val announceItems: MutableList<TopicItem> = ArrayList()
    val forumItems: MutableList<TopicItem> = ArrayList()
    var pagination: Pagination = Pagination.createForumDefault()

    fun canCreateTopic(): Boolean {
        return canCreateTopic
    }

    fun setCanCreateTopic(canCreateTopic: Boolean) {
        this.canCreateTopic = canCreateTopic
    }


    fun addTopicItem(topicItem: TopicItem) {
        topicItems.add(topicItem)
    }


    fun addAnnounceItem(announceItem: TopicItem) {
        announceItems.add(announceItem)
    }

    fun addPinnedItem(pinnedItem: TopicItem) {
        pinnedItems.add(pinnedItem)
    }


    fun addForumItem(forumItem: TopicItem) {
        forumItems.add(forumItem)
    }
}
