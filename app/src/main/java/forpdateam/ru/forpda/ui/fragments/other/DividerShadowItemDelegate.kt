package forpdateam.ru.forpda.ui.fragments.other

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.drawers.adapters.DividerShadowListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class DividerShadowItemDelegate : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is DividerShadowListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_divider_shadow, parent, false))

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
