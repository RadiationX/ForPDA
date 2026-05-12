package forpdateam.ru.forpda.ui.fragments.topics

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.adapters.SectionItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.buildSections
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicAnnounceListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicForumListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicListItem

class TopicsAdapter(
    private val topicClickListener: OnItemClickListener<TopicItem>,
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.apply {
            addDelegate(SectionItemDelegate())
            addDelegate(TopicAnnounceDelegate(topicClickListener))
            addDelegate(TopicForumDelegate(topicClickListener))
            addDelegate(TopicDelegate(topicClickListener))
        }
    }

    fun bindItems(data: TopicsData) {
        this.items = buildSections {
            val pinnedItems = data.topicItems.filter { it.flags.isPinned }
            val notPinnedItems = data.topicItems.filter { !it.flags.isPinned }
            addSection(R.string.forum_section, data.forumItems) {
                TopicForumListItem(it)
            }
            addSection(R.string.announce_section, data.announceItems) {
                TopicAnnounceListItem(it)
            }
            addSection(R.string.pinned_section, pinnedItems) {
                TopicListItem(it)
            }
            addSection(R.string.themes_section, notPinnedItems) {
                TopicListItem(it)
            }
        }
        notifyDataSetChanged()
    }
}