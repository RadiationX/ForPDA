package forpdateam.ru.forpda.ui.fragments.forum

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.forum.ForumPresenter
import forpdateam.ru.forpda.presentation.forum.ForumView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 15.02.17.
 */

class ForumFragment : RecyclerFragment(), ForumView {

    private lateinit var dialogMenu: DynamicDialogMenu<ForumFragment, ForumItemFlat>
    private val authHolder by inject<AuthHolder>()

    private lateinit var adapter: ForumsAdapter

    private val clickListener = object : OnItemClickListener<ForumItemFlat> {

        override fun onItemClick(item: ForumItemFlat) {
            presenter.navigateToForum(item)
        }

        override fun onItemLongClick(item: ForumItemFlat): Boolean {
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

                show(requireContext(), this@ForumFragment, item)
            }
            return false
        }
    }

    private val presenter by quillMoxyPresenter<ForumPresenter>()

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_forum)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.targetForumId = getInt(ARG_FORUM_ID, -1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setScrollFlagsEnterAlways()

        adapter = ForumsAdapter(clickListener)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
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

    override fun showForums(forums: List<ForumItemFlat>) {
        adapter.bindItems(forums)
    }

    private fun openAddToFavoriteDialog(forumId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.favorites_subscribe_email)
            .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                presenter.addToFavorite(forumId, FavoritesApi.SUB_TYPES[which])
            }
            .show()
    }

    private fun openMarkReadDialog(item: ForumItemFlat) {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.mark_read) + "?")
            .setPositiveButton(R.string.ok) { _, _ ->
                presenter.markRead(item.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openMarkAllReadDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.mark_all_read) + "?")
            .setPositiveButton(R.string.ok) { _, _ ->
                presenter.markAllRead()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onMarkRead() {
        Toast.makeText(requireContext(), R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onMarkAllRead() {
        Toast.makeText(requireContext(), R.string.action_complete, Toast.LENGTH_SHORT).show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(
            requireContext(),
            if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun scrollToForum(id: Int) {
        adapter.expand(id)
        recyclerView.scrollToPosition(adapter.getItemPosition(id))
    }

    companion object {
        const val ARG_FORUM_ID = "ARG_FORUM_ID"
    }
}
