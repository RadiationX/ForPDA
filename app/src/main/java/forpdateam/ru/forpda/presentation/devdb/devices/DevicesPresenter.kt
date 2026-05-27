package forpdateam.ru.forpda.presentation.devdb.devices

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */

data class DevicesExtra(
    val devicesId: DevDbDevicesId
): QuillExtra

@InjectViewState
class DevicesPresenter(
    private val argExtra: DevicesExtra,
    private val devDbRepository: DevDbRepository,
    private val router: TabRouter,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<DevicesView>() {

    var currentData: Brand? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadBrand()
    }

    fun loadBrand() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                devDbRepository.getDevices(argExtra.devicesId)
            }.onSuccess {
                currentData = it
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun openDevice(item: Brand.DeviceItem) {
        router.navigateTo(Screen.DevDbDevice(deviceId = item.id))
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch(text = null))
    }

    fun copyLink(item: Brand.DeviceItem) {
        currentData?.let {
            utils.copyToClipBoard("https://4pda.to/devdb/${item.id.id}")
        }
    }

    fun shareLink(item: Brand.DeviceItem) {
        currentData?.let {
            utils.shareText("https://4pda.to/devdb/${item.id.id}")
        }
    }

    fun createNote(item: Brand.DeviceItem) {
        currentData?.let {
            val title = "DevDb: ${it.title} ${item.title}"
            val url = "https://4pda.to/devdb/" + item.id.id
            viewState.showCreateNote(title, url)
        }
    }
}
