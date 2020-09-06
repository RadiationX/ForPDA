package forpdateam.ru.forpda.ui.fragments.topics

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.topics.TopicsPresenter
import forpdateam.ru.forpda.presentation.topics.TopicsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedAdapter
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper

/**
 * Created by radiationx on 01.03.17.
 */

class TopicsFragment : RecyclerFragment(), TopicsView {

    private lateinit var adapter: TopicsAdapter
    private lateinit var paginationHelper: PaginationHelper
    private lateinit var dialogMenu: DynamicDialogMenu<TopicsFragment, TopicItem>
    private val authHolder = App.get().Di().authHolder


    private val paginationListener = object : PaginationHelper.PaginationListener {
        override fun onTabSelected(tab: TabLayout.Tab): Boolean {
            return refreshLayout.isRefreshing
        }

        override fun onSelectedPage(pageNumber: Int) {
            presenter.loadPage(pageNumber)
        }
    }

    private val adapterListener = object : BaseSectionedAdapter.OnItemClickListener<TopicItem> {
        override fun onItemClick(item: TopicItem) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: TopicItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    @InjectPresenter
    lateinit var presenter: TopicsPresenter

    @ProvidePresenter
    fun providePresenter(): TopicsPresenter = TopicsPresenter(
            App.get().Di().topicsRepository,
            App.get().Di().forumRepository,
            App.get().Di().favoritesRepository,
            App.get().Di().crossScreenInteractor,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_topics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.id = getInt(TOPICS_ID_ARG)
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
            addItem(getString(R.string.copy_link)) { _, data1 ->
                val url: String = if (data1.isAnnounce) {
                    data1.announceUrl
                } else {
                    "https://4pda.ru/forum/index.php?showtopic=" + data1.id
                }
                Utils.copyToClipBoard(url)
            }
            addItem(getString(R.string.open_theme_forum)) { _, _ ->
                presenter.openTopicForum()
            }
            addItem(getString(R.string.add_to_favorites)) { _, data1 ->
                if (data1.isForum) {
                    openAddForumToFavoriteDialog(data1.id)
                } else {
                    openAddTopicToFavoriteDialog(data1.id)
                }
            }
        }

        refreshLayout.setOnRefreshListener { presenter.loadTopics() }
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = TopicsAdapter()
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(adapterListener)
        paginationHelper.setListener(paginationListener)
    }

    override fun isShadowVisible(): Boolean {
        return true
    }

    override fun showTopics(data: TopicsData) {
        setTitle(data.title)
        adapter.clear()
        if (!data.forumItems.isEmpty())
            adapter.addSection(getString(R.string.forum_section), data.forumItems)
        if (!data.announceItems.isEmpty())
            adapter.addSection(getString(R.string.announce_section), data.announceItems)
        if (!data.pinnedItems.isEmpty())
            adapter.addSection(getString(R.string.pinned_section), data.pinnedItems)
        adapter.addSection(getString(R.string.themes_section), data.topicItems)
        adapter.notifyDataSetChanged()
        paginationHelper.updatePagination(data.pagination)
        setSubtitle(paginationHelper.title)
        listScrollTop()
    }

    override fun updateList() {
        adapter.notifyDataSetChanged()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu
                .add(R.string.open_forum)
                .setOnMenuItemClickListener {
                    presenter.openForum()
                    true
                }
        if (authHolder.get().isAuth()) {
            menu
                    .add(R.string.mark_read)
                    .setOnMenuItemClickListener {
                        openMarkReadDialog()
                        true
                    }
        }

        menu.add(R.string.fragment_title_search)
                .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_search))
                .setOnMenuItemClickListener {
                    presenter.openSearch()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    private fun openAddForumToFavoriteDialog(forumId: Int) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addForumToFavorite(forumId, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    private fun openAddTopicToFavoriteDialog(topicId: Int) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addTopicToFavorite(topicId, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    private fun openMarkReadDialog() {
        AlertDialog.Builder(context!!)
                .setMessage(getString(R.string.mark_read) + "?")
                .setPositiveButton(R.string.ok) { _, _ ->
                    presenter.markRead()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun onMarkRead() {
        Toast.makeText(context, R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(context, if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        paginationHelper.destroy()
    }

    override fun showItemDialogMenu(item: TopicItem) {
        dialogMenu.apply {
            disallowAll()
            allow(0)
            if (!item.isAnnounce) {
                allow(1)
                if (authHolder.get().isAuth()) {
                    allow(2)
                }
            }
            show(context, this@TopicsFragment, item)
        }
    }

    companion object {
        const val TOPICS_ID_ARG = "TOPICS_ID_ARG"
    }
}
