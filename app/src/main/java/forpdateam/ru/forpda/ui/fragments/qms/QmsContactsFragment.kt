package forpdateam.ru.forpda.ui.fragments.qms

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.presentation.qms.contacts.QmsContactsPresenter
import forpdateam.ru.forpda.presentation.qms.contacts.QmsContactsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter

/**
 * Created by radiationx on 25.08.16.
 */
class QmsContactsFragment : RecyclerFragment(), BaseAdapter.OnItemClickListener<QmsContact>, QmsContactsView {

    private lateinit var adapter: QmsContactsAdapter
    private val dialogMenu = DynamicDialogMenu<QmsContactsFragment, QmsContact>()

    @InjectPresenter
    lateinit var presenter: QmsContactsPresenter

    @ProvidePresenter
    internal fun providePresenter(): QmsContactsPresenter = QmsContactsPresenter(
            App.get().Di().qmsInteractor,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().countersHolder,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_contacts)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        contentController.setFirstLoad(false)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFabBehavior()
        refreshLayoutStyle(refreshLayout)
        setScrollFlagsEnterAlways()
        refreshLayout.setOnRefreshListener { presenter.loadContacts() }
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val pauseOnScrollListener = PauseOnScrollListener(ImageLoader.getInstance(), true, true)
        recyclerView.addOnScrollListener(pauseOnScrollListener)


        fab.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_fab_create))
        fab.setOnClickListener { presenter.openChatCreator() }
        fab.visibility = View.VISIBLE

        dialogMenu.apply {
            addItem(getString(R.string.profile)) { _, data ->
                presenter.openProfile(data)
            }
            addItem(getString(R.string.add_to_blacklist)) { _, data ->
                presenter.blockUser(data)
            }
            addItem(getString(R.string.delete)) { _, data ->
                presenter.deleteDialog(data.id)
            }
            addItem(getString(R.string.create_note)) { _, data ->
                presenter.createNote(data)
            }
        }

        adapter = QmsContactsAdapter()
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
        presenter.loadContacts()
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        toolbar.inflateMenu(R.menu.qms_contacts_menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        searchView.setIconifiedByDefault(true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                presenter.searchLocal(newText)
                return false
            }
        })
        searchView.queryHint = getString(R.string.user)
        menu.add(R.string.blacklist)
                .setOnMenuItemClickListener {
                    presenter.openBlackList()
                    false
                }
    }

    override fun onBackPressed(): Boolean {
        return if (toolbar.menu.findItem(R.id.action_search).isActionViewExpanded) {
            recyclerView.adapter = adapter
            toolbar.collapseActionView()
            true
        } else {
            super.onBackPressed()
        }
    }

    override fun showContacts(items: List<QmsContact>) {
        recyclerView.scrollToPosition(0)
        adapter.addAll(items)
    }

    override fun onBlockUser(res: Boolean) {
        if (res) {
            Toast.makeText(context, R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showCreateNote(nick: String, url: String) {
        val title = String.format(getString(R.string.dialogs_Nick), nick)
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun showItemDialogMenu(item: QmsContact) {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(context, this@QmsContactsFragment, item)
        }
    }

    override fun onItemClick(item: QmsContact) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: QmsContact): Boolean {
        presenter.onItemLongClick(item)
        return false
    }
}
