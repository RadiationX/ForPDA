package forpdateam.ru.forpda.model.repository.news

import android.util.Log
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
import ru.radiationx.coretypes.ArticleAnswerId
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.ArticlePollId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.PageNumber
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getNews(pageNumber: PageNumber): List<NewsItem> {
        val news = newsApi.getNews(pageNumber)
        return news.map {
            val forumUser = forumUsersCache.getUserById(it.author.id)
            Log.e(
                "kekosina",
                "forumUser ${it.author.id}, ${forumUser?.id}, ${forumUser?.nick}, ${forumUser?.avatar}"
            )
            if (forumUser != null) {
                it.copy(avatar = forumUser.avatar?.asDeferredData())
            } else {
                it
            }
        }
    }

    suspend fun likeComment(articleId: ArticleId, commentId: CommentId): Boolean {
        return newsApi.likeComment(articleId, commentId)
    }

    suspend fun sendPoll(from: String, pollId: ArticlePollId, answersIds: List<ArticleAnswerId>): DetailsPage {
        return newsApi.sendPoll(from, pollId, answersIds)
    }

    suspend fun replyComment(articleId: ArticleId, commentId: CommentId?, comment: String): DetailsPage {
        return newsApi.replyComment(articleId, commentId, comment)
    }

    suspend fun getDetails(id: ArticleId): DetailsPage {
        return newsApi.getDetails(id)
    }

    suspend fun getComments(article: DetailsPage): List<Comment> {
        return newsApi.parseComments(article.karmaMap, article.commentsSource)
    }

}
