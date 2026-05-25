package forpdateam.ru.forpda.presentation.announce

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 02.01.18.
 */
data class AnnounceExtra(
    val announceId: AnnounceId
) : QuillExtra

@InjectViewState
class AnnouncePresenter(
    private val argExtra: AnnounceExtra,
    private val forumRepository: ForumRepository,
    private val announceTemplate: AnnounceTemplate,
    private val templateManager: TemplateManager,
    private val errorHandler: ErrorHandler
) : BasePresenter<AnnounceView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        templateManager
            .observeThemeType()
            .onEach {
                viewState.setStyleType(it)
            }
            .launchIn(viewModelScope)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getAnnounce(argExtra.announceId)
            }.map {
                announceTemplate.mapEntity(it)
            }.onSuccess {
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

}
