package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ProfileSubItemDeviceBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 15.09.17.
 */
internal class DevicesAdapter(
    private val listener: InfoHolder.Listener
) : BaseAdapter<ProfileModel.Device, DevicesAdapter.InfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        return InfoHolder(
            inflateLayout(parent, R.layout.profile_sub_item_device),
            listener
        )
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class InfoHolder(
        itemView: View,
        listener: Listener
    ) : BaseViewHolder<ProfileModel.Device>(itemView) {
        private val binding by viewBinding<ProfileSubItemDeviceBinding>()
        private var currentItem: ProfileModel.Device? = null

        init {
            itemView.setOnClickListener { v: View? -> listener.onClick(currentItem) }
        }

        override fun bind(item: ProfileModel.Device) {
            currentItem = item
            binding.itemTitle.text = String.format("${item.name} ${item.accessory}")
        }

        internal interface Listener {
            fun onClick(item: ProfileModel.Device?)
        }
    }
}