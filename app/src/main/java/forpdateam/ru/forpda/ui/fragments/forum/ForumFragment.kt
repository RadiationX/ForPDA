package forpdateam.ru.forpda.ui.fragments.forum

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.forum.ForumPresenter
import forpdateam.ru.forpda.presentation.forum.ForumView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu

/**
 * Created by radiationx on 15.02.17.
 */

class ForumFragment : TabFragment(), ForumView {

    private lateinit var root: TreeNode
    private lateinit var treeView: AndroidTreeView
    private lateinit var treeContainer: NestedScrollView

    private lateinit var dialogMenu: DynamicDialogMenu<ForumFragment, ForumItemTree>
    private val authHolder = App.get().Di().authHolder

    private var listScrollY = 0
    private var appBarOffset = 0

    private val nodeClickListener = TreeNode.TreeNodeClickListener { _, value ->
        val item = value as ForumItemTree
        if (item.forums == null) {
            presenter.navigateToForum(item)
        }
    }

    private val nodeLongClickListener = TreeNode.TreeNodeLongClickListener { _, value ->
        val item = value as ForumItemTree
        dialogMenu.apply {
            disallowAll()
            if (item.level > 0)
                allow(0)
            allow(1)
            if (authHolder.get().isAuth()) {
                allow(2)
                allow(3)
            }
            allow(4)

            show(context, this@ForumFragment, item)
        }

        false
    }

    @InjectPresenter
    lateinit var presenter: ForumPresenter

    @ProvidePresenter
    fun providePresenter(): ForumPresenter = ForumPresenter(
            App.get().Di().forumRepository,
            App.get().Di().favoritesRepository,
            App.get().Di().router,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_forum)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.targetForumId = getInt(ARG_FORUM_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_forum)
        treeContainer = findViewById(R.id.nested_scroll_view) as NestedScrollView
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListsBackground()
        setScrollFlagsEnterAlways()

        treeContainer.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { nestedScrollView, _, _, _, _ ->
            listScrollY = nestedScrollView.computeVerticalScrollOffset()
            updateToolbarShadow()
        })

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, i ->
            appBarOffset = i
            updateToolbarShadow()
        })

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.open_forum)) { _, data ->
                presenter.navigateToForum(data)
            }
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.mark_read)) { _, data ->
                openMarkReadDialog(data)
            }
            addItem(getString(R.string.add_to_favorites)) { _, data ->
                openAddToFavoriteDialog(data.id)
            }
            addItem(getString(R.string.fragment_title_search)) { _, data ->
                presenter.navigateToSearch(data)
            }
        }

    }

    override fun isShadowVisible(): Boolean {
        return appBarOffset != 0 || listScrollY > 0
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.forum_refresh)
                .setOnMenuItemClickListener {
                    presenter.loadForums()
                    false
                }
        menu.add(R.string.mark_all_read)
                .setOnMenuItemClickListener {
                    openMarkAllReadDialog()
                    false
                }
    }

    override fun showForums(forumRoot: ForumItemTree) {
        treeView = AndroidTreeView(context)
        root = TreeNode.root()
        recourse(forumRoot, root)
        treeView.setRoot(root)

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom)
        treeView.setDefaultViewHolder(DefaultForumHolder::class.java)
        treeView.setDefaultNodeClickListener(nodeClickListener)
        treeView.setDefaultNodeLongClickListener(nodeLongClickListener)
        treeContainer.removeAllViews()
        treeContainer.addView(treeView.view)
    }

    private fun openAddToFavoriteDialog(forumId: Int) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addToFavorite(forumId, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    private fun openMarkReadDialog(item: ForumItemTree) {
        AlertDialog.Builder(context!!)
                .setMessage(getString(R.string.mark_read) + "?")
                .setPositiveButton(R.string.ok) { _, _ ->
                    presenter.markRead(item.id)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun openMarkAllReadDialog() {
        AlertDialog.Builder(context!!)
                .setMessage(getString(R.string.mark_all_read) + "?")
                .setPositiveButton(R.string.ok) { _, _ ->
                    presenter.markAllRead()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    override fun onMarkRead() {
        Toast.makeText(context, R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onMarkAllRead() {
        Toast.makeText(context, R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(context, if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
    }

    override fun scrollToForum(id: Int) {
        val targetNode = findNodeById(id, root)

        if (targetNode != null) {
            var upToParent: TreeNode = targetNode
            while (upToParent.parent != null) {
                treeView.expandNode(upToParent)
                upToParent = upToParent.parent
            }
        }
    }

    private fun findNodeById(id: Int, root: TreeNode): TreeNode? {
        if (root.value != null && (root.value as ForumItemTree).id == id) return root
        if (root.children == null && root.children.isEmpty()) return null
        for (item in root.children) {
            val node = findNodeById(id, item)
            if (node != null) return node
        }
        return null
    }

    private fun recourse(rootForum: ForumItemTree, rootNode: TreeNode) {
        rootForum.forums?.also {
            for (item in it) {
                val child = TreeNode(item)
                recourse(item, child)
                rootNode.addChild(child)
            }
        } ?: return
    }

    companion object {
        const val ARG_FORUM_ID = "ARG_FORUM_ID"
    }
}
