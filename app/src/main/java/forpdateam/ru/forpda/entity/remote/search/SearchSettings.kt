package forpdateam.ru.forpda.entity.remote.search

import android.net.Uri
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import java.util.regex.Pattern

/**
 * Created by radiationx on 01.02.17.
 */
class SearchSettings {
    var resourceType: String
    var result: String?
    var sort: String?
    var source: String?
    private var query = ""
    private var nick = ""
    var subforums: String?
    var excludeTrash: Int = 0
    var st: Int = 0
    private val forums: MutableList<String?>
    private val topics: MutableList<String?>

    init {
        resourceType = RESOURCE_FORUM.first
        result = RESULT_TOPICS.first
        sort = SORT_DD.first
        source = SOURCE_TITLES.first
        subforums = SUB_FORUMS_TRUE
        forums = ArrayList()
        topics = ArrayList()
    }


    fun getQuery(): String? {
        return query
    }

    fun setQuery(query: String) {
        this.query = query
    }

    fun getNick(): String? {
        return nick
    }

    fun setNick(nick: String) {
        this.nick = nick
    }

    fun getForums(): List<String?> {
        return forums
    }

    fun addForum(forum: String?) {
        forums.add(forum)
    }

    fun getTopics(): List<String?> {
        return topics
    }

    fun addTopic(topic: String?) {
        topics.add(topic)
    }

    fun toUrl(): String {
        return Companion.toUrl(this)
    }

    companion object {
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
            return parseSettings(SearchSettings(), url)
        }

        fun parseSettings(settings: SearchSettings, url: String): SearchSettings {
            val matcher = argsPattern.matcher(url)
            var name: String
            var value: String?
            while (matcher.find()) {
                name = matcher.group(1).lowercase(Locale.getDefault())
                value = matcher.group(2)
                when (name) {
                    ARG_ST -> settings.st = value.toInt()
                    ARG_RESULT -> settings.result = value
                    ARG_SORT -> settings.sort = value
                    ARG_SOURCE -> settings.source = value
                    ARG_QUERY_FORUM -> {
                        settings.resourceType = RESOURCE_FORUM.first
                        try {
                            settings.setQuery(URLDecoder.decode(value, "windows-1251"))
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }

                    ARG_QUERY_NEWS -> {
                        settings.resourceType = RESOURCE_NEWS.first
                        try {
                            settings.setQuery(URLDecoder.decode(value, "windows-1251"))
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }

                    ARG_NICK -> try {
                        settings.setNick(URLDecoder.decode(value, "windows-1251"))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                    ARG_SUB_FORUMS -> settings.subforums = value
                    ARG_EXCLUDE_TRASH -> settings.excludeTrash =
                        value.toInt()
                }

                if (name == ARG_FORUMS || name == ARG_FORUMS_SIMPLE) {
                    try {
                        settings.addForum(value)
                    } catch (ignore: NumberFormatException) {
                    }
                }
                if (name == ARG_TOPICS || name == ARG_TOPICS_SIMPLE) {
                    try {
                        settings.addTopic(value)
                    } catch (ignore: NumberFormatException) {
                    }
                }
            }
            return settings
        }

        fun toUrl(settings: SearchSettings): String {
            val builder = Uri.Builder()
            builder.scheme("https")
                .authority("4pda.to")
            if (settings.resourceType == RESOURCE_NEWS.first) {
                builder.appendPath("page")
                builder.appendPath(settings.st.toString())
                try {
                    builder.appendQueryParameter(
                        ARG_QUERY_NEWS,
                        URLEncoder.encode(settings.getQuery(), "windows-1251")
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            } else {
                builder.appendPath("forum")
                builder.appendQueryParameter("act", "search")
                builder.appendQueryParameter(ARG_RESULT, settings.result)
                builder.appendQueryParameter(ARG_SORT, settings.sort)
                builder.appendQueryParameter(ARG_SOURCE, settings.source)
                if (settings.getQuery() != null && !settings.getQuery()!!.isEmpty()) {
                    try {
                        builder.appendQueryParameter(
                            ARG_QUERY_FORUM,
                            URLEncoder.encode(settings.getQuery(), "windows-1251")
                        )
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
                if (settings.getNick() != null && !settings.getNick()!!.isEmpty()) {
                    try {
                        builder.appendQueryParameter(
                            ARG_NICK,
                            URLEncoder.encode(settings.getNick(), "windows-1251")
                        )
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }

                for (forum in settings.getForums()) builder.appendQueryParameter(ARG_FORUMS, forum)

                for (topic in settings.getTopics()) builder.appendQueryParameter(ARG_TOPICS, topic)

                if (settings.subforums != null) {
                    builder.appendQueryParameter(
                        ARG_SUB_FORUMS,
                        settings.subforums
                    )
                }
                builder.appendQueryParameter(ARG_NO_FORM, "1")
                builder.appendQueryParameter(ARG_ST, settings.st.toString())
                builder.appendQueryParameter(ARG_EXCLUDE_TRASH, settings.excludeTrash.toString())
            }

            val url = builder.build().toString()
            try {
                return URLDecoder.decode(url, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return url
        }
    }
}
