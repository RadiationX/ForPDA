package forpdateam.ru.forpda.presentation.notes

import android.util.Log
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.note.NotesRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import moxy.InjectViewState

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
            .onStart {
                viewState.setRefreshing(true)
            }
            .onEach {
                viewState.setRefreshing(false)
                currentItems.clear()
                currentItems.addAll(it)
                updateItems()
            }
            .launchIn(viewModelScope)

        closeableInfoHolder
            .observe()
            .onEach { info ->
                Log.d("kekeke", "closeable $info")
                currentInfos.clear()
                currentInfos.addAll(info.filter { closeableInfoIds.contains(it.id) && !it.isClosed })
                updateItems()
            }
            .launchIn(viewModelScope)
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            coRunCatching {
                notesRepository.deleteNote(id)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun addNote(item: NoteItem) {
        viewModelScope.launch {
            coRunCatching {
                notesRepository.addNote(item)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun addNotes(items: List<NoteItem>) {
        viewModelScope.launch {
            coRunCatching {
                notesRepository.addNotes(items)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun exportNotes() {
        viewModelScope.launch {
            coRunCatching {
                notesRepository.exportNotes()
            }.onSuccess {
                viewState.onExportNotes()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun importNotes(file: RequestFile) {
        viewModelScope.launch {
            coRunCatching {
                notesRepository.importNotes(file)
            }.onSuccess {
                viewState.onImportNotes()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
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
