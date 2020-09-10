package forpdateam.ru.forpda.ui.fragments.notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.FilePickHelper
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.presentation.notes.NotesPresenter
import forpdateam.ru.forpda.presentation.notes.NotesView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.notes.adapters.NotesAdapter
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter

/**
 * Created by radiationx on 06.09.17.
 */

class NotesFragment : RecyclerFragment(), NotesView, BaseAdapter.OnItemClickListener<NoteItem> {

    private lateinit var adapter: NotesAdapter
    private val dialogMenu = DynamicDialogMenu<NotesFragment, NoteItem>()

    @InjectPresenter
    lateinit var presenter: NotesPresenter

    @ProvidePresenter
    fun providePresenter(): NotesPresenter = NotesPresenter(
            App.get().Di().notesRepository,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_notes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCardsBackground()
        setScrollFlagsEnterAlways()
        adapter = NotesAdapter()
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        refreshLayout.setOnRefreshListener { presenter.loadNotes() }
        recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px8, false))

        dialogMenu.apply {
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.edit)) { _, data ->
                presenter.editNote(data)
            }
            addItem(getString(R.string.delete)) { _, data ->
                presenter.deleteNote(data.id)
            }
        }
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu
                .add(R.string.add)
                .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_add))
                .setOnMenuItemClickListener {
                    presenter.addNote()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu
                .add(R.string.import_s)
                .setOnMenuItemClickListener {
                    App.get().checkStoragePermission({
                        startActivityForResult(FilePickHelper.pickFile(false), TabFragment.REQUEST_PICK_FILE)
                    }, App.getActivity())
                    true
                }
        menu
                .add(R.string.export_s)
                .setOnMenuItemClickListener {
                    App.get().checkStoragePermission({ presenter.exportNotes() }, App.getActivity())
                    true
                }

    }

    override fun showNotes(items: List<NoteItem>) {
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(context)
                        .setImage(R.drawable.ic_bookmark)
                        .setTitle(R.string.funny_notes_nodata_title)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        adapter.addAll(items)
    }

    override fun showNotesEditPopup(item: NoteItem) {
        NotesAddPopup(context, item)
    }

    override fun showNotesAddPopup() {
        NotesAddPopup(context, null)
    }

    override fun onImportNotes() {
        Toast.makeText(context, "Заметки успешно импортированы", Toast.LENGTH_SHORT).show()
    }

    override fun onExportNotes(path: String) {
        Toast.makeText(context, "Заметки успешно экспортированы в $path", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return
            }
            if (requestCode == TabFragment.REQUEST_PICK_FILE) {
                val files = FilePickHelper.onActivityResult(context, data)
                val file = files[0]
                presenter.importNotes(file)
            } else if (requestCode == TabFragment.REQUEST_SAVE_FILE) {

            }
        }
    }

    override fun onItemClick(item: NoteItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: NoteItem): Boolean {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(context, this@NotesFragment, item)
        }
        return true
    }
}
