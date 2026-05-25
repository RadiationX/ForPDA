package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */
data class DeviceExtra(
    val deviceId: DevDbDeviceId
): QuillExtra

@InjectViewState
class DevicePresenter(
    private val argExtra: DeviceExtra,
    private val devDbRepository: DevDbRepository,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<DeviceView>() {

    var currentData: Device? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadBrand()
    }

    fun loadBrand() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                devDbRepository.getDevice(argExtra.deviceId)
            }.onSuccess {
                currentData = it
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch(text = null))
    }

    fun copyLink() {
        currentData?.let {
            utils.copyToClipBoard("https://4pda.to/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            utils.shareText("https://4pda.to/devdb/${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val title = "DevDb: ${it.brandTitle} ${it.title}"
            val url = "https://4pda.to/devdb/${it.id}"
            viewState.showCreateNote(title, url)
        }
    }

    fun openDevices() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/devdb/${it.catId}/${it.brandId}")
        }
    }

    fun openBrands() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/devdb/${it.catId}")
        }
    }
}
