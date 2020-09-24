package forpdateam.ru.forpda.presentation.history

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

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
                .subscribe({
                    viewState.showHistory(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
        getHistory()
    }

    fun getHistory() {
        historyRepository
                .getHistory()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showHistory(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun remove(id: Int) {
        historyRepository
                .remove(id)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun clear() {
        historyRepository
                .clear()
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun copyLink(item: HistoryItem) {
        Utils.copyToClipBoard(item.url)
    }

    fun onItemClick(item: HistoryItem) {
        linkHandler.handle(item.url, router, mapOf(
                Screen.ARG_TITLE to  item.title
        ))
    }

    fun onItemLongClick(item: HistoryItem) {
        viewState.showItemDialogMenu(item)
    }
}
