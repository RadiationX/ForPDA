package forpdateam.ru.forpda.ui.fragments.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.drawers.adapters.DividerShadowListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DividerShadowItemDelegate : AdapterDelegate<List<ListItem>>() {
    override fun isForViewType(items: List<ListItem>, position: Int): Boolean =
        items[position] is DividerShadowListItem

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<Any>
    ) {
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_other_divider_shadow, parent, false)
        )

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
