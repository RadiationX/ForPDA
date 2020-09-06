package forpdateam.ru.forpda.model.data.remote.api.news

import android.util.SparseArray
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.entity.remote.news.DetailsPage
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.news.Constants.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Created by radiationx on 31.07.16.
 */
class NewsApi(
        private val webClient: IWebClient,
        private val articleParser: ArticleParser
) {

    fun getNews(category: String, pageNumber: Int): List<NewsItem> {
        val url = getLink(category, pageNumber)
        val response = webClient.get(url)
        return articleParser.parseArticles(response.body)
    }

    fun getDetails(id: Int): DetailsPage {
        val response = webClient.get("https://4pda.ru/index.php?p=$id")
        return articleParser.parseArticle(response.body)
    }

    fun getDetails(url: String): DetailsPage {
        val response = webClient.get(url)
        return articleParser.parseArticle(response.body)
    }

    fun sendPoll(from: String, pollId: Int, answersId: IntArray): DetailsPage {
        val url = "https://4pda.ru/pages/poll/?act=vote&poll_id=$pollId"
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

    fun likeComment(articleId: Int, commentId: Int): Boolean {
        val url = "https://4pda.ru/pages/karma?p=$articleId&c=$commentId&v=1"
        webClient.request(NetworkRequest.Builder().url(url).xhrHeader().build())
        return true
    }

    fun parseComments(karmaMap: SparseArray<Comment.Karma>, source: String?): Comment {
        return articleParser.parseComments(karmaMap, source)
    }

    fun replyComment(articleId: Int, commentId: Int, text: String): DetailsPage {
        var comment = text
        try {
            comment = URLEncoder.encode(comment, "Windows-1251")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/wp-comments-post.php")
                .formHeader("comment_post_ID", articleId.toString())
                .formHeader("comment_reply_ID", commentId.toString())
                .formHeader("comment_reply_dp", if (commentId == 0) "0" else "1")
                .formHeader("comment", comment, true)
        val response = webClient.request(builder.build())
        return articleParser.parseArticle(response.body)
    }


    private fun getLink(category: String?, pageNumber: Int): String {
        var link = getUrlCategory(category)
        if (pageNumber >= 2) {
            link = link + "page/" + pageNumber + "/"
        }
        return link
    }

    private fun getUrlCategory(category: String?): String {
        if (category == null) return NEWS_URL_ROOT
        when (category) {
            NEWS_CATEGORY_ROOT -> return NEWS_URL_ROOT
            NEWS_CATEGORY_ALL -> return NEWS_URL_ALL
            NEWS_CATEGORY_ARTICLES -> return NEWS_URL_ARTICLES
            NEWS_CATEGORY_REVIEWS -> return NEWS_URL_REVIEWS
            NEWS_CATEGORY_SOFTWARE -> return NEWS_URL_SOFTWARE
            NEWS_CATEGORY_GAMES -> return NEWS_URL_GAMES
            NEWS_SUBCATEGORY_DEVSTORY_GAMES -> return NEWS_URL_DEVSTORY_GAMES
            NEWS_SUBCATEGORY_WP7_GAME -> return NEWS_URL_WP7_GAME
            NEWS_SUBCATEGORY_IOS_GAME -> return NEWS_URL_IOS_GAME
            NEWS_SUBCATEGORY_ANDROID_GAME -> return NEWS_URL_ANDROID_GAME
            NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE -> return NEWS_URL_DEVSTORY_SOFTWARE
            NEWS_SUBCATEGORY_WP7_SOFTWARE -> return NEWS_URL_WP7_SOFTWARE
            NEWS_SUBCATEGORY_IOS_SOFTWARE -> return NEWS_URL_IOS_SOFTWARE
            NEWS_SUBCATEGORY_ANDROID_SOFTWARE -> return NEWS_URL_ANDROID_SOFTWARE
            NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS -> return NEWS_URL_SMARTPHONES_REVIEWS
            NEWS_SUBCATEGORY_TABLETS_REVIEWS -> return NEWS_URL_TABLETS_REVIEWS
            NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS -> return NEWS_URL_SMART_WATCH_REVIEWS
            NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS -> return NEWS_URL_ACCESSORIES_REVIEWS
            NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS -> return NEWS_URL_NOTEBOOKS_REVIEWS
            NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS -> return NEWS_URL_ACOUSTICS_REVIEWS
            NEWS_SUBCATEGORY_HOW_TO_ANDROID -> return NEWS_URL_HOW_TO_ANDROID
            NEWS_SUBCATEGORY_HOW_TO_IOS -> return NEWS_URL_HOW_TO_IOS
            NEWS_SUBCATEGORY_HOW_TO_WP -> return NEWS_URL_HOW_TO_WP
            NEWS_SUBCATEGORY_HOW_TO_INTERVIEW -> return NEWS_URL_HOW_TO_INTERVIEW
        }
        return NEWS_URL_ALL
    }
}
