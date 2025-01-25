package forpdateam.ru.forpda.ui.fragments.qms.adapters

import android.graphics.Typeface
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsThemesAdapter.ThemeHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 25.08.16.
 */
class QmsThemesAdapter : BaseAdapter<QmsTheme, ThemeHolder>() {
    private var itemClickListener: OnItemClickListener<QmsTheme>? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener<QmsTheme>?) {
        this.itemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeHolder {
        val v = inflateLayout(parent, R.layout.qms_theme_item)
        return ThemeHolder(v)
    }

    override fun onBindViewHolder(holder: ThemeHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ThemeHolder(v: View) : BaseViewHolder<QmsTheme>(v), View.OnClickListener,
        OnLongClickListener {
        var name: TextView = v.findViewById(R.id.qms_theme_name)
        var count: TextView = v.findViewById(R.id.qms_theme_count)

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: QmsTheme, position: Int) {
            name.text = item.name
            name.typeface = if (item.countNew > 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (item.countNew == 0) {
                count.visibility = View.GONE
            } else {
                count.text = item.countNew.toString()
                count.visibility = View.VISIBLE
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClick(getItem(layoutPosition))
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (itemClickListener != null) {
                itemClickListener!!.onItemLongClick(getItem(layoutPosition))
                return true
            }
            return false
        }
    }
}
