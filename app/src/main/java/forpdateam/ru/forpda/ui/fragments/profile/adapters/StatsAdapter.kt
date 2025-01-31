package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.Stat
import forpdateam.ru.forpda.model.repository.temp.TempHelper.getTypeString
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
        private val title: TextView = itemView.findViewById(R.id.item_title)
        private val value: TextView = itemView.findViewById(R.id.item_value)
        private var currentItem: Stat? = null

        init {
            itemView.setOnClickListener { v: View? -> listener.onClick(requireNotNull(currentItem)) }
        }

        override fun bind(item: Stat) {
            currentItem = item
            title.text = getTypeString(title.context, item.type)
            value.text = item.value
        }

        internal interface Listener {
            fun onClick(item: Stat)
        }
    }
}
