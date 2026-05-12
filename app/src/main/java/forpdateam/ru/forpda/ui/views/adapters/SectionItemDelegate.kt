package forpdateam.ru.forpda.ui.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.TopicItemSectionBinding
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.SectionListItem

class SectionItemDelegate : AbsListItemAdapterDelegate<SectionListItem, ListItem, SectionItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is SectionListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = TopicItemSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(item: SectionListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: TopicItemSectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SectionListItem) {
            binding.topicItemTopDivider.isVisible = item.topDivider
            binding.topicItemTitle.text = when (item) {
                is SectionListItem.String -> item.title
                is SectionListItem.Res -> binding.root.context.getString(item.titleRes)
            }
        }
    }
}
