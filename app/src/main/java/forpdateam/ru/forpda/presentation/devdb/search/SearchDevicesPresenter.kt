package forpdateam.ru.forpda.presentation.devdb.search

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class SearchDevicesPresenter(
    private val devDbRepository: DevDbRepository,
    private val router: TabRouter,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<SearchDevicesView>() {

    var searchQuery: String? = null

    var currentData: BrandSearch? = null

    fun refresh() = search(searchQuery)

    fun search(query: String?) {
        searchQuery = query
        if (searchQuery.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                devDbRepository.search(searchQuery.orEmpty())
            }.onSuccess {
                currentData = it
                viewState.showData(it, searchQuery.orEmpty())
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun openDevice(item: Brand.DeviceItem) {
        currentData?.let {
            router.navigateTo(Screen.DevDbDevice(deviceId = item.id))
        }
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch())
    }

    fun copyLink(item: Brand.DeviceItem) {
        currentData?.let {
            utils.copyToClipBoard("https://4pda.to/devdb/${item.id}")
        }
    }

    fun shareLink(item: Brand.DeviceItem) {
        currentData?.let {
            utils.shareText("https://4pda.to/devdb/${item.id}")
        }
    }

    fun createNote(item: Brand.DeviceItem) {
        currentData?.let {
            val title = "DevDb: ${item.title}"
            val url = "https://4pda.to/devdb/" + item.id
            viewState.showCreateNote(title, url)
        }
    }
}
