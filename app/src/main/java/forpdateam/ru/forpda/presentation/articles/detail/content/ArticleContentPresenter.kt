package forpdateam.ru.forpda.presentation.articles.detail.content

import moxy.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.ui.TemplateManager

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
        articleInteractor
                .observeData()
                .subscribe({
                    viewState.showData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun sendPoll(from: String, pollId: Int, answersId: IntArray) {
        articleInteractor
                .sendPoll(from, pollId, answersId)
                .subscribe({}, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

}
