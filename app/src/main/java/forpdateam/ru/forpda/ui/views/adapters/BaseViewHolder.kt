package forpdateam.ru.forpda.ui.views.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by radiationx on 14.09.17.
 */
open class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(item: T, section: Int) {
    }

    open fun bind(item: T) {
    }

    open fun bind(position: Int) {
    }

    fun bind() {
    }
}
