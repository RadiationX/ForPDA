package forpdateam.ru.forpda.ui.fragments.topics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.TopicItemAnnounceBinding
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicAnnounceListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class TopicAnnounceDelegate(
    private val clickListener: OnItemClickListener<TopicItem.Announce>,
) : AbsListItemAdapterDelegate<TopicAnnounceListItem, ListItem, TopicAnnounceDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is TopicAnnounceListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = TopicItemAnnounceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: TopicAnnounceListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: TopicItemAnnounceBinding,
        private val clickListener: OnItemClickListener<TopicItem.Announce>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: TopicAnnounceListItem) {
            binding.topicItemTitle.text = listItem.item.title
            clickListener.attachTo(binding.root, listItem.item)
        }
    }
}
