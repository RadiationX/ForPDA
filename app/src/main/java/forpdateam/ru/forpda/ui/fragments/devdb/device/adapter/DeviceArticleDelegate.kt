package forpdateam.ru.forpda.ui.fragments.devdb.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.databinding.DevicePostNewsItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceArticleListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DeviceArticleDelegate(
    private val clickListener: (Device.Article) -> Unit,
) : AbsListItemAdapterDelegate<DeviceArticleListItem, ListItem, DeviceArticleDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is DeviceArticleListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = DevicePostNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: DeviceArticleListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: DevicePostNewsItemBinding,
        private val clickListener: (Device.Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: DeviceArticleListItem) {
            val item = listItem.article
            binding.itemTitle.text = item.title
            binding.itemDate.text = item.date
            if (item.desc != null) {
                binding.itemDesc.text = ApiUtils.spannedFromHtml(item.desc)
                binding.itemDesc.visibility = View.VISIBLE
            } else {
                binding.itemDesc.visibility = View.GONE
            }
            ImageLoader.getInstance().displayImage(item.image, binding.itemImage)
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}
