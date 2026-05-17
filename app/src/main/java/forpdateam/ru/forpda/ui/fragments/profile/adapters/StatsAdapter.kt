package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ProfileSubItemStatBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.Stat
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import forpdateam.ru.forpda.ui.fragments.profile.adapters.StatsAdapter.StatHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 14.09.17.
 */
internal class StatsAdapter(
    private val listener: StatHolder.Listener
) : BaseAdapter<Stat, StatHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatHolder {
        return StatHolder(
            inflateLayout(parent, R.layout.profile_sub_item_stat),
            listener
        )
    }

    override fun onBindViewHolder(holder: StatHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class StatHolder(
        itemView: View,
        listener: Listener
    ) : BaseViewHolder<Stat>(itemView) {
        private val binding by viewBinding<ProfileSubItemStatBinding>()
        private var currentItem: Stat? = null

        init {
            itemView.setOnClickListener { v: View? -> listener.onClick(requireNotNull(currentItem)) }
        }

        override fun bind(item: Stat) {
            currentItem = item
            binding.itemTitle.text = TempHelper.getTypeString(binding.itemTitle.context, item.type)
            binding.itemValue.text = item.value
        }

        internal interface Listener {
            fun onClick(item: Stat)
        }
    }
}
