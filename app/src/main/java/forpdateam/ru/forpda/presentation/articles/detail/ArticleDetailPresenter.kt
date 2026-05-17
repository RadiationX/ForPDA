package forpdateam.ru.forpda.presentation.articles.detail

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleDetailPresenter(
    private val articleInteractor: ArticleInteractor,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<ArticleDetailView>() {

    var currentData: DetailsPage? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadArticle()
        articleInteractor
            .observeData()
            .onEach {
                currentData = it

            }
            .launchIn(viewModelScope)
    }

    fun loadArticle() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                articleInteractor.loadArticle()
            }.onSuccess {
                viewState.showArticle(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun openAuthorProfile() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showuser=${it.authorId}", router)
        }
    }

    fun copyLink() {
        currentData?.let {
            utils.copyToClipBoard("https://4pda.to/index.php?p=${it.id}")
        }
    }

    fun shareLink() {
        currentData?.let {
            utils.shareText("https://4pda.to/index.php?p=${it.id}")
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.to/index.php?p=${it.id}"
            viewState.showCreateNote(it.title.orEmpty(), url)
        }
    }

}
