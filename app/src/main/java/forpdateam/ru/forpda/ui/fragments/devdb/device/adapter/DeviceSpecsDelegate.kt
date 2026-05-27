package forpdateam.ru.forpda.ui.fragments.devdb.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.DeviceSpecItemBinding
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceSpecsListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DeviceSpecsDelegate(
) : AbsListItemAdapterDelegate<DeviceSpecsListItem, ListItem, DeviceSpecsDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is DeviceSpecsListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = DeviceSpecItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(item: DeviceSpecsListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: DeviceSpecItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: DeviceSpecsListItem) {

        }
    }
}
