package forpdateam.ru.forpda.ui.fragments.other

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.Log
import forpdateam.ru.forpda.model.interactors.other.MenuRepository

/**
 * Created by radiationx on 26.05.17.
 */

class OtherItemDragCallback(
        private val otherAdapter: OtherAdapter,
        private val listener: ItemTouchHelperListener
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
        Log.e("lplplp", "getMovementFlags")
        val dragFlags = if (checkViewHolder(viewHolder)) {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else {
            ItemTouchHelper.ACTION_STATE_IDLE
        }
        val swipeFlags = ItemTouchHelper.ACTION_STATE_IDLE
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
            recyclerView: androidx.recyclerview.widget.RecyclerView,
            viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
            target: androidx.recyclerview.widget.RecyclerView.ViewHolder
    ): Boolean {
        if (checkViewHolder(viewHolder) && checkViewHolder(target)) {
            listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }
        return false
    }


    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if(actionState==ItemTouchHelper.ACTION_STATE_DRAG){
            listener.onDragStart()
        }
    }

    override fun clearView(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        listener.onDragEnd()
    }

    private fun checkViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return viewHolder is MenuItemDelegate.ViewHolder && MenuRepository.GROUP_MAIN.contains(viewHolder.getItem().appItem.id)
    }

    interface ItemTouchHelperListener {
        fun onDragStart()
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun onDragEnd()
    }
}
