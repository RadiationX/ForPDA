package forpdateam.ru.forpda.presentation.announce

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager

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
        forumRepository
                .getAnnounce(id, forumId)
                .map { announceTemplate.mapEntity(it) }
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

}
