package forpdateam.ru.forpda.ui.fragments.reputation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.presentation.reputation.ReputationPresenter
import forpdateam.ru.forpda.presentation.reputation.ReputationView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper

/**
 * Created by radiationx on 20.03.17.
 */

class ReputationFragment : RecyclerFragment(), ReputationView {

    private lateinit var adapter: ReputationAdapter
    private lateinit var paginationHelper: PaginationHelper
    private lateinit var dialogMenu: DynamicDialogMenu<ReputationFragment, RepItem>

    private lateinit var descSortMenuItem: MenuItem
    private lateinit var ascSortMenuItem: MenuItem
    private lateinit var repModeMenuItem: MenuItem
    private lateinit var upRepMenuItem: MenuItem
    private lateinit var downRepMenuItem: MenuItem

    private val authHolder = App.get().Di().authHolder

    private val paginationListener = object : PaginationHelper.PaginationListener {
        override fun onTabSelected(tab: TabLayout.Tab): Boolean {
            return refreshLayout.isRefreshing
        }

        override fun onSelectedPage(pageNumber: Int) {
            presenter.selectPage(pageNumber)
        }
    }

    private val adapterListener = object : BaseAdapter.OnItemClickListener<RepItem> {
        override fun onItemClick(item: RepItem) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: RepItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    @InjectPresenter
    lateinit var presenter: ReputationPresenter

    @ProvidePresenter
    fun providePresenter(): ReputationPresenter = ReputationPresenter(
            App.get().Di().reputationRepository,
            App.get().Di().avatarRepository,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_reputation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getString(TabFragment.ARG_TAB)?.also {
                presenter.currentData = ReputationApi.fromUrl(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        paginationHelper = PaginationHelper(activity)
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScrollFlagsEnterAlways()

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.profile)) { _, data ->
                presenter.navigateToProfile(data.userId)
            }
            addItem(getString(R.string.go_to_message)) { _, data ->
                presenter.navigateToMessage(data)
            }
        }

        refreshLayout.setOnRefreshListener { presenter.loadReputation() }
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ReputationAdapter()
        recyclerView.adapter = adapter
        paginationHelper.setListener(paginationListener)
        adapter.setOnItemClickListener(adapterListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        paginationHelper.destroy()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        val subMenu = menu.addSubMenu(R.string.sorting_title)
        subMenu.item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        subMenu.item.icon = App.getVecDrawable(context, R.drawable.ic_toolbar_sort)
        descSortMenuItem = subMenu.add(R.string.sorting_desc).setOnMenuItemClickListener {
            presenter.setSort(ReputationApi.SORT_DESC)
            false
        }
        ascSortMenuItem = subMenu.add(R.string.sorting_asc).setOnMenuItemClickListener {
            presenter.setSort(ReputationApi.SORT_ASC)
            false
        }
        repModeMenuItem = menu.add(getString(if (presenter.currentData.mode == ReputationApi.MODE_FROM) R.string.reputation_mode_from else R.string.reputation_mode_to))
                .setOnMenuItemClickListener {
                    presenter.changeReputationMode()
                    false
                }
        upRepMenuItem = menu.add(R.string.increase)
                .setOnMenuItemClickListener {
                    showChangeReputationDialog(true)
                    false
                }
        downRepMenuItem = menu.add(R.string.decrease)
                .setOnMenuItemClickListener {
                    showChangeReputationDialog(false)
                    false
                }
        refreshToolbarMenuItems(false)
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            descSortMenuItem.isEnabled = true
            ascSortMenuItem.isEnabled = true
            repModeMenuItem.isEnabled = true
            repModeMenuItem.title = getString(if (presenter.currentData.mode == ReputationApi.MODE_FROM) R.string.reputation_mode_from else R.string.reputation_mode_to)
            if (presenter.currentData.id != authHolder.get().userId) {
                upRepMenuItem.isEnabled = true
                upRepMenuItem.isVisible = true
                downRepMenuItem.isEnabled = true
                downRepMenuItem.isVisible = true
            }
        } else {
            descSortMenuItem.isEnabled = false
            ascSortMenuItem.isEnabled = false
            repModeMenuItem.isEnabled = false
            upRepMenuItem.isEnabled = false
            upRepMenuItem.isEnabled = false
            upRepMenuItem.isVisible = false
            downRepMenuItem.isVisible = false
        }
    }

    @SuppressLint("InflateParams")
    fun showChangeReputationDialog(type: Boolean) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val layout = inflater.inflate(R.layout.reputation_change_layout, null)

        val text = layout.findViewById<View>(R.id.reputation_text) as TextView
        val messageField = layout.findViewById<View>(R.id.reputation_text_field) as EditText
        text.text = String.format(getString(R.string.change_reputation_Type_Nick), getString(if (type) R.string.increase else R.string.decrease), presenter.currentData.nick)

        AlertDialog.Builder(context!!)
                .setView(layout)
                .setPositiveButton(R.string.ok) { _, _ ->
                    presenter.changeReputation(type, messageField.text.toString())
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun onChangeReputation(result: Boolean) {
        Toast.makeText(context, getString(R.string.reputation_changed), Toast.LENGTH_SHORT).show()
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        super.setRefreshing(isRefreshing)
        refreshToolbarMenuItems(!isRefreshing)
    }

    override fun showAvatar(avatarUrl: String) {
        ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView)
        toolbarImageView.visibility = View.VISIBLE
        toolbarImageView.contentDescription = getString(R.string.user_avatar)
    }

    override fun showReputation(repData: RepData) {
        if (repData.items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(context)
                        .setImage(R.drawable.ic_history)
                        .setTitle(R.string.funny_reputation_nodata_title)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }

        adapter.addAll(repData.items)
        paginationHelper.updatePagination(repData.pagination)
        refreshToolbarMenuItems(true)
        setSubtitle("${repData.positive - repData.negative} (+${repData.positive} / -${repData.negative})")
        setTabTitle("Репутация ${repData.nick}${if (repData.mode == ReputationApi.MODE_FROM) ": кому изменял" else ""}")
        setTitle("Репутация ${repData.nick}${if (repData.mode == ReputationApi.MODE_FROM) ": кому изменял" else ""}")
        listScrollTop()
        toolbarImageView.setOnClickListener { presenter.navigateToProfile(repData.id) }
    }

    override fun showItemDialogMenu(item: RepItem) {
        dialogMenu.disallowAll()
        dialogMenu.allow(0)
        if (item.sourceUrl != null)
            dialogMenu.allow(1)
        dialogMenu.show(context, item.userNick, this@ReputationFragment, item)
    }
}
