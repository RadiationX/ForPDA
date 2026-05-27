package forpdateam.ru.forpda.ui.fragments.devdb.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceCommentItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.setBackgroundAttr
import forpdateam.ru.forpda.extensions.setBackgroundTintColor
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceCommentListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DeviceCommentDelegate(
    private val clickListener: (Device.Comment) -> Unit,
) : AbsListItemAdapterDelegate<DeviceCommentListItem, ListItem, DeviceCommentDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is DeviceCommentListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = DeviceCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: DeviceCommentListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: DeviceCommentItemBinding,
        private val clickListener: (Device.Comment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                binding.itemLikeBtn.context.getDrawable(R.drawable.ic_thumb_up), null, null, null
            )
            binding.itemDislikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                binding.itemLikeBtn.context.getDrawable(R.drawable.ic_thumb_down), null, null, null
            )
            binding.itemRating.setBackgroundAttr(R.attr.count_background)
        }

        fun bind(listItem: DeviceCommentListItem) {
            val item = listItem.comment
            binding.itemTitle.text = item.user.nick
            binding.itemDate.text = item.date
            binding.itemDesc.text = ApiUtils.spannedFromHtml(item.text)
            binding.itemRating.text = item.rating.toString()
            binding.itemLikeBtn.text = item.likes.toString()
            binding.itemDislikeBtn.text = item.dislikes.toString()
            binding.itemRating.setBackgroundTintColor(DevDbHelper.getColor(item.rating))
            binding.itemTitle.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}
