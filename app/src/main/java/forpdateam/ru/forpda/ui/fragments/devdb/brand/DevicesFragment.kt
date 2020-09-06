package forpdateam.ru.forpda.ui.fragments.devdb.brand

import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.presentation.devdb.devices.DevicesPresenter
import forpdateam.ru.forpda.presentation.devdb.devices.DevicesView
import forpdateam.ru.forpda.ui.fragments.RecyclerTopScroller
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView

/**
 * Created by radiationx on 08.08.17.
 */

class DevicesFragment : TabFragment(), DevicesView, BaseAdapter.OnItemClickListener<Brand.DeviceItem>, TabTopScroller {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: AutoFitRecyclerView
    private lateinit var adapter: DevicesAdapter
    private val dialogMenu = DynamicDialogMenu<DevicesFragment, Brand.DeviceItem>()

    private var listScrollY = 0
    private var appBarOffset = 0

    private lateinit var topScroller: RecyclerTopScroller


    @InjectPresenter
    lateinit var presenter: DevicesPresenter

    @ProvidePresenter
    fun providePresenter(): DevicesPresenter = DevicesPresenter(
            App.get().Di().devDbRepository,
            App.get().Di().router,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_brand)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.categoryId = getString(ARG_CATEGORY_ID, null)
            presenter.brandId = getString(ARG_BRAND_ID, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_brand)
        refreshLayout = findViewById(R.id.swipe_refresh_list) as SwipeRefreshLayout
        recyclerView = findViewById(R.id.base_list) as AutoFitRecyclerView
        contentController.setMainRefresh(refreshLayout)
        setScrollFlagsEnterAlways()
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCardsBackground()
        refreshLayoutStyle(refreshLayout)
        refreshLayout.setOnRefreshListener { presenter.loadBrand() }

        val pauseOnScrollListener = PauseOnScrollListener(ImageLoader.getInstance(), true, true)
        recyclerView.addOnScrollListener(pauseOnScrollListener)

        adapter = DevicesAdapter()
        adapter.setItemClickListener(this)
        recyclerView.setColumnWidth(App.get().dpToPx(144, recyclerView.context))
        recyclerView.adapter = adapter
        try {
            val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
            recyclerView.addItemDecoration(SpacingItemDecoration(gridLayoutManager, App.px8))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        dialogMenu.apply {
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.share)) { _, data ->
                presenter.shareLink(data)
            }
            addItem(getString(R.string.create_note)) { _, data ->
                presenter.createNote(data)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                listScrollY = recyclerView.computeVerticalScrollOffset()
                updateToolbarShadow()
            }
        })

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            appBarOffset = offset
            updateToolbarShadow()
        })

        topScroller = RecyclerTopScroller(recyclerView, appBarLayout)
    }


    override fun isShadowVisible(): Boolean {
        return true || appBarOffset != 0 || listScrollY > 0
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.fragment_title_device_search)
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    presenter.openSearch()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun showData(data: Brand) {
        setTitle(data.title)
        setTabTitle("${data.catTitle} ${data.title}")
        setSubtitle(data.catTitle)
        adapter.addAll(data.devices)
    }

    override fun showCreateNote(title: String, url: String) {
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun onItemClick(item: Brand.DeviceItem) {
        presenter.openDevice(item)
    }

    override fun onItemLongClick(item: Brand.DeviceItem): Boolean {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(context, this@DevicesFragment, item)
        }
        return false
    }

    class SpacingItemDecoration : RecyclerView.ItemDecoration {
        private var spanCount = 1
        private var fullWidth = false
        private val includeEdge = true
        private var spacing: Int = 0
        private var manager: GridLayoutManager? = null

        constructor(manager: GridLayoutManager, spacing: Int) {
            this.spacing = spacing
            this.manager = manager
        }

        constructor(spacing: Int) {
            this.spacing = spacing
        }

        constructor(spacing: Int, fullWidth: Boolean) {
            this.spacing = spacing
            this.fullWidth = fullWidth
        }


        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            manager?.also {
                spanCount = it.spanCount
            }

            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                if (!fullWidth) {
                    outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
                }
                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                if (!fullWidth) {
                    outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                }
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }

    companion object {
        const val ARG_CATEGORY_ID = "CATEGORY_ID"
        const val ARG_BRAND_ID = "BRAND_ID"
    }
}
