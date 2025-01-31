package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.repository.temp.TempHelper.getTypeString
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 14.09.17.
 */
internal class InfoAdapter : BaseAdapter<ProfileModel.Info, InfoAdapter.InfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        return InfoHolder(inflateLayout(parent, R.layout.profile_sub_item_info))
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal inner class InfoHolder(itemView: View) : BaseViewHolder<ProfileModel.Info>(itemView) {
        private val title: TextView =
            itemView.findViewById(R.id.item_title)
        private val value: TextView =
            itemView.findViewById(R.id.item_value)

        override fun bind(item: ProfileModel.Info) {
            title.text = getTypeString(title.context, item.type)
            value.text = item.value
        }
    }
}