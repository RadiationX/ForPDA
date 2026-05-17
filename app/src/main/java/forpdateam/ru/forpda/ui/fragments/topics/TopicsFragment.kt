package forpdateam.ru.forpda.ui.fragments.topics

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.topics.TopicsPresenter
import forpdateam.ru.forpda.presentation.topics.TopicsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 01.03.17.
 */

class TopicsFragment : RecyclerFragment(), TopicsView {

    private lateinit var adapter: TopicsAdapter
    private lateinit var paginationHelper: PaginationHelper
    private lateinit var dialogMenu: DynamicDialogMenu<TopicsFragment, TopicItem>
    private val authHolder by inject<AuthHolder>()


    private val paginationListener = object : PaginationHelper.PaginationListener {
        override fun onTabSelected(tab: TabLayout.Tab): Boolean {
            return refreshLayout.isRefreshing
        }

        override fun onSelectedPage(pageNumber: Int) {
            presenter.loadPage(pageNumber)
        }
    }

    private val adapterListener = object : OnItemClickListener<TopicItem> {
        override fun onItemClick(item: TopicItem) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: TopicItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    private val presenter by quillMoxyPresenter<TopicsPresenter>()

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_topics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.id = getInt(TOPICS_ID_ARG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paginationHelper = PaginationHelper(requireActivity())
        paginationHelper.addInToolbar(toolbarLayout, configuration.isFitSystemWindow)

        setScrollFlagsEnterAlways()

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.copy_link)) { _, data1 ->
                presenter.copyLink(data1)
            }
            addItem(getString(R.string.open_theme_forum)) { _, _ ->
                presenter.openTopicForum()
            }
            addItem(getString(R.string.add_to_favorites)) { _, data1 ->
                when (data1) {
                    is TopicItem.Forum -> openAddForumToFavoriteDialog(data1.id)
                    is TopicItem.Topic -> openAddTopicToFavoriteDialog(data1.id)
                    is TopicItem.Announce -> {
                        // do nothing
                    }
                }
            }
        }

        refreshLayout.setOnRefreshListener { presenter.loadTopics() }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TopicsAdapter(adapterListener)
        recyclerView.adapter = adapter
        paginationHelper.setListener(paginationListener)
    }

    override fun isShadowVisible(): Boolean {
        return true
    }

    override fun showTopics(data: TopicsData) {
        setTitle(data.title)
        adapter.bindItems(data)
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
            .setIcon(R.drawable.ic_toolbar_search)
            .setOnMenuItemClickListener {
                presenter.openSearch()
                true
            }
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    private fun openAddForumToFavoriteDialog(forumId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.favorites_subscribe_email)
            .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                presenter.addForumToFavorite(forumId, FavoritesApi.SUB_TYPES[which])
            }
            .show()
    }

    private fun openAddTopicToFavoriteDialog(topicId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.favorites_subscribe_email)
            .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                presenter.addTopicToFavorite(topicId, FavoritesApi.SUB_TYPES[which])
            }
            .show()
    }

    private fun openMarkReadDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.mark_read) + "?")
            .setPositiveButton(R.string.ok) { _, _ ->
                presenter.markRead()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onMarkRead() {
        Toast.makeText(requireContext(), R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(
            requireContext(),
            if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        paginationHelper.destroy()
    }

    override fun showItemDialogMenu(item: TopicItem) {
        dialogMenu.apply {
            disallowAll()
            allow(0)
            if (item !is TopicItem.Announce) {
                allow(1)
                if (authHolder.get().isAuth()) {
                    allow(2)
                }
            }
            show(requireContext(), this@TopicsFragment, item)
        }
    }

    companion object {
        const val TOPICS_ID_ARG = "TOPICS_ID_ARG"
    }
}
