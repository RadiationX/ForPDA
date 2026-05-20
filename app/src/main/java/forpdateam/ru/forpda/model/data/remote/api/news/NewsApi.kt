package forpdateam.ru.forpda.model.data.remote.api.news

import android.util.SparseArray
import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.16.
 */
class NewsApi @Inject constructor(
    private val webClient: WebClient,
    private val articleParser: ArticleParser
) {

    suspend fun getNews(pageNumber: Int): List<NewsItem> {
        val response = webClient.request(ApiRequest.Site.GetArticles(pageNumber))
        return articleParser.parseArticles(response.body)
    }

    suspend fun getDetails(id: Int): DetailsPage {
        val response = webClient.request(ApiRequest.Site.GetArticle(id))
        return articleParser.parseArticle(response.body)
    }

    suspend fun sendPoll(from: String, pollId: Int, answersId: IntArray): DetailsPage {
        val response = webClient.request(ApiRequest.Site.SendPoll(pollId, answersId.toList(), from))
        return articleParser.parseArticle(response.body)
    }

    suspend fun likeComment(articleId: Int, commentId: Int): Boolean {
        webClient.request(ApiRequest.Site.LikeComment(articleId, commentId))
        return true
    }

    suspend fun parseComments(karmaMap: SparseArray<Comment.Karma>, source: String?): List<Comment> {
        return articleParser.parseComments(karmaMap, source)
    }

    suspend fun replyComment(articleId: Int, commentId: Int, text: String): DetailsPage {
        val response = webClient.request(ApiRequest.Site.SendComment(articleId, commentId, text))
        return articleParser.parseArticle(response.body)
    }
}
