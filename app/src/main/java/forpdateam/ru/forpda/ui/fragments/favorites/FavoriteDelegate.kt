package forpdateam.ru.forpda.ui.fragments.favorites

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.TopicItemBinding
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.FavoriteListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class FavoriteDelegate(
    private val clickListener: OnItemClickListener<Favorite>,
) : AbsListItemAdapterDelegate<FavoriteListItem, ListItem, FavoriteDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is FavoriteListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = TopicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: FavoriteListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: TopicItemBinding,
        private val clickListener: OnItemClickListener<Favorite>
    ) : RecyclerView.ViewHolder(binding.root) {

        private val titleColor = binding.root.context.getColorFromAttr(R.attr.second_text_color)
        private val titleColorNew = binding.root.context.getColorFromAttr(R.attr.default_text_color)

        fun bind(listItem: FavoriteListItem) {
            val item = listItem.item
            binding.topicItemTitle.text = item.title
            binding.topicItemTitle.setTypeface(if (item.isNew) Typeface.DEFAULT_BOLD else Typeface.DEFAULT)
            binding.topicItemTitle.setTextColor(if (item.isNew) titleColorNew else titleColor)
            binding.topicItemDot.isVisible = listItem.showDot && item.isNew
            binding.topicItemForumIcon.isVisible = when (item) {
                is Favorite.Topic -> false
                is Favorite.Forum -> true
            }
            binding.topicItemLockIcon.isVisible = when (item) {
                is Favorite.Topic -> item.isClosed
                is Favorite.Forum -> false
            }
            binding.topicItemPollIcon.isVisible = when (item) {
                is Favorite.Topic -> item.isPoll
                is Favorite.Forum -> false
            }
            binding.topicItemLastNick.text = when (item) {
                is Favorite.Topic -> item.lastUser.nick
                is Favorite.Forum -> item.lastUser?.nick
            }
            binding.topicItemDate.text = item.date
            binding.topicItemDesc.isVisible = false
            clickListener.attachTo(binding.root, listItem.item)
        }
    }
}
