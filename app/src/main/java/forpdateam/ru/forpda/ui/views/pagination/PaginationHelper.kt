package forpdateam.ru.forpda.ui.views.pagination

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getToolBarHeight
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.ui.DimensionHelper.Dimensions
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by radiationx on 03.03.17.
 */
class PaginationHelper(context: Activity) {
    private val context: Context = context
    private var tabLayoutInToolbar: TabLayout? = null

    private val dimensionsProvider = get().Di().dimensionsProvider
    private val disposables = CompositeDisposable()

    var currentPage: Int = 0
        private set

    private val tabLayouts = ArrayList<TabLayout>()
    private var pagination: Pagination? = null
    private var listener: PaginationListener? = null
    private val tabSelectedListener: OnTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            if (listener!!.onTabSelected(tab) || tab.tag == null) return
            when (tab.tag as Int?) {
                TAG_FIRST -> firstPage()
                TAG_PREV -> prevPage()
                TAG_NEXT -> nextPage()
                TAG_LAST -> lastPage()
                TAG_SELECT -> selectPageDialog()
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            onTabSelected(tab)
        }
    }

    private fun updateDimens(dimensions: Dimensions) {
        if (tabLayoutInToolbar != null) {
            val params = tabLayoutInToolbar!!.layoutParams as CollapsingToolbarLayout.LayoutParams
            params.topMargin = getToolBarHeight(tabLayoutInToolbar!!.context) + dimensions.statusBar
            tabLayoutInToolbar!!.layoutParams = params
        }
    }

    fun setPagination(pagination: Pagination?) {
        this.pagination = pagination
    }

    fun addInToolbar(
        inflater: LayoutInflater,
        target: CollapsingToolbarLayout,
        enablePadding: Boolean
    ) {
        val tabLayout = inflater.inflate(R.layout.pagination_toolbar, target, false) as TabLayout
        target.addView(tabLayout, target.indexOfChild(target.findViewById(R.id.toolbar)))
        tabLayoutInToolbar = tabLayout
        if (enablePadding) {
            disposables.add(
                dimensionsProvider
                    .observeDimensions()
                    .subscribe { dimensions: Dimensions ->
                        if (tabLayoutInToolbar != null) {
                            tabLayoutInToolbar!!.post {
                                if (tabLayoutInToolbar != null) {
                                    updateDimens(dimensions)
                                }
                            }
                        }
                        updateDimens(dimensions)
                    }
            )
        }

        val params = target.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        target.layoutParams = params
        target.scrimVisibleHeightTrigger = App.px56 + App.px24
        setupTabLayout(tabLayout, true)
        tabLayouts.add(tabLayout)
        target.requestLayout()
    }

    fun addInList(inflater: LayoutInflater, target: ViewGroup) {
        val tabLayout = inflater.inflate(R.layout.pagination_list, target, false) as TabLayout
        target.addView(tabLayout)
        setupTabLayout(tabLayout, false)
        tabLayouts.add(tabLayout)
        target.requestLayout()
    }

    private fun setupTabLayout(tabLayout: TabLayout, firstLast: Boolean) {
        if (firstLast) {
            tabLayout.addTab(
                tabLayout.newTab()
                    .setIcon(R.drawable.ic_toolbar_chevron_double_left)
                    .setTag(TAG_FIRST)
                    .setContentDescription(R.string.pagination_first)
            )
        }

        tabLayout.addTab(
            tabLayout.newTab()
                .setIcon(R.drawable.ic_toolbar_chevron_left)
                .setTag(TAG_PREV)
                .setContentDescription(R.string.pagination_prev)
        )

        tabLayout.addTab(
            tabLayout.newTab()
                .setText(R.string.pagination_select)
                .setTag(TAG_SELECT)
                .setContentDescription(R.string.pagination_select_desc)
        )

        tabLayout.addTab(
            tabLayout.newTab()
                .setIcon(R.drawable.ic_toolbar_chevron_right)
                .setTag(TAG_NEXT)
                .setContentDescription(R.string.pagination_next)
        )

        if (firstLast) {
            tabLayout.addTab(
                tabLayout.newTab()
                    .setIcon(R.drawable.ic_toolbar_chevron_double_right)
                    .setTag(TAG_LAST)
                    .setContentDescription(R.string.pagination_last)
            )
        }

        tabLayout.addOnTabSelectedListener(tabSelectedListener)
    }

    private fun selectPage(pageNumber: Int) {
        currentPage = pageNumber
        if (listener != null) {
            listener!!.onSelectedPage(pageNumber)
        }
    }

    fun firstPage() {
        if (pagination!!.current <= 1) return
        selectPage(if (pagination!!.isForum) 0 else 1)
    }

    fun prevPage() {
        if (pagination!!.current <= 1) return
        selectPage(pagination!!.getPage(pagination!!.current - (if (pagination!!.isForum) 2 else 1)))
    }

    fun nextPage() {
        if (pagination!!.current == pagination!!.all) return
        selectPage(pagination!!.getPage(pagination!!.current + (if (pagination!!.isForum) 0 else 1)))
    }

    fun lastPage() {
        if (pagination!!.current == pagination!!.all) return
        selectPage(pagination!!.getPage(pagination!!.all - (if (pagination!!.isForum) 1 else 0)))
    }

    fun updatePagination(newPagination: Pagination?) {
        this.pagination = newPagination
        for (tabLayout in tabLayouts) {
            if (pagination!!.all <= 1) {
                tabLayout.visibility = View.GONE
                return
            }
            tabLayout.visibility = View.VISIBLE
            val prevDisabled = pagination!!.current <= 1
            val nextDisabled = pagination!!.current == pagination!!.all
            var tab: TabLayout.Tab?
            var tag: Int
            for (i in 0 until tabLayout.tabCount) {
                tab = tabLayout.getTabAt(i)
                if (tab == null || tab.tag == null) return
                tag = (tab.tag as Int?)!!
                if ((tag) == TAG_SELECT) continue
                if (tab.icon != null) {
                    if (if ((tag == TAG_FIRST || tag == TAG_PREV)) prevDisabled else nextDisabled) tab.icon!!.colorFilter =
                        colorFilter
                    else tab.icon!!.clearColorFilter()
                }
            }
        }
    }

    val title: String?
        get() = if (pagination == null || pagination!!.all <= 1) null else (pagination!!.current.toString() + "/" + pagination!!.all.toString())

    fun selectPageDialog() {
        val pages = IntArray(pagination!!.all)

        for (i in 0 until pagination!!.all) pages[i] = i + 1

        val listView = ListView(context)
        listView.divider = null
        listView.dividerHeight = 0
        listView.isFastScrollEnabled = true

        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.adapter = PaginationAdapter(context, pages)
        listView.setItemChecked(pagination!!.current - 1, true)
        listView.setSelection(pagination!!.current - 1)

        val dialog = AlertDialog.Builder(context)
            .setView(listView)
            .show()

        if (dialog.window != null) dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view1: View?, i2: Int, l: Long ->
                if (listView.tag != null && !(listView.tag as Boolean)) {
                    return@OnItemClickListener
                }
                selectPage(i2 * pagination!!.perPage)
                dialog.cancel()
            }
    }

    fun setListener(listener: PaginationListener?) {
        this.listener = listener
    }

    fun destroy() {
        disposables.dispose()
    }

    interface PaginationListener {
        fun onTabSelected(tab: TabLayout.Tab): Boolean

        fun onSelectedPage(pageNumber: Int)
    }

    companion object {
        private const val TAG_FIRST = 0
        private const val TAG_PREV = 1
        private const val TAG_SELECT = 2
        private const val TAG_NEXT = 3
        private const val TAG_LAST = 4
        private val colorFilter: ColorFilter =
            PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN)
    }
}
