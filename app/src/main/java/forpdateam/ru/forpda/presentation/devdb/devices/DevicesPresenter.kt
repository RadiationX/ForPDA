package forpdateam.ru.forpda.presentation.devdb.devices

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class DevicesPresenter(
        private val devDbRepository: DevDbRepository,
        private val router: TabRouter,
        private val errorHandler: IErrorHandler
) : BasePresenter<DevicesView>() {

    var categoryId: String? = null
    var brandId: String? = null
    var currentData: Brand? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadBrand()
    }

    fun loadBrand() {
        devDbRepository
                .getBrand(categoryId.orEmpty(), brandId.orEmpty())
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun openDevice(item: Brand.DeviceItem) {
        currentData?.let {
            router.navigateTo(Screen.DevDbDevice().apply {
                deviceId = item.id
            })
        }
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch())
    }

    fun copyLink(item: Brand.DeviceItem) {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.to/devdb/${item.id}")
        }
    }

    fun shareLink(item: Brand.DeviceItem) {
        currentData?.let {
            Utils.shareText("https://4pda.to/devdb/${item.id}")
        }
    }

    fun createNote(item: Brand.DeviceItem) {
        currentData?.let {
            val title = "DevDb: ${it.title} ${item.title}"
            val url = "https://4pda.to/devdb/" + item.id
            viewState.showCreateNote(title, url)
        }
    }
}
