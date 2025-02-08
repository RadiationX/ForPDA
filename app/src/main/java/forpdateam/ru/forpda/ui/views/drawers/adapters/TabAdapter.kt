package forpdateam.ru.forpda.ui.views.drawers.adapters

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DrawerTabItemBinding
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabAdapter.TabHolder

/**
 * Created by radiationx on 02.05.17.
 */
class TabAdapter : BaseAdapter<TabFragment, TabHolder>() {
    private val color = Color.argb(24, 128, 128, 128)

    private var itemClickListener: OnItemClickListener<TabFragment>? = null
    private var closeClickListener: OnItemClickListener<TabFragment>? = null

    private var currentFragmentTag: String? = null

    fun setItemClickListener(itemClickListener: OnItemClickListener<TabFragment>?) {
        this.itemClickListener = itemClickListener
    }

    fun setCloseClickListener(closeClickListener: OnItemClickListener<TabFragment>?) {
        this.closeClickListener = closeClickListener
    }

    fun setCurrentFragmentTag(tag: String?) {
        currentFragmentTag = tag
    }

    fun removeAt(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
        val v = inflateLayout(parent, R.layout.drawer_tab_item)
        return TabHolder(v)
    }

    override fun onBindViewHolder(holder: TabHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class TabHolder(v: View) : BaseViewHolder<TabFragment>(v), View.OnClickListener {

        private val binding by viewBinding<DrawerTabItemBinding>()
        private var currentItem: TabFragment? = null

        init {
            v.setOnClickListener(this)
            binding.drawerItemClose.setOnClickListener { v1: View? ->
                if (closeClickListener != null) {
                    closeClickListener!!.onItemClick(requireNotNull(currentItem))
                }
            }
        }

        override fun bind(item: TabFragment, position: Int) {
            currentItem = item
            val isActive = item.tag != null && item.tag == currentFragmentTag
            Log.d("lalala", "TabAdapter bind $item : $isActive : $position")

            if (isActive) binding.drawerItemWrapper.setBackgroundColor(color)
            else binding.drawerItemWrapper.setBackgroundColor(Color.TRANSPARENT)

            binding.drawerItemTitle.text = item.getTabTitle()
        }

        override fun onClick(view: View) {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClick(requireNotNull(currentItem))
            }
        }
    }
}