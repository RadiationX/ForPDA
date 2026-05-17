package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ProfileSubItemInfoBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.repository.temp.TempHelper
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
        private val binding by viewBinding<ProfileSubItemInfoBinding>()

        override fun bind(item: ProfileModel.Info) {
            binding.itemTitle.text = TempHelper.getTypeString(binding.itemTitle.context, item.type)
            binding.itemValue.text = item.value
        }
    }
}