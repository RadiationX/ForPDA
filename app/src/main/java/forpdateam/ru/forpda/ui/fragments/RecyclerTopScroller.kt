package forpdateam.ru.forpda.ui.fragments

import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class RecyclerTopScroller(
        private val recyclerView: androidx.recyclerview.widget.RecyclerView,
        private val appBarLayout: AppBarLayout
) : TabTopScroller {

    private var lastItemOffset = 0
    private var lastItemPosition = 0
    private var scrolledToTop = false

    init {
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
        val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
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