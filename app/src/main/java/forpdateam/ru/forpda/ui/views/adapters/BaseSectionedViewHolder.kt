package forpdateam.ru.forpda.ui.views.adapters

import android.view.View
import com.afollestad.sectionedrecyclerview.SectionedViewHolder

/**
 * Created by radiationx on 14.09.17.
 */
open class BaseSectionedViewHolder<T>(itemView: View) : SectionedViewHolder(itemView) {
    open fun bind(item: T, section: Int, relativePosition: Int, absolutePosition: Int) {
    }

    fun bind(item: T, section: Int) {
    }

    open fun bind(item: T) {
    }

    open fun bind(section: Int) {
    }

    fun bind() {
    }
}
