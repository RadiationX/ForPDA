package forpdateam.ru.forpda.presentation.notes

import android.util.Log
import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.note.NotesRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class NotesPresenter(
        private val notesRepository: NotesRepository,
        private val closeableInfoHolder: CloseableInfoHolder,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<NotesView>() {

    private val closeableInfoIds = arrayOf(
            CloseableInfoHolder.item_notes_sync
    )

    private val currentItems = mutableListOf<NoteItem>()
    private val currentInfos = mutableListOf<CloseableInfo>()


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        notesRepository
                .observeItems()
                .subscribe({
                    currentItems.clear()
                    currentItems.addAll(it)
                    updateItems()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()

        closeableInfoHolder
                .observe()
                .subscribe { info ->
                    Log.d("kekeke", "closeable $info")
                    currentInfos.clear()
                    currentInfos.addAll(info.filter { closeableInfoIds.contains(it.id) && !it.isClosed })
                    updateItems()
                }
                .untilDestroy()
        loadNotes()
    }

    fun loadNotes() {
        notesRepository
                .loadNotes()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentItems.clear()
                    currentItems.addAll(it)
                    updateItems()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun deleteNote(id: Long) {
        notesRepository
                .deleteNote(id)
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun addNote(item: NoteItem) {
        notesRepository
                .addNote(item)
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun addNotes(items: List<NoteItem>) {
        notesRepository
                .addNotes(items)
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun exportNotes() {
        notesRepository
                .exportNotes()
                .subscribe({
                    viewState.onExportNotes(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun importNotes(file: RequestFile) {
        notesRepository
                .importNotes(file)
                .subscribe({
                    viewState.onImportNotes()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }


    fun onItemClick(item: NoteItem) {
        linkHandler.handle(item.link, router)
    }

    fun onInfoClick(info: CloseableInfo) {
        closeableInfoHolder.close(info)
    }

    fun copyLink(item: NoteItem) {
        Utils.copyToClipBoard(item.link)
    }

    fun editNote(item: NoteItem) {
        viewState.showNotesEditPopup(item)
    }

    fun addNote() {
        viewState.showNotesAddPopup()
    }

    private fun updateItems() {
        Log.d("kekeke", "updateItems ${currentItems.size}, ${currentInfos.size}")
        viewState.showNotes(currentItems, currentInfos)
    }

}
