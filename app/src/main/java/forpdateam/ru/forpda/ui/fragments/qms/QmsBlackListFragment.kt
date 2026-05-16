package forpdateam.ru.forpda.ui.fragments.qms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.databinding.ToolbarQmsBlackListBinding
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.presentation.qms.blacklist.QmsBlackListPresenter
import forpdateam.ru.forpda.presentation.qms.blacklist.QmsBlackListView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter
import forpdateam.ru.forpda.ui.fragments.tabToolbarBinding
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * Created by radiationx on 22.03.17.
 */

class QmsBlackListFragment : RecyclerFragment(), BaseAdapter.OnItemClickListener<QmsContact>,
    QmsBlackListView {

    private val toolbarBinding by tabToolbarBinding(ToolbarQmsBlackListBinding::bind)

    private val nickField: AppCompatAutoCompleteTextView
        get() = toolbarBinding.qmsBlackListNickField

    private lateinit var adapter: QmsContactsAdapter
    private val dialogMenu = DynamicDialogMenu<QmsBlackListFragment, QmsContact>()

    @InjectPresenter
    lateinit var presenter: QmsBlackListPresenter

    @ProvidePresenter
    fun providePresenter(): QmsBlackListPresenter = QmsBlackListPresenter(
        App.get().Di().qmsInteractor,
        App.get().Di().router,
        App.get().Di().linkHandler,
        App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_blacklist)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseInflateToolbar(R.layout.toolbar_qms_black_list)
        setScrollFlagsEnterAlways()
        nickField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter.searchUser(s.toString())
            }
        })

        refreshLayout.setOnRefreshListener { presenter.loadContacts() }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dialogMenu.apply {
            addItem(getString(R.string.profile)) { _, data ->
                presenter.openProfile(data)
            }
            addItem(getString(R.string.dialogs)) { _, data ->
                presenter.openDialogs(data)
            }
            addItem(getString(R.string.delete)) { _, data ->
                presenter.unBlockUser(data.user.id)
            }
        }

        adapter = QmsContactsAdapter()
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.add)
            .setIcon(R.drawable.ic_toolbar_add)
            .setOnMenuItemClickListener {
                var nick = ""
                if (nickField.text != null)
                    nick = nickField.text.toString()
                presenter.blockUser(nick)
                false
            }
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun showContacts(items: List<QmsContact>) {
        setRefreshing(false)
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(requireContext())
                    .setImage(R.drawable.ic_contacts)
                    .setTitle(R.string.funny_blacklist_nodata_title)
                    .setDesc(R.string.funny_blacklist_nodata_desc)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        recyclerView.scrollToPosition(0)
        adapter.addAll(items)
    }

    override fun clearNickField() {
        nickField.setText("")
    }

    override fun showFoundUsers(items: List<ForumUser>) {
        val nicks = items.map { it.nick.orEmpty() }
        nickField.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nicks
            )
        )
    }

    override fun showItemDialogMenu(item: QmsContact) {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(requireContext(), this@QmsBlackListFragment, item)
        }
    }

    override fun onItemClick(item: QmsContact) {
        presenter.onItemLongClick(item)
    }

    override fun onItemLongClick(item: QmsContact): Boolean {
        presenter.onItemLongClick(item)
        return false
    }
}
