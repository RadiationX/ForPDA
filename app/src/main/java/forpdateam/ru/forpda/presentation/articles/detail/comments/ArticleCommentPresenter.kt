package forpdateam.ru.forpda.presentation.articles.detail.comments

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.interactors.news.ArticleInteractor
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import java.util.*

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticleCommentPresenter(
        private val articleInteractor: ArticleInteractor,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val authHolder: AuthHolder,
        private val errorHandler: IErrorHandler
) : BasePresenter<ArticleCommentView>() {

    private var firstShow: Boolean = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        articleInteractor
                .observeComments()
                .map { commentsToList(it) }
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showComments(it)
                    if (firstShow) {
                        val targetCommentId = articleInteractor.initData.commentId
                        val index = it.indexOfFirst { it.id == targetCommentId }
                        viewState.scrollToComment(index)
                        firstShow = false
                    }
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()

        authHolder
                .observe()
                .subscribe {
                    viewState.setMessageFieldVisible(it.isAuth())
                }
                .untilDestroy()
    }

    fun updateComments() {
        articleInteractor
                .loadArticle()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun replyComment(commentId: Int, text: String) {
        articleInteractor
                .replyComment(commentId, text)
                .doOnSubscribe { viewState.setSendRefreshing(true) }
                .doAfterTerminate { viewState.setSendRefreshing(false) }
                .subscribe({
                    viewState.onReplyComment()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun likeComment(commentId: Int) {
        articleInteractor
                .likeComment(commentId)
                .subscribe({}, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun commentsToList(comment: Comment): ArrayList<Comment> {
        val comments = ArrayList<Comment>()
        recurseCommentsToList(comments, comment)
        return comments
    }


    fun recurseCommentsToList(comments: ArrayList<Comment>, comment: Comment) {
        for (child in comment.children) {
            comments.add(Comment(child))
            recurseCommentsToList(comments, child)
        }
    }

    fun openProfile(comment: Comment) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${comment.userId}", router);
    }

}
