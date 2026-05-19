package forpdateam.ru.forpda.model.data.remote.api.news

import android.util.SparseArray
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.16.
 */
class NewsApi @Inject constructor(
    private val webClient: WebClient,
    private val articleParser: ArticleParser
) {

    suspend fun getNews(pageNumber: Int): List<NewsItem> {
        val response = webClient.get("https://4pda.to/page/${pageNumber}/")
        return articleParser.parseArticles(response.body)
    }

    suspend fun getDetails(id: Int): DetailsPage {
        val response = webClient.get("https://4pda.to/index.php?p=$id")
        return articleParser.parseArticle(response.body)
    }

    suspend fun getDetails(url: String): DetailsPage {
        val response = webClient.get(url)
        return articleParser.parseArticle(response.body)
    }

    suspend fun sendPoll(from: String, pollId: Int, answersId: IntArray): DetailsPage {
        val url = "https://4pda.to/pages/poll/?act=vote&poll_id=$pollId"
        val rBuilder = NetworkRequest.Builder()
            .url(url)
            .multipart()
            .xhrHeader()
            .formHeader("from", from)
            .apply {
                answersId.forEach {
                    formHeader("answer[]", it.toString())
                }
            }

        val response = webClient.request(rBuilder.build())
        return articleParser.parseArticle(response.body)
    }

    suspend fun likeComment(articleId: Int, commentId: Int): Boolean {
        val url = "https://4pda.to/pages/karma?p=$articleId&c=$commentId&v=1"
        webClient.request(NetworkRequest.Builder().url(url).xhrHeader().build())
        return true
    }

    suspend fun parseComments(karmaMap: SparseArray<Comment.Karma>, source: String?): List<Comment> {
        return articleParser.parseComments(karmaMap, source)
    }

    suspend fun replyComment(articleId: Int, commentId: Int, text: String): DetailsPage {
        var comment = text
        try {
            comment = URLEncoder.encode(comment, "Windows-1251")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/wp-comments-post.php")
            .formHeader("comment_post_ID", articleId.toString())
            .formHeader("comment_reply_ID", commentId.toString())
            .formHeader("comment_reply_dp", if (commentId == 0) "0" else "1")
            .formHeader("comment", comment, true)
        val response = webClient.request(builder.build())
        return articleParser.parseArticle(response.body)
    }
}
