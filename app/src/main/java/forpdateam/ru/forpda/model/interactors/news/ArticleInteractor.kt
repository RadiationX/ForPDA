package forpdateam.ru.forpda.model.interactors.news

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.extensions.replace
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.presentation.articles.detail.ArticleTemplate
import io.reactivex.Observable
import io.reactivex.Single

class ArticleInteractor(
    val initData: InitData,
    private val newsRepository: NewsRepository,
    private val articleTemplate: ArticleTemplate
) {

    private val dataRelay = BehaviorRelay.create<DetailsPage>()
    private val commentsRelay = BehaviorRelay.create<List<Comment>>()

    fun observeData(): Observable<DetailsPage> = dataRelay
    fun observeComments(): Observable<List<Comment>> = commentsRelay

    fun loadArticle(): Single<DetailsPage> = Single
        .defer {
            if (initData.newsId > 0) {
                newsRepository.getDetails(initData.newsId)
            } else {
                newsRepository.getDetails(initData.newsUrl.orEmpty())
            }
        }
        .map { articleTemplate.mapEntity(it) }
        .doOnSuccess { updateData(it) }


    fun likeComment(commentId: Int) = newsRepository
        .likeComment(initData.newsId, commentId)
        .doOnSubscribe {
            updateComments { comments ->
                comments.replace(
                    condition = { it.id == commentId },
                    map = {
                        val karma = it.karma
                        it.copy(
                            karma = karma?.copy(
                                status = Comment.Karma.LIKED,
                                count = karma.count + 1
                            )
                        )
                    }
                )
            }
        }

    fun sendPoll(from: String, pollId: Int, answersId: IntArray) = newsRepository
        .sendPoll(from, pollId, answersId)

    fun replyComment(commentId: Int, comment: String): Single<DetailsPage> = newsRepository
        .replyComment(initData.newsId, commentId, comment)
        .map { articleTemplate.mapEntity(it) }
        .doOnSuccess { updateData(it) }

    private fun updateData(article: DetailsPage) {
        initData.newsId = article.id
        dataRelay.accept(article)
        parseComments(article)
    }

    private fun updateComments(block: (List<Comment>) -> List<Comment>) {
        commentsRelay.value?.also { comments ->
            commentsRelay.accept(block.invoke(comments))
        }
    }

    private fun parseComments(article: DetailsPage) {
        newsRepository
            .getComments(article)
            .subscribe({
                commentsRelay.accept(it)
            }, {
                it.printStackTrace()
            })
    }


    data class InitData(
        var newsUrl: String? = null,
        var newsId: Int = -1,
        var commentId: Int = -1
    )
}