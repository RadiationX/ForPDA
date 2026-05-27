package forpdateam.ru.forpda.ui.fragments.devdb.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.DevicePostForumItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceTopicListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DeviceTopicDelegate(
    private val clickListener: (Device.Topic) -> Unit,
) : AbsListItemAdapterDelegate<DeviceTopicListItem, ListItem, DeviceTopicDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is DeviceTopicListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = DevicePostForumItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: DeviceTopicListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: DevicePostForumItemBinding,
        private val clickListener: (Device.Topic) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: DeviceTopicListItem) {
            val item = listItem.topic
            binding.itemTitle.text = item.title
            binding.itemDate.text = item.date
            if (item.desc != null) {
                binding.itemDesc.text = ApiUtils.spannedFromHtml(item.desc)
                binding.itemDesc.visibility = View.VISIBLE
            } else {
                binding.itemDesc.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}
