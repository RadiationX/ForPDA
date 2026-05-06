package forpdateam.ru.forpda.presentation.announce

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AnnouncePresenter(
    private val forumRepository: ForumRepository,
    private val announceTemplate: AnnounceTemplate,
    private val templateManager: TemplateManager,
    private val errorHandler: IErrorHandler
) : BasePresenter<AnnounceView>() {

    var id = 0
    var forumId = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        templateManager
            .observeThemeType()
            .subscribe {
                viewState.setStyleType(it)
            }
            .untilDestroy()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getAnnounce(id, forumId)
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
