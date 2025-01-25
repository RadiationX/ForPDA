package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Created by radiationx on 27.10.16.
 */
class DynamicDialogMenu<T, E> {
    private val allItems: MutableList<MenuItem> = ArrayList<MenuItem>()
    private val allowedItems: MutableList<MenuItem> = ArrayList<MenuItem>()

    fun addItem(title: CharSequence, listener: OnClickListener<T, E>?): MenuItem {
        val item: MenuItem = MenuItem(title, listener)
        allItems.add(item)
        return item
    }

    fun addItem(title: CharSequence): MenuItem {
        val item: MenuItem = MenuItem(title)
        allItems.add(item)
        return item
    }

    fun allow(index: Int) {
        allow(get(index))
    }

    fun allow(item: MenuItem) {
        allowedItems.add(item)
    }

    fun allowAll() {
        allowedItems.addAll(allItems)
    }

    fun disallowAll() {
        allowedItems.clear()
    }

    fun getAllItems(): List<MenuItem> {
        return allItems
    }

    fun get(index: Int): MenuItem {
        return allItems[index]
    }

    fun containsIndex(title: CharSequence): Int {
        for (i in allItems.indices) if (allItems[i].title == title) return i
        return -1
    }

    fun changeTitle(i: Int, title: CharSequence) {
        allItems[i].setTitle(title)
    }

    val titles: Array<CharSequence?>
        get() {
            val result =
                arrayOfNulls<CharSequence>(allowedItems.size)
            for (i in allowedItems.indices) result[i] = allowedItems[i].title
            return result
        }

    fun show(uiContext: Context, context: T, data: E) {
        show(uiContext, null, context, data)
    }

    fun show(uiContext: Context, title: String?, context: T, data: E) {
        val builder = AlertDialog.Builder(uiContext)
        if (title != null) {
            builder.setTitle(title)
        }
        builder.setItems(
            titles
        ) { dialog: DialogInterface?, which: Int -> onClick(which, context, data) }
        builder.show()
    }

    fun onClick(i: Int, context: T, data: E) {
        allowedItems[i].onClick(context, data)
    }

    inner class MenuItem : OnClickListener<T, E> {
        var listener: OnClickListener<T, E>? = null
        var title: CharSequence

        constructor(title: CharSequence, listener: OnClickListener<T, E>?) {
            this.title = title
            this.listener = listener
        }

        constructor(title: CharSequence) {
            this.title = title
        }

        fun setTitle(title: CharSequence): MenuItem {
            this.title = title
            return this
        }

        fun setListener(listener: OnClickListener<T, E>?): MenuItem {
            this.listener = listener
            return this
        }

        override fun onClick(context: T, data: E) {
            if (listener != null) listener!!.onClick(context, data)
        }
    }

    fun interface OnClickListener<T, E> {
        fun onClick(context: T, data: E)
    }
}
