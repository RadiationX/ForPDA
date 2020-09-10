package forpdateam.ru.forpda.ui.fragments.qms

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.presentation.qms.themes.QmsThemesPresenter
import forpdateam.ru.forpda.presentation.qms.themes.QmsThemesView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsThemesAdapter
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter

/**
 * Created by radiationx on 25.08.16.
 */
class QmsThemesFragment : RecyclerFragment(), BaseAdapter.OnItemClickListener<QmsTheme>, QmsThemesView {

    private lateinit var blackListMenuItem: MenuItem
    private lateinit var noteMenuItem: MenuItem
    private lateinit var adapter: QmsThemesAdapter
    private val dialogMenu = DynamicDialogMenu<QmsThemesFragment, QmsTheme>()

    @InjectPresenter
    lateinit var presenter: QmsThemesPresenter

    @ProvidePresenter
    fun providePresenter(): QmsThemesPresenter = QmsThemesPresenter(
            App.get().Di().qmsInteractor,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_dialogs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.themesId = getInt(USER_ID_ARG)
            presenter.avatarUrl = getString(USER_AVATAR_ARG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        contentController.setFirstLoad(false)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFabBehavior()
        setScrollFlagsEnterAlways()

        refreshLayout.setOnRefreshListener { presenter.loadThemes() }
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        fab.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_fab_create))
        fab.setOnClickListener { presenter.openChat() }
        fab.visibility = View.VISIBLE

        dialogMenu.apply {
            addItem(getString(R.string.delete)) { _, data ->
                presenter.deleteTheme(data.id)
            }
            addItem(getString(R.string.create_note)) { _, data ->
                presenter.createThemeNote(data)
            }
        }

        adapter = QmsThemesAdapter()
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
        presenter.loadThemes()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        blackListMenuItem = menu
                .add(R.string.add_to_blacklist)
                .setOnMenuItemClickListener {
                    presenter.blockUser()
                    false
                }
        noteMenuItem = menu
                .add(R.string.create_note)
                .setOnMenuItemClickListener {
                    presenter.createNote()
                    true
                }
        refreshToolbarMenuItems(false)
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            blackListMenuItem.isEnabled = true
            noteMenuItem.isEnabled = true
        } else {
            blackListMenuItem.isEnabled = false
            noteMenuItem.isEnabled = false
        }
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        super.setRefreshing(isRefreshing)
        refreshToolbarMenuItems(!isRefreshing)
    }

    override fun showThemes(data: QmsThemes) {
        recyclerView.scrollToPosition(0)

        setTabTitle(String.format(getString(R.string.dialogs_Nick), data.nick))
        setTitle(data.nick)

        adapter.addAll(data.themes)
        adapter.notifyDataSetChanged()
    }

    override fun showAvatar(avatarUrl: String) {
        ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView)
        toolbarImageView.visibility = View.VISIBLE
        toolbarImageView.setOnClickListener { presenter.openProfile(presenter.themesId) }
        toolbarImageView.contentDescription = App.get().getString(R.string.user_avatar)
    }

    override fun showCreateNote(nick: String, url: String) {
        val title = String.format(getString(R.string.dialogs_Nick), nick)
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun showCreateNote(name: String, nick: String, url: String) {
        val title = String.format(getString(R.string.dialog_Title_Nick), name, nick)
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun onBlockUser(res: Boolean) {
        Toast.makeText(context, R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show()
    }

    override fun showItemDialogMenu(item: QmsTheme) {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(context, this@QmsThemesFragment, item)
        }
    }

    override fun onItemClick(item: QmsTheme) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: QmsTheme): Boolean {
        presenter.onItemLongClick(item)
        return false
    }

    companion object {
        const val USER_ID_ARG = "USER_ID_ARG"
        const val USER_AVATAR_ARG = "USER_AVATAR_ARG"
    }
}
