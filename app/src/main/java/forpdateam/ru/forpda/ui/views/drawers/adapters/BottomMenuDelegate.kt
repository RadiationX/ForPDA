package forpdateam.ru.forpda.ui.views.drawers.adapters

import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemBottomTabBinding

class BottomMenuDelegate(
    private val clickListener: Listener
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean =
        items[position] is BottomTabListItem

    override fun onBindViewHolder(
        items: MutableList<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val item = items[position] as BottomTabListItem
        (holder as ViewHolder).bind(item.item, item.selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_tab, parent, false)
        )

    private inner class ViewHolder(val view: View) :
        RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<ItemBottomTabBinding>()


        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: DrawerMenuItem, selected: Boolean) {
            this.currentItem = item
            view.apply {
                contentDescription = context.getString(item.title)
                binding.itemBottomMenuIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        item.icon
                    )
                )

                val colorRes = if (selected) App.getColorFromAttr(
                    context,
                    androidx.appcompat.R.attr.colorAccent
                ) else App.getColorFromAttr(context, R.attr.icon_base)
                binding.itemBottomMenuIcon.setColorFilter(
                    colorRes,
                    PorterDuff.Mode.SRC_ATOP
                )

                binding.itemBottomMenuCounter.visibility = if (item.appItem.count > 0) {
                    // This is done that way because of a bug in the support library related to autosizing when width/height=WRAP_CONTENT
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(
                        binding.itemBottomMenuCounter,
                        TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
                    )
                    binding.itemBottomMenuCounter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    binding.itemBottomMenuCounter.text = item.appItem.count.toString()
                    doOnLayout {
                        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                            binding.itemBottomMenuCounter,
                            3,
                            10,
                            1,
                            TypedValue.COMPLEX_UNIT_SP
                        )
                    }
                    View.VISIBLE
                } else View.GONE
            }
        }
    }

    interface Listener {
        fun onTabClick(menu: DrawerMenuItem)
    }
}
