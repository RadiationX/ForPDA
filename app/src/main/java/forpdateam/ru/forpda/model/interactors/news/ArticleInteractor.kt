package forpdateam.ru.forpda.model.interactors.news

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
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
    private val commentsRelay = BehaviorRelay.create<Comment>()

    fun observeData(): Observable<DetailsPage> = dataRelay
    fun observeComments(): Observable<Comment> = commentsRelay

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

    fun sendPoll(from: String, pollId: Int, answersId: IntArray) = newsRepository
            .sendPoll(from, pollId, answersId)

    fun replyComment(commentId: Int, comment: String): Single<DetailsPage> = newsRepository
            .replyComment(initData.newsId, commentId, comment)
            .map { articleTemplate.mapEntity(it) }
            .doOnSuccess { updateData(it) }

    private fun updateData(article: DetailsPage) {
        initData.newsId = article.id
        article.commentId = initData.commentId
        dataRelay.accept(article)
        parseComments(article)
    }

    private fun parseComments(article: DetailsPage) {
        newsRepository
                .getComments(article)
                .subscribe({
                    if (dataRelay.hasValue()) {
                        dataRelay.value?.commentTree = it
                    }
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