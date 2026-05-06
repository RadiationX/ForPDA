package forpdateam.ru.forpda.presentation.articles.detail.content

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleContentPresenter(
    private val articleInteractor: ArticleInteractor,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val templateManager: TemplateManager,
    private val errorHandler: IErrorHandler
) : BasePresenter<ArticleContentView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        templateManager
            .observeThemeType()
            .onEach {
                viewState.setStyleType(it)
            }
            .launchIn(viewModelScope)
        mainPreferencesHolder
            .observeWebViewFontSize()
            .onEach {
                viewState.setFontSize(it)
            }
            .launchIn(viewModelScope)
        articleInteractor
            .observeData()
            .onEach {
                viewState.showData(it)
            }
            .launchIn(viewModelScope)
    }

    fun sendPoll(from: String, pollId: Int, answersId: IntArray) {
        viewModelScope.launch {
            coRunCatching {
                articleInteractor.sendPoll(from, pollId, answersId)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

}
