package forpdateam.ru.forpda.ui.fragments.forum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.App.Companion.getDrawableResAttr
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ForumItemDefaultBinding
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.ForumListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class ForumDelegate(
    private val clickListener: OnItemClickListener<ForumListItem>,
) : AbsListItemAdapterDelegate<ForumListItem, ListItem, ForumDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is ForumListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = ForumItemDefaultBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: ForumListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: ForumItemDefaultBinding,
        private val clickListener: OnItemClickListener<ForumListItem>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: ForumListItem) {
            val item = listItem.item
            binding.forumItemTitle.text = item.title

            val iconRes = if (listItem.isLeaf) {
                R.drawable.ic_forum_go_to_topics
            } else {
                if (listItem.expanded) {
                    R.drawable.ic_expand_less_black_24dp
                } else {
                    R.drawable.ic_expand_more_black_24dp
                }
            }
            binding.forumItemIcon.setImageDrawable(getVecDrawable(binding.forumItemIcon.context, iconRes))
            if (listItem.isLeaf) {
                val bg = getDrawableResAttr(binding.forumItemIcon.context, R.attr.count_background)
                binding.forumItemIcon.setBackgroundResource(bg)
            } else {
                binding.forumItemIcon.background = null
            }
            clickListener.attachTo(binding.root, listItem)
        }
    }
}
