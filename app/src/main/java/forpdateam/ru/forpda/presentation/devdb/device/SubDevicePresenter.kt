package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.fragments.devdb.device.SubDeviceType
import forpdateam.ru.forpda.ui.fragments.devdb.device.di.DeviceSharedData
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.InjectViewState
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */

data class SubDeviceExtra(
    val type: SubDeviceType
) : QuillExtra

@InjectViewState
class SubDevicePresenter(
    private val argExtra: SubDeviceExtra,
    private val router: TabRouter,
    private val deviceSharedData: DeviceSharedData,
    private val linkHandler: LinkHandler
) : BasePresenter<SubDeviceView>() {

    override fun onFirstViewAttach() {
        deviceSharedData.deviceFlow
            .filterNotNull()
            .onEach {
                when (argExtra.type) {
                    SubDeviceType.Specs -> viewState.bindSpecs(it.specs)
                    SubDeviceType.Comments -> viewState.bindComments(it.comments)
                    SubDeviceType.Articles -> viewState.bindArticles(it.news)
                    SubDeviceType.Discussions -> viewState.bindTopics(it.discussions)
                    SubDeviceType.Firmwares -> viewState.bindTopics(it.firmwares)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCommentClick(item: Device.Comment) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${item.user.id.id}")
    }

    fun onArticleClick(item: Device.Article) {
        linkHandler.handle("https://4pda.to/index.php?p=${item.id.id}")
    }

    fun onTopicClick(item: Device.Topic) {
        linkHandler.handle("https://4pda.to/forum/index.php?showtopic=${item.id.id}")
    }

}
