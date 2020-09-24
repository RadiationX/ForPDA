package forpdateam.ru.forpda.ui.fragments.devdb.search

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.*
import android.widget.LinearLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.presentation.devdb.search.SearchDevicesPresenter
import forpdateam.ru.forpda.presentation.devdb.search.SearchDevicesView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesAdapter
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView

/**
 * Created by radiationx on 09.11.17.
 */

class DevDbSearchFragment : TabFragment(), SearchDevicesView, BaseAdapter.OnItemClickListener<Brand.DeviceItem> {
    private lateinit var adapter: DevicesAdapter
    private lateinit var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var recyclerView: AutoFitRecyclerView
    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem
    private val dialogMenu = DynamicDialogMenu<DevDbSearchFragment, Brand.DeviceItem>()

    @InjectPresenter
    lateinit var presenter: SearchDevicesPresenter

    @ProvidePresenter
    fun providePresenter(): SearchDevicesPresenter = SearchDevicesPresenter(
            App.get().Di().devDbRepository,
            App.get().Di().router,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = "Поиск устройств"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_brand)
        refreshLayout = findViewById(R.id.swipe_refresh_list) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        recyclerView = findViewById(R.id.base_list) as AutoFitRecyclerView
        contentController.setMainRefresh(refreshLayout)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCardsBackground()
        refreshLayoutStyle(refreshLayout)
        setScrollFlagsEnterAlways()
        refreshLayout.setOnRefreshListener { presenter.refresh() }

        val pauseOnScrollListener = PauseOnScrollListener(ImageLoader.getInstance(), true, true)
        recyclerView.addOnScrollListener(pauseOnScrollListener)

        adapter = DevicesAdapter()
        recyclerView.setColumnWidth(App.get().dpToPx(144, recyclerView.context))
        recyclerView.adapter = adapter
        try {
            val gridLayoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.GridLayoutManager
            recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(gridLayoutManager, App.px8))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        adapter.setItemClickListener(this)

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        searchView.queryHint = getString(R.string.search_keywords)

        val searchEditFrame = searchView.findViewById<View>(R.id.search_edit_frame) as LinearLayout
        val params = searchEditFrame.layoutParams as LinearLayout.LayoutParams
        params.leftMargin = 0

        val searchSrcText = searchView.findViewById<View>(R.id.search_src_text)
        searchSrcText.setPadding(0, searchSrcText.paddingTop, 0, searchSrcText.paddingBottom)

        searchMenuItem.expandActionView()

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

    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        toolbar.inflateMenu(R.menu.qms_contacts_menu)
        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setIconifiedByDefault(true)
    }

    override fun showData(data: Brand, query: String) {
        setTitle("Поиск $query")
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
            show(context, this@DevDbSearchFragment, item)
        }
        return false
    }

}
