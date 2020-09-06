package forpdateam.ru.forpda.model.repository.news

import android.util.Log
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class NewsRepository(
        private val schedulers: SchedulersProvider,
        private val newsApi: NewsApi,
        private val forumUsersCache: ForumUsersCache
) : BaseRepository(schedulers) {

    fun getNews(category: String, pageNumber: Int): Single<List<NewsItem>> = Single
            .fromCallable { newsApi.getNews(category, pageNumber) }
            .doOnSuccess { data ->
                data.forEach {
                    val forumUser = forumUsersCache.getUserById(it.authorId)
                    Log.e("kekosina", "forumUser ${it.authorId}, ${forumUser?.id}, ${forumUser?.nick}, ${forumUser?.avatar}")
                    if (forumUser != null) {
                        it.avatar = forumUser.avatar
                    }/* else {
                        forumUsersCache.saveUser(ForumUser().apply {
                            id = it.authorId
                            nick = it.author
                        })
                    }*/
                }
            }
            .runInIoToUi()

    fun likeComment(articleId: Int, commentId: Int): Single<Boolean> = Single
            .fromCallable { newsApi.likeComment(articleId, commentId) }
            .runInIoToUi()

    fun sendPoll(from: String, pollId: Int, answersId: IntArray): Single<DetailsPage> = Single
            .fromCallable { newsApi.sendPoll(from, pollId, answersId) }
            .runInIoToUi()

    fun replyComment(articleId: Int, commentId: Int, comment: String): Single<DetailsPage> = Single
            .fromCallable { newsApi.replyComment(articleId, commentId, comment) }
            .runInIoToUi()

    fun getDetails(id: Int): Single<DetailsPage> = Single
            .fromCallable { newsApi.getDetails(id) }
            .runInIoToUi()

    fun getDetails(url: String): Single<DetailsPage> = Single
            .fromCallable { newsApi.getDetails(url) }
            .runInIoToUi()

    fun getComments(article: DetailsPage): Single<Comment> = Single
            .fromCallable { newsApi.parseComments(article.karmaMap, article.commentsSource) }
            .runInIoToUi()

}
