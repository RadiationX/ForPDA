package forpdateam.ru.forpda.ui.fragments

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.util.*

class RecyclerTopScroller(
        private val recyclerView: RecyclerView,
        private val appBarLayout: AppBarLayout
) : TabTopScroller {

    private var lastItemOffset = 0
    private var lastItemPosition = 0
    private var scrolledToTop = false

    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val listScrollY = recyclerView.computeVerticalScrollOffset()
                if (scrolledToTop && listScrollY > 0) {
                    lastItemOffset = 0
                    lastItemPosition = 0
                    scrolledToTop = false
                }
            }
        })
    }

    override fun toggleScrollTop() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        if (lastItemOffset > 0 || lastItemPosition > 0) {
            //appBarLayout.setExpanded(false, true);
            val position = lastItemPosition
            val offset = lastItemOffset
            lastItemOffset = 0
            lastItemPosition = 0
            scrolledToTop = false
            layoutManager.scrollToPositionWithOffset(position, offset)
        } else {
            appBarLayout.setExpanded(true, true)
            recyclerView.scrollToPosition(0)
            val position = layoutManager.findFirstVisibleItemPosition()
            val itemView = layoutManager.findViewByPosition(position)
            val offset = itemView?.top ?: 0
            lastItemPosition = position
            lastItemOffset = offset
            scrolledToTop = true
        }
    }
}