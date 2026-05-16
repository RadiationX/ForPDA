package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ProfileSubItemWarningBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.WarningType
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.ui.fragments.profile.adapters.WarningsAdapter.WarningHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 15.09.17.
 */
internal class WarningsAdapter : BaseAdapter<ProfileModel.Warning, WarningHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningHolder {
        return WarningHolder(inflateLayout(parent, R.layout.profile_sub_item_warning))
    }

    override fun onBindViewHolder(holder: WarningHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WarningHolder(itemView: View) : BaseViewHolder<ProfileModel.Warning>(itemView) {
        private val binding by viewBinding<ProfileSubItemWarningBinding>()

        override fun bind(item: ProfileModel.Warning) {
            binding.itemTitle.text = item.title
            binding.itemDate.text = item.date
            binding.itemContent.text = item.content
            val color = when (item.type) {
                WarningType.Positive -> {
                    binding.itemTitle.context.getColor(R.color.md_green_400)
                }

                WarningType.Negative -> {
                    binding.itemTitle.context.getColor(R.color.md_red_400)
                }

                WarningType.Unknown -> {
                    binding.itemTitle.context.getColorFromAttr(R.attr.default_text_color)
                }
            }
            binding.itemTitle.setTextColor(color)
        }
    }
}