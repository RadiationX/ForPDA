package forpdateam.ru.forpda.model.repository.news

import android.util.Log
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getNews(category: String, pageNumber: Int): List<NewsItem> {
        val news = newsApi.getNews(category, pageNumber)
        return news.map {
            val forumUser = forumUsersCache.getUserById(it.authorId)
            Log.e(
                "kekosina",
                "forumUser ${it.authorId}, ${forumUser?.id}, ${forumUser?.nick}, ${forumUser?.avatar}"
            )
            if (forumUser != null) {
                it.copy(avatar = forumUser.avatar?.asDeferredData())
            } else {
                it
            }
        }
    }

    suspend fun likeComment(articleId: Int, commentId: Int): Boolean {
        return newsApi.likeComment(articleId, commentId)
    }

    suspend fun sendPoll(from: String, pollId: Int, answersId: IntArray): DetailsPage {
        return newsApi.sendPoll(from, pollId, answersId)
    }

    suspend fun replyComment(articleId: Int, commentId: Int, comment: String): DetailsPage {
        return newsApi.replyComment(articleId, commentId, comment)
    }

    suspend fun getDetails(id: Int): DetailsPage {
        return newsApi.getDetails(id)
    }

    suspend fun getDetails(url: String): DetailsPage {
        return newsApi.getDetails(url)
    }

    suspend fun getComments(article: DetailsPage): List<Comment> {
        return newsApi.parseComments(article.karmaMap, article.commentsSource)
    }

}
