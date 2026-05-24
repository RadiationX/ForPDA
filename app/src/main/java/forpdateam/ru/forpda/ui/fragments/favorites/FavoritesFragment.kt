package forpdateam.ru.forpda.ui.fragments.favorites

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.presentation.favorites.FavoritesPresenter
import forpdateam.ru.forpda.presentation.favorites.FavoritesView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper

/**
 * Created by radiationx on 22.09.16.
 */

class FavoritesFragment : RecyclerFragment(), FavoritesView {

    companion object {
        fun getSubNames(context: Context): Array<String> {
            return arrayOf(
                context.getString(R.string.fav_subscribe_none),
                context.getString(R.string.fav_subscribe_delayed),
                context.getString(R.string.fav_subscribe_immediate),
                context.getString(R.string.fav_subscribe_daily),
                context.getString(R.string.fav_subscribe_weekly),
                context.getString(R.string.fav_subscribe_pinned)
            )
        }

        fun newInstance() = FavoritesFragment()
    }

    private lateinit var dialogMenu: DynamicDialogMenu<FavoritesFragment, Favorite>
    private lateinit var adapter: FavoritesAdapter

    private lateinit var paginationHelper: PaginationHelper

    private lateinit var dialog: BottomSheetDialog
    private lateinit var sortingView: ViewGroup
    private lateinit var keySpinner: Spinner
    private lateinit var orderSpinner: Spinner
    private lateinit var sortApply: Button
    private lateinit var sortReset: Button

    private val paginationListener = object : PaginationHelper.PaginationListener {
        override fun onTabSelected(tab: TabLayout.Tab): Boolean {
            return refreshLayout.isRefreshing
        }

        override fun onSelectedPage(pageNumber: Int) {
            presenter.loadFavorites(pageNumber)
        }
    }

