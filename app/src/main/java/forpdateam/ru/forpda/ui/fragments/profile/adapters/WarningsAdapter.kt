package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.WarningType
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
        private val title: TextView =
            itemView.findViewById(R.id.item_title)
        private val date: TextView =
            itemView.findViewById(R.id.item_date)
        private val content: TextView =
            itemView.findViewById(R.id.item_content)

        override fun bind(item: ProfileModel.Warning) {
            title.text = item.title
            date.text = item.date
            content.text = item.content
            when (item.type) {
                WarningType.POSITIVE -> title.setTextColor(
                    ContextCompat.getColor(
                        title.context,
                        R.color.md_green_400
                    )
                )

                WarningType.NEGATIVE -> title.setTextColor(
                    ContextCompat.getColor(
                        title.context,
                        R.color.md_red_400
                    )
                )

                null -> {
                    // do nothing
                }
            }
        }
    }
}