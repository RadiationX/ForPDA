package forpdateam.ru.forpda.presentation.articles.detail.comments

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
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
class ArticleCommentPresenter(
    private val articleInteractor: ArticleInteractor,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val authHolder: AuthHolder,
    private val errorHandler: ErrorHandler
) : BasePresenter<ArticleCommentView>() {

    private var firstShow: Boolean = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        articleInteractor
            .observeComments()
            .onEach {
                viewState.showComments(it)
                if (firstShow) {
                    val targetCommentId = articleInteractor.initData.commentId
                    val index = it.indexOfFirst { it.id == targetCommentId }
                    viewState.scrollToComment(index)
                    firstShow = false
                }
            }
            .launchIn(viewModelScope)

        authHolder
            .observe()
            .onEach { viewState.setMessageFieldVisible(it.isAuth()) }
            .launchIn(viewModelScope)
    }

    fun updateComments() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                articleInteractor.loadArticle()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun replyComment(commentId: Int, text: String) {
        viewModelScope.launch {
            viewState.setSendRefreshing(true)
            coRunCatching {
                articleInteractor.replyComment(commentId, text)
            }.onSuccess {
                viewState.onReplyComment()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setSendRefreshing(false)
        }
    }

    fun likeComment(commentId: Int) {
        viewModelScope.launch {
            coRunCatching {
                articleInteractor.likeComment(commentId)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun openProfile(comment: Comment) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${comment.user.id}")
    }

}
