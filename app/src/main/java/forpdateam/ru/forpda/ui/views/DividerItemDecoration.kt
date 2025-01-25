package forpdateam.ru.forpda.ui.views

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * Created by radiationx on 22.09.16.
 */
class DividerItemDecoration : ItemDecoration {
    private val mDivider: Drawable?

    /**
     * Default divider will be used
     */
    constructor(context: Context) {
        val styledAttributes = context.obtainStyledAttributes(ATTRS)
        mDivider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    /**
     * Custom divider will be used
     */
    constructor(context: Context, resId: Int) {
        mDivider = ContextCompat.getDrawable(context, resId)
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

    companion object {
        private val ATTRS = intArrayOf(R.attr.listDivider)
    }
}
