package forpdateam.ru.forpda.ui.fragments.other

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
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

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
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
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        if (checkViewHolder(viewHolder) && checkViewHolder(target)) {
            listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }
        return false
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if(actionState==ItemTouchHelper.ACTION_STATE_DRAG){
            listener.onDragStart()
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        listener.onDragEnd()
    }

    private fun checkViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder is MenuItemDelegate.ViewHolder && MenuRepository.GROUP_MAIN.contains(viewHolder.getItem().appItem.id)
    }

    interface ItemTouchHelperListener {
        fun onDragStart()
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun onDragEnd()
    }
}
