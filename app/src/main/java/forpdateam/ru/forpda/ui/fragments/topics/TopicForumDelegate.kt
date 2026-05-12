package forpdateam.ru.forpda.ui.fragments.topics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.TopicItemAnnounceBinding
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicForumListItem

class TopicForumDelegate(
    private val clickListener: OnItemClickListener<TopicItem.Forum>,
) : AbsListItemAdapterDelegate<TopicForumListItem, ListItem, TopicForumDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is TopicForumListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = TopicItemAnnounceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: TopicForumListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: TopicItemAnnounceBinding,
        private val clickListener: OnItemClickListener<TopicItem.Forum>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: TopicForumListItem) {
            binding.topicItemTitle.text = listItem.item.title
            clickListener.attachTo(binding.root, listItem.item)
        }
    }
}