    private val adapterListener = object : OnItemClickListener<Favorite> {
        override fun onItemClick(item: Favorite) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: Favorite): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    private val presenter by quillMoxyPresenter<FavoritesPresenter>()

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_favorite)
    }

    private fun getPinText(b: Boolean): CharSequence {
        return getString(if (b) R.string.fav_unpin else R.string.fav_pin)
    }

    private fun getSubText(subTypeIndex: Int): CharSequence {
        return "${getString(R.string.fav_change_subscribe_type)} (${getSubNames(requireContext())[subTypeIndex]})"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortingView = View.inflate(requireContext(), R.layout.favorite_sorting, null) as ViewGroup
        keySpinner = sortingView.findViewById<View>(R.id.sorting_key) as Spinner
        orderSpinner = sortingView.findViewById<View>(R.id.sorting_order) as Spinner
        sortApply = sortingView.findViewById<View>(R.id.sorting_apply) as Button
        sortReset = sortingView.findViewById(R.id.sorting_reset)
        dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener { dialog1 ->
            (dialog1 as Dialog).window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        paginationHelper = PaginationHelper(requireActivity())
        paginationHelper.addInToolbar(toolbarLayout, configuration.isFitSystemWindow)
        contentController.setFirstLoad(false)

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.attachments)) { _, data ->
                presenter.openAttachments(data)
            }
            addItem(getString(R.string.open_theme_forum)) { _, data ->
                presenter.openForum(data)
            }
            addItem(getString(R.string.fav_change_subscribe_type)) { _, data ->
                presenter.showSubscribeDialog(data)
            }
            addItem(getPinText(false)) { _, data ->
                presenter.changeFav(
                    FavoritesApi.ACTION_EDIT_PIN_STATE,
                    if (data.isPin) "unpin" else "pin",
                    data.favId
                )
            }
            addItem(getString(R.string.delete)) { _, data ->
                presenter.changeFav(FavoritesApi.ACTION_DELETE, null, data.favId)
            }
        }


        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoritesAdapter(adapterListener)
        recyclerView.adapter = adapter

        paginationHelper.setListener(paginationListener)

        initSpinnerItems(
            keySpinner,
            arrayOf(getString(R.string.fav_sort_last_post), getString(R.string.fav_sort_title))
        )
        initSpinnerItems(
            orderSpinner,
            arrayOf(getString(R.string.sorting_asc), getString(R.string.sorting_desc))
        )
        sortApply.setOnClickListener {
            val key = when (keySpinner.selectedItemPosition) {
                0 -> Sorting.Key.LAST_POST
                1 -> Sorting.Key.TITLE
                else -> ""
            }
            val order = when (orderSpinner.selectedItemPosition) {
                0 -> Sorting.Order.ASC
                1 -> Sorting.Order.DESC
                else -> ""
            }
            presenter.updateSorting(key, order)
            dialog.dismiss()
        }
        sortReset.setOnClickListener {
            presenter.updateSorting("", "")
            dialog.dismiss()
        }

        presenter.refresh()
    }

    override fun isShadowVisible(): Boolean {
        return true
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.sorting_title)
            .setIcon(R.drawable.ic_toolbar_sort)
            .setOnMenuItemClickListener {
                hideKeyboard()
                if (sortingView.parent != null && sortingView.parent is ViewGroup) {
                    (sortingView.parent as ViewGroup).removeView(sortingView)
                }
                dialog.setContentView(sortingView)
                dialog.show()
                false
            }
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onMarkAllRead() {
        Toast.makeText(requireContext(), R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onLoadFavorites(data: FavoritesData) {
        Log.e("kjkjkj", "onLoadFavorites")
        selectSpinners(data.sorting)
        paginationHelper.updatePagination(data.pagination)
        setSubtitle(paginationHelper.title)
    }

    override fun setShowDot(enabled: Boolean) {
        adapter.setShowDot(enabled)
    }

    override fun setUnreadTop(unreadTop: Boolean) {
        adapter.setUnreadTop(unreadTop)
    }

    override fun onShowFavorite(items: List<Favorite>) {
        Log.e("kjkjkj", "onShowFavorite")
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(requireContext())
                    .setImage(R.drawable.ic_star)
                    .setTitle(R.string.funny_favorites_nodata_title)
                    .setDesc(R.string.funny_favorites_nodata_desc)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        adapter.bindItems(items)
    }

    override fun initSorting(sorting: Sorting) {
        selectSpinners(sorting)
    }

    private fun selectSpinners(sorting: Sorting) {
        when (sorting.key) {
            Sorting.Key.LAST_POST -> keySpinner.setSelection(0)
            Sorting.Key.TITLE -> keySpinner.setSelection(1)
        }
        when (sorting.order) {
            Sorting.Order.ASC -> orderSpinner.setSelection(0)
            Sorting.Order.DESC -> orderSpinner.setSelection(1)
        }
    }

    private fun initSpinnerItems(spinner: Spinner, items: Array<String>) {
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        paginationHelper.destroy()
    }

    override fun onChangeFav(result: Boolean) {
        Toast.makeText(requireContext(), R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun showSubscribeDialog(item: Favorite) {
        val subTypeIndex = FavoritesApi.SUB_TYPES.indexOf(item.trackType)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.favorites_subscribe_email)
            .setSingleChoiceItems(getSubNames(requireContext()), subTypeIndex) { dialog, which ->
                presenter.changeFav(
                    FavoritesApi.ACTION_EDIT_SUB_TYPE,
                    FavoritesApi.SUB_TYPES[which],
                    item.favId
                )
                dialog.dismiss()
            }
            .show()
    }

    override fun showItemDialogMenu(item: Favorite) {
        dialogMenu.apply {
            disallowAll()
            allow(0)
            if (item is Favorite.Topic) {
                allow(1)
                allow(2)
            }
            allow(3)
            allow(4)
            allow(5)

            val index = containsIndex(getPinText(!item.isPin))
            if (index != -1)
                changeTitle(index, getPinText(item.isPin))

            val subTypeIndex = FavoritesApi.SUB_TYPES.indexOf(item.trackType)
            changeTitle(3, getSubText(subTypeIndex))

            show(requireContext(), this@FavoritesFragment, item)
        }
    }
}
