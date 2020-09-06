package forpdateam.ru.forpda.presentation.notes

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.notes.NoteItem
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
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<NotesView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        notesRepository
                .observeItems()
                .subscribe({
                    viewState.showNotes(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
        loadNotes()
    }

    fun loadNotes() {
        notesRepository
                .loadNotes()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showNotes(it)
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

    fun copyLink(item: NoteItem) {
        Utils.copyToClipBoard(item.link)
    }

    fun editNote(item: NoteItem) {
        viewState.showNotesEditPopup(item)
    }

    fun addNote() {
        viewState.showNotesAddPopup()
    }

}
