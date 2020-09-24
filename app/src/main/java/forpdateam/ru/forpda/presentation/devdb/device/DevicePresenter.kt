package forpdateam.ru.forpda.presentation.devdb.device

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class DevicePresenter(
        private val devDbRepository: DevDbRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<DeviceView>() {

    var deviceId: String? = null
    var currentData: Device? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadBrand()
    }

    fun loadBrand() {
        devDbRepository
                .getDevice(deviceId.orEmpty())
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


    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch())
    }

    fun copyLink() {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            Utils.shareText("https://4pda.ru/devdb/${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val title = "DevDb: ${it.brandTitle} ${it.title}"
            val url = "https://4pda.ru/devdb/${it.id}"
            viewState.showCreateNote(title, url)
        }
    }

    fun openDevices() {
        currentData?.let {
            linkHandler.handle("https://4pda.ru/devdb/${it.catId}/${it.brandId}", router)
        }
    }

    fun openBrands() {
        currentData?.let {
            linkHandler.handle("https://4pda.ru/devdb/${it.catId}", router)
        }
    }
}
