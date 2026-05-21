package forpdateam.ru.forpda.entity.remote.search

import android.os.Parcelable
import forpdateam.ru.forpda.common.ApiRequest
import kotlinx.parcelize.Parcelize
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.Locale
import java.util.regex.Pattern

/**
 * Created by radiationx on 01.02.17.
 */
@Parcelize
data class SearchSettings(
    val resourceType: String,
    val result: String?,
    val sort: String?,
    val source: String?,
    val query: String?,
    val nick: String?,
    val subforums: String?,
    val excludeTrash: Int,
    val st: Int,
    val forums: List<Int>,
    val topics: List<Int>
) : Parcelable {

    fun toUrl(): String {
        return Companion.toUrl(this)
    }

    private class Builder(
        var resourceType: String = RESOURCE_FORUM.first,
        var result: String? = RESULT_TOPICS.first,
        var sort: String? = SORT_DD.first,
        var source: String? = SOURCE_TITLES.first,
        var query: String? = null,
        var nick: String? = null,
        var subforums: String? = SUB_FORUMS_TRUE,
        var excludeTrash: Int = 0,
        var st: Int = 0,
        var forums: MutableList<Int> = mutableListOf(),
        var topics: MutableList<Int> = mutableListOf()
    ) {

        fun build(): SearchSettings {
            return SearchSettings(
                resourceType = resourceType,
                result = result,
                sort = sort,
                source = source,
                query = query,
                nick = nick,
                subforums = subforums,
                excludeTrash = excludeTrash,
                st = st,
                forums = forums,
                topics = topics
            )
        }
    }

    companion object {

        private val _default by lazy { Builder().build() }
        fun default(): SearchSettings = _default

        private val argsPattern: Pattern =
            Pattern.compile("(?:\\?|\\&)([^=]*?)=([\\s\\S]*?)(?=&| |$)")
        val RESOURCE_NEWS: Pair<String, String> = Pair("news", "Новости")
        val RESOURCE_FORUM: Pair<String, String> = Pair("forum", "Форум")

        const val ARG_RESULT: String = "result"
        const val ARG_SORT: String = "sort"
        const val ARG_SOURCE: String = "source"
        const val ARG_QUERY_FORUM: String = "query"
        const val ARG_QUERY_NEWS: String = "s"
        const val ARG_NICK: String = "username"
        const val ARG_FORUMS_SIMPLE: String = "forums"
        const val ARG_TOPICS_SIMPLE: String = "topics"
        const val ARG_FORUMS: String = "forums%5b%5d"
        const val ARG_TOPICS: String = "topics%5b%5d"
        const val ARG_SUB_FORUMS: String = "subforums"
        const val ARG_NO_FORM: String = "noform"
        const val ARG_ST: String = "st"
        const val ARG_USER_ID: String = "username-id"
        const val ARG_EXCLUDE_TRASH: String = "exclude_trash"

        val RESULT_TOPICS: Pair<String, String> = Pair("topics", "Темы")
        val RESULT_POSTS: Pair<String, String> = Pair("posts", "Сообщения")

        val SORT_DA: Pair<String, String> = Pair("da", "Возрастание даты")
        val SORT_DD: Pair<String, String> = Pair("dd", "Убывание даты")
        val SORT_REL: Pair<String, String> = Pair("rel", "Соответствие")

        val SOURCE_ALL: Pair<String, String> = Pair("all", "Везде")
        val SOURCE_TITLES: Pair<String, String> = Pair("top", "Заголовки")
        val SOURCE_CONTENT: Pair<String, String> = Pair("pst", "Содержание")

        const val SUB_FORUMS_TRUE: String = "1"
        const val SUB_FORUMS_FALSE: String = "0"

        fun parseSettings(url: String): SearchSettings {
            val builder = Builder()
            val matcher = argsPattern.matcher(url)
            while (matcher.find()) {
                val name = matcher.group(1).lowercase(Locale.getDefault())
                val value = matcher.group(2)
                when (name) {
                    ARG_ST -> builder.st = value.toInt()
                    ARG_RESULT -> builder.result = value
                    ARG_SORT -> builder.sort = value
                    ARG_SOURCE -> builder.source = value
                    ARG_QUERY_FORUM -> {
                        builder.resourceType = RESOURCE_FORUM.first
                        try {
                            builder.query = URLDecoder.decode(value, "windows-1251")
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }

                    ARG_QUERY_NEWS -> {
                        builder.resourceType = RESOURCE_NEWS.first
                        try {
                            builder.query = URLDecoder.decode(value, "windows-1251")
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }

                    ARG_NICK -> try {
                        builder.nick = URLDecoder.decode(value, "windows-1251")
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                    ARG_SUB_FORUMS -> builder.subforums = value
                    ARG_EXCLUDE_TRASH -> builder.excludeTrash = value.toInt()
                }

                if (name == ARG_FORUMS || name == ARG_FORUMS_SIMPLE) {
                    try {
                        builder.forums.add(value.toInt())
                    } catch (ignore: NumberFormatException) {
                    }
                }
                if (name == ARG_TOPICS || name == ARG_TOPICS_SIMPLE) {
                    try {
                        builder.topics.add(value.toInt())
                    } catch (ignore: NumberFormatException) {
                    }
                }
            }
            return builder.build()
        }

        fun toUrl(settings: SearchSettings): String {
            return ApiRequest.Search(settings).buildHttpUrl().toString()
        }
    }
}
