package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Created by radiationx on 05.10.17.
 */
/*
 * Для управления и дополнительными вьюхами, когда нет данных и т.д.
 * */
class ContentController(
    private var additionalRefresh: View?,
    private val additionalContent: ViewGroup,
    private var mainContent: ViewGroup?
) {
    private var mainRefresh: View? = null
    private var firstLoad = true

    private val contents = HashMap<Any, View?>()


    fun setMainRefresh(mainRefresh: View?) {
        this.mainRefresh = mainRefresh
    }

    fun contains(tag: Any): Boolean {
        return contents[tag] != null
    }

    fun addContent(content: View?, tag: Any): View {
        var view = contents[tag]
        if (view == null) {
            view = content
            view!!.visibility = View.GONE
            contents[tag] = view
            additionalContent.addView(view, 0)
        }
        return view
    }

    fun addContent(context: Context?, @LayoutRes id: Int, tag: Any): View {
        var view = contents[tag]
        if (view == null) {
            view = View.inflate(context, id, null)
            view.setVisibility(View.GONE)
            contents[tag] = view
            additionalContent.addView(view, 0)
        }
        return view!!
    }

    fun showContent(tag: Any) {
        val view = contents[tag]
        if (view != null) {
            view.visibility = View.VISIBLE
            //mainContent.setVisibility(View.GONE);
        }
    }

    fun hideContent(tag: Any) {
        val view = contents[tag]
        if (view != null) {
            view.visibility = View.GONE
            //mainContent.setVisibility(View.VISIBLE);
        }
    }

    fun startRefreshing() {
        if (firstLoad) {
            mainContent!!.visibility = View.INVISIBLE
            additionalRefresh!!.visibility = View.VISIBLE
        } else if (mainRefresh != null) {
            if (mainRefresh is SwipeRefreshLayout) {
                (mainRefresh as SwipeRefreshLayout).isRefreshing = true
            }
        }
    }

    fun stopRefreshing() {
        if (firstLoad) {
            mainContent!!.visibility = View.VISIBLE
            additionalRefresh!!.visibility = View.GONE
            firstLoad = false
        } else if (mainRefresh != null) {
            if (mainRefresh is SwipeRefreshLayout) {
                (mainRefresh as SwipeRefreshLayout).isRefreshing = false
            }
        }
    }

    fun setFirstLoad(b: Boolean) {
        firstLoad = b
    }

    fun destroy() {
        additionalRefresh = null
        mainContent = null
        mainRefresh = null
        contents.clear()
    }

    companion object {
        const val TAG_NO_DATA: String = "NO_DATA"
    }
}
