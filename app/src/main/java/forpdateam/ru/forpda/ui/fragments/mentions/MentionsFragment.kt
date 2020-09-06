package forpdateam.ru.forpda.ui.fragments.mentions

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.mentions.MentionsPresenter
import forpdateam.ru.forpda.presentation.mentions.MentionsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper

/**
 * Created by radiationx on 21.01.17.
 */

class MentionsFragment : RecyclerFragment(), MentionsView {

    private lateinit var dialogMenu: DynamicDialogMenu<MentionsFragment, MentionItem>
    private lateinit var adapter: MentionsAdapter
    private lateinit var paginationHelper: PaginationHelper
    private val authHolder = App.get().Di().authHolder

    private val paginationListener = object : PaginationHelper.PaginationListener {
        override fun onTabSelected(tab: TabLayout.Tab): Boolean {
            return refreshLayout.isRefreshing
        }

        override fun onSelectedPage(pageNumber: Int) {
            presenter.currentSt = pageNumber
            presenter.getMentions()
        }
    }

    private val adapterListener = object : BaseAdapter.OnItemClickListener<MentionItem> {
        override fun onItemClick(item: MentionItem) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: MentionItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    @InjectPresenter
    lateinit var presenter: MentionsPresenter

    @ProvidePresenter
    fun providePresenter(): MentionsPresenter = MentionsPresenter(
            App.get().Di().mentionsRepository,
            App.get().Di().favoritesRepository,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_mentions)
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
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.add_to_favorites)) { _, data ->
                presenter.addToFavorites(data)
            }
        }

        adapter = MentionsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(adapterListener)
        refreshLayout.setOnRefreshListener { presenter.getMentions() }
        paginationHelper.setListener(paginationListener)
    }

    override fun showMentions(data: MentionsData) {
        if (data.items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(context)
                        .setImage(R.drawable.ic_notifications)
                        .setTitle(R.string.funny_mentions_nodata_title)
                        .setDesc(R.string.funny_mentions_nodata_desc)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }

        adapter.addAll(data.items)
        paginationHelper.updatePagination(data.pagination)
        setSubtitle(paginationHelper.title)
        listScrollTop()
    }

    override fun onDestroy() {
        super.onDestroy()
        paginationHelper.destroy()
    }

    override fun showItemDialogMenu(item: MentionItem) {
        dialogMenu.apply {
            disallowAll()
            allow(0)
            if (item.isTopic && authHolder.get().isAuth()) {
                allow(1)
            }
            show(context, this@MentionsFragment, item)
        }
    }

    override fun showAddFavoritesDialog(id: Int) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addTopicToFavorite(id, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(context, if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
    }
}
