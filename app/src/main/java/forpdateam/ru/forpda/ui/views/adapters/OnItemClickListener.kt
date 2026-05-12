package forpdateam.ru.forpda.ui.views.adapters

import android.view.View

interface OnItemClickListener<in T> {
    fun onItemClick(item: T)

    fun onItemLongClick(item: T): Boolean

    fun attachTo(view: View, item: T) {
        view.setOnClickListener { onItemClick(item) }
        view.setOnLongClickListener { onItemLongClick(item) }
    }
}