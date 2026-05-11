package forpdateam.ru.forpda.ui.fragments.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.filepicker.registerFilePicker
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.presentation.notes.NotesPresenter
import forpdateam.ru.forpda.presentation.notes.NotesView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.notes.adapters.NotesAdapter
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

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
        App.get().Di().closeableInfoHolder,
        App.get().Di().router,
        App.get().Di().linkHandler,
        App.get().Di().errorHandler
    )

    private val filePicker = registerFilePicker {
        presenter.importNotes(it)
    }

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_notes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCardsBackground()
        setScrollFlagsEnterAlways()
        adapter = NotesAdapter(this, presenter::onInfoClick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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
            .setIcon(App.getVecDrawable(requireContext(), R.drawable.ic_toolbar_add))
            .setOnMenuItemClickListener {
                presenter.addNote()
                true
            }
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu
            .add(R.string.import_s)
            .setOnMenuItemClickListener {
                filePicker.launch()
                true
            }
        menu
            .add(R.string.export_s)
            .setOnMenuItemClickListener {
                App.get().checkStoragePermission({ presenter.exportNotes() }, App.getActivity())
                true
            }

    }

    override fun showNotes(items: List<NoteItem>, info: List<CloseableInfo>) {
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(requireContext())
                    .setImage(R.drawable.ic_bookmark)
                    .setTitle(R.string.funny_notes_nodata_title)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        adapter.bindItems(items, info)
    }

    override fun showNotesEditPopup(item: NoteItem) {
        NotesAddPopup(requireContext(), item)
    }

    override fun showNotesAddPopup() {
        NotesAddPopup(requireContext(), null)
    }

    override fun onImportNotes() {
        Toast.makeText(requireContext(), "Заметки успешно импортированы", Toast.LENGTH_SHORT).show()
    }

    override fun onExportNotes(path: String) {
        Toast.makeText(requireContext(), "Заметки успешно экспортированы в $path", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(item: NoteItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: NoteItem): Boolean {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(requireContext(), this@NotesFragment, item)
        }
        return true
    }
}
