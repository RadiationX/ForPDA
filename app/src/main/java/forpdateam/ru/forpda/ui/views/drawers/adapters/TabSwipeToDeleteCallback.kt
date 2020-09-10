package forpdateam.ru.forpda.ui.views.drawers.adapters;

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import forpdateam.ru.forpda.R
import kotlin.math.abs

abstract class TabSwipeToDeleteCallback(
        color: Int
) : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    val background = ColorDrawable(color)

    override fun onMove(p0: androidx.recyclerview.widget.RecyclerView, p1: androidx.recyclerview.widget.RecyclerView.ViewHolder, p2: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder.itemView
        val width = itemView.right - itemView.left
        //val target: View = itemView.findViewById(R.id.drawer_item_wrapper) ?: itemView
        //target.translationX = dX

        //background.color = backgroundColor
        if (abs(dX) > (width / 2)){
            background.alpha = 255
        }else{
            background.alpha = 127
        }
        val left = if (dX > 0) itemView.left else if (dX == 0f) 0 else itemView.right + dX.toInt()
        val right = if (dX < 0) itemView.right else if (dX == 0f) 0 else itemView.left + dX.toInt()
        background.setBounds(
                left,
                itemView.top,
                right,
                itemView.bottom
        )
        background.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
