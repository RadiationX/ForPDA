package forpdateam.ru.forpda.ui.fragments.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem

class MenuItemDelegate(
    private val clickListener: (DrawerMenuItem) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean =
        items[position] is MenuListItem

    override fun onBindViewHolder(
        items: MutableList<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val item = items[position] as MenuListItem
        (holder as ViewHolder).bind(item.menuItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_other_menu, parent, false),
            clickListener
        )

    class ViewHolder(
        val view: View,
        val clickListener: (DrawerMenuItem) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val otherMenuCounter: TextView = view.findViewById(R.id.otherMenuCounter)
        private val otherMenuIcon: ImageView = view.findViewById(R.id.otherMenuIcon)
        private val otherMenuTitle: TextView = view.findViewById(R.id.otherMenuTitle)

        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener(currentItem) }
        }

        fun getItem() = currentItem

        fun bind(item: DrawerMenuItem) {
            this.currentItem = item
            view.apply {
                otherMenuTitle.setText(item.title)
                otherMenuIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        view.context,
                        item.icon
                    )
                )
                otherMenuCounter.text = item.appItem.count.toString()
                otherMenuCounter.visibility =
                    if (item.appItem.count > 0) View.VISIBLE else View.GONE
            }
        }
    }
}
