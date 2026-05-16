package forpdateam.ru.forpda.ui.fragments.topics

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.TopicItemBinding
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TopicListItem

class TopicDelegate(
    private val clickListener: OnItemClickListener<TopicItem.Topic>,
) : AbsListItemAdapterDelegate<TopicListItem, ListItem, TopicDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is TopicListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = TopicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: TopicListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: TopicItemBinding,
        private val clickListener: OnItemClickListener<TopicItem.Topic>
    ) : RecyclerView.ViewHolder(binding.root) {

        private val titleColor = binding.root.context.getColorFromAttr(R.attr.second_text_color)
        private val titleColorNew = binding.root.context.getColorFromAttr(R.attr.default_text_color)

        fun bind(listItem: TopicListItem) {
            val item = listItem.item
            binding.topicItemTitle.text = item.title
            binding.topicItemTitle.setTypeface(if (item.flags.isNew) Typeface.DEFAULT_BOLD else Typeface.DEFAULT)
            binding.topicItemTitle.setTextColor(if (item.flags.isNew) titleColorNew else titleColor)
            binding.topicItemDesc.text = item.desc
            binding.topicItemDesc.isVisible = item.desc != null
            binding.topicItemLockIcon.setVisibility(if (item.flags.isClosed) View.VISIBLE else View.GONE)
            binding.topicItemPollIcon.setVisibility(if (item.flags.isPoll) View.VISIBLE else View.GONE)
            binding.topicItemLastNick.text = item.lastUser.nick
            binding.topicItemDate.text = item.date
            clickListener.attachTo(binding.root, item)
        }

    }
}
