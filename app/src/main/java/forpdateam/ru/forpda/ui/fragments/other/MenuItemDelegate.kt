package forpdateam.ru.forpda.ui.fragments.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemOtherMenuBinding
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem

class MenuItemDelegate(
    private val clickListener: (DrawerMenuItem) -> Unit
) : AdapterDelegate<List<ListItem>>() {

    override fun isForViewType(items: List<ListItem>, position: Int): Boolean =
        items[position] is MenuListItem

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<Any>
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

        private val binding by viewBinding<ItemOtherMenuBinding>()

        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener(currentItem) }
        }

        fun getItem() = currentItem

        fun bind(item: DrawerMenuItem) {
            this.currentItem = item
            view.apply {
                binding.otherMenuTitle.setText(item.title)
                binding.otherMenuIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        view.context,
                        item.icon
                    )
                )
                binding.otherMenuCounter.text = item.appItem.count.toString()
                binding.otherMenuCounter.visibility =
                    if (item.appItem.count > 0) View.VISIBLE else View.GONE
            }
        }
    }
}
