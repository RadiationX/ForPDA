package forpdateam.ru.forpda.presentation.forumrules

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class ForumRulesPresenter(
    private val forumRepository: ForumRepository,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val forumRulesTemplate: ForumRulesTemplate,
    private val templateManager: TemplateManager,
    private val errorHandler: IErrorHandler
) : BasePresenter<ForumRulesView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        templateManager
            .observeThemeType()
            .subscribe {
                viewState.setStyleType(it)
            }
            .untilDestroy()

        mainPreferencesHolder
            .observeWebViewFontSize()
            .subscribe {
                viewState.setFontSize(it)
            }
            .untilDestroy()

        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getRules()
            }.map {
                forumRulesTemplate.mapEntity(it)
            }.onSuccess {
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }


}
