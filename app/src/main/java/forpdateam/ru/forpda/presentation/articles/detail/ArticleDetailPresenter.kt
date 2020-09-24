package forpdateam.ru.forpda.presentation.articles.detail

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleDetailPresenter(
        private val articleInteractor: ArticleInteractor,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ArticleDetailView>() {

    var currentData: DetailsPage? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadArticle()
        articleInteractor
                .observeData()
                .subscribe({
                    currentData = it
                },{
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun loadArticle() {
        articleInteractor
                .loadArticle()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showArticle(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun openAuthorProfile() {
        currentData?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.authorId}", router)
        }
    }

    fun copyLink() {
        currentData?.let {
            Utils.copyToClipBoard("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            Utils.shareText("https://4pda.ru/index.php?p=${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.ru/index.php?p=${it.id}"
            viewState.showCreateNote(it.title.orEmpty(), url)
        }
    }

}
