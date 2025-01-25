package forpdateam.ru.forpda.ui.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by radiationx on 14.09.17.
 */
abstract class BaseAdapter<E, VH : BaseViewHolder<*>> : RecyclerView.Adapter<VH>() {
    @JvmField
    protected var items: ArrayList<E> = ArrayList()

    fun setItems(items: ArrayList<E>) {
        clear()
        this.items = items
    }

    fun addAll(items: Collection<E>) {
        addAll(items, true)
    }

    open fun addAll(items: Collection<E>, clearList: Boolean) {
        if (clearList) clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(position: Int): E {
        return items[position]
    }

    protected fun inflateLayout(parent: ViewGroup, @LayoutRes id: Int): View {
        return LayoutInflater.from(parent.context).inflate(id, parent, false)
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)

        fun onItemLongClick(item: T): Boolean
    }
}
