package forpdateam.ru.forpda.entity.remote.topics

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 01.03.17.
 */
class TopicsData {
    private var canCreateTopic = false
    var id: Int = 0
    var title: String? = null
    private val topicItems: MutableList<TopicItem> = ArrayList()
    private val pinnedItems: MutableList<TopicItem> = ArrayList()
    private val announceItems: MutableList<TopicItem> = ArrayList()
    private val forumItems: MutableList<TopicItem> = ArrayList()
    var pagination: Pagination = Pagination()

    fun canCreateTopic(): Boolean {
        return canCreateTopic
    }

    fun setCanCreateTopic(canCreateTopic: Boolean) {
        this.canCreateTopic = canCreateTopic
    }

    fun getTopicItems(): List<TopicItem> {
        return topicItems
    }

    fun addTopicItem(topicItem: TopicItem) {
        topicItems.add(topicItem)
    }

    fun getAnnounceItems(): List<TopicItem> {
        return announceItems
    }

    fun addAnnounceItem(announceItem: TopicItem) {
        announceItems.add(announceItem)
    }

    fun getPinnedItems(): List<TopicItem> {
        return pinnedItems
    }

    fun addPinnedItem(pinnedItem: TopicItem) {
        pinnedItems.add(pinnedItem)
    }

    fun getForumItems(): List<TopicItem> {
        return forumItems
    }

    fun addForumItem(forumItem: TopicItem) {
        forumItems.add(forumItem)
    }
}
