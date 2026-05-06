package forpdateam.ru.forpda.presentation.history

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
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
class HistoryPresenter(
    private val historyRepository: HistoryRepository,
    private val router: TabRouter,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler
) : BasePresenter<HistoryView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        historyRepository
            .observeItems()
            .onStart {
                viewState.setRefreshing(true)
            }
            .onEach {
                viewState.setRefreshing(false)
                viewState.showHistory(it)
            }
            .launchIn(viewModelScope)
    }


    fun remove(id: Int) {
        viewModelScope.launch {
            coRunCatching {
                historyRepository.remove(id)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            coRunCatching {
                historyRepository.clear()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun copyLink(item: HistoryItem) {
        Utils.copyToClipBoard(item.url)
    }

    fun onItemClick(item: HistoryItem) {
        linkHandler.handle(
            item.url, router, mapOf(
                Screen.ARG_TITLE to item.title
            )
        )
    }

    fun onItemLongClick(item: HistoryItem) {
        viewState.showItemDialogMenu(item)
    }
}
