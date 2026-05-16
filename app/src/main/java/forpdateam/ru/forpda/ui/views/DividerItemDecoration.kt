package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import forpdateam.ru.forpda.extensions.getDrawableAttr

/**
 * Created by radiationx on 22.09.16.
 */
class DividerItemDecoration : ItemDecoration {
    private val mDivider: Drawable?

    /**
     * Default divider will be used
     */
    constructor(context: Context) {
        mDivider = context.getDrawableAttr(android.R.attr.listDivider)
    }

    /**
     * Custom divider will be used
     */
    constructor(context: Context, resId: Int) {
        mDivider = context.getDrawable(resId)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight

            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
}
