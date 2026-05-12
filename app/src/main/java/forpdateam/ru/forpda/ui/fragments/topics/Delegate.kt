package forpdateam.ru.forpda.ui.fragments.topics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.ItemOtherProfileBinding
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem

class Delegate(
    private val clickListener: OnItemClickListener<ForumUser>,
) : AbsListItemAdapterDelegate<ProfileListItem, ListItem, Delegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is ProfileListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = ItemOtherProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(
        item: ProfileListItem,
        holder: ViewHolder,
        payloads: List<Any?>
    ) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: ItemOtherProfileBinding,
        private val clickListener: OnItemClickListener<ForumUser>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: ProfileListItem) {

            clickListener.attachTo(binding.root, listItem.user!!)
        }
    }
}
