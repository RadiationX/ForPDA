package forpdateam.ru.forpda.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.FragmentBaseListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by radiationx on 14.08.17.
 */

abstract class RecyclerFragment : TabFragment(R.layout.fragment_base_list), TabTopScroller {

    private val binding by tabBinding(FragmentBaseListBinding::bind)

    protected val refreshLayout: SwipeRefreshLayout
        get() = binding.swipeRefreshList
    protected val recyclerView: RecyclerView
        get() = binding.baseList

    private var listScrollY = 0
    private var appBarOffset = 0

    private lateinit var topScroller: RecyclerTopScroller

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentController.setMainRefresh(refreshLayout)
        setListsBackground()
        recyclerView.setHasFixedSize(true)
        refreshLayoutStyle(refreshLayout)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                listScrollY = recyclerView.computeVerticalScrollOffset()
                updateToolbarShadow()
            }
        })

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, i ->
            appBarOffset = i
            updateToolbarShadow()
        })

        topScroller = RecyclerTopScroller(recyclerView, appBarLayout)
    }

    override fun isShadowVisible(): Boolean {
        //Log.e("kololo", "isShadowVisible " + appBarOffset + " " + listScrollY + " -> " + (appBarOffset != 0 || listScrollY > 0));
        return appBarOffset != 0 || listScrollY > 0
    }

    protected fun listScrollTop() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(225)
            recyclerView.smoothScrollToPosition(0)
        }
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }
}
