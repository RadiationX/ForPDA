package forpdateam.ru.forpda.entity.remote.theme

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed interface TopicUrl : Parcelable {

    @Parcelize
    data class FindPost(
        val postId: Int,
        val anchor: Anchor?
    ) : TopicUrl

    sealed interface ShowTopic : TopicUrl {

        val topicId: Int
        val showPollResults: Boolean

        @Parcelize
        data class Page(
            override val topicId: Int,
            override val showPollResults: Boolean,
            val st: Int,
            val anchor: Anchor?
        ) : ShowTopic

        @Parcelize
        data class FindPost(
            override val topicId: Int,
            override val showPollResults: Boolean,
            val postId: Int,
            val anchor: Anchor?
        ) : ShowTopic

        @Parcelize
        data class GetNewPost(
            override val topicId: Int,
            override val showPollResults: Boolean,
        ) : ShowTopic

        @Parcelize
        data class GetLastPost(
            override val topicId: Int,
            override val showPollResults: Boolean,
        ) : ShowTopic
    }

    sealed interface Anchor : Parcelable {

        val postId: Int
        val value: String

        @Parcelize
        data class Post(
            override val postId: Int,
        ) : Anchor {

            override val value: String
                get() = "entry$postId"
        }

        @Parcelize
        data class Node(
            val name: String,
            override val postId: Int,
            val number: Int,
        ) : Anchor {

            override val value: String
                get() = "$name-$postId-$number"
        }
    }

    fun toHttpUrl(): HttpUrl {
        return toUrl(this)
    }


    companion object {

        /*
        * https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=Spoil-115499851-1 - копирование спойлера
        * https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=entry115493099 - копирование спойлера
        * https://4pda.to/forum/index.php?act=findpost&pid=115493099 - упоминание например
        * https://4pda.to/forum/index.php?showtopic=1045802&anchor=entry115493099#Spoil-115499851-1
        * https://4pda.to/forum/index.php?showtopic=1045802#entry115493099 - после findpost&pid=
        * https://4pda.to/forum/index.php?showtopic=1045802#Spoil-115499851-1 - после findpost&pid=
        * https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=114328645 - копирование ссылки на пост
        * https://4pda.to/forum/index.php?showtopic=1045802&mode=show&st=0 - показать результаты опроса
        * https://4pda.to/forum/index.php?showtopic=208182&view=getnewpost
        * https://4pda.to/forum/index.php?showtopic=208182&view=getlastpost#Spoil-115499851-1
        *
        *
        * */

        private val entryRegex = Regex("entry(\\d+)")
        private val nodeRegex = Regex("(\\w+)-(\\d+)-(\\d+)")


        fun toUrl(args: TopicUrl): HttpUrl {
            return HttpUrl.Builder().apply {
                scheme("https")
                host("4pda.to")
                addPathSegment("forum")
                addPathSegment("index.php")
                when (args) {
                    is FindPost -> {
                        addQueryParameter("act", "findpost")
                        addQueryParameter("pid", args.postId.toString())
                        fragment(args.anchor?.value)
                    }

                    is ShowTopic -> {
                        addQueryParameter("showtopic", args.topicId.toString())
                        if (args.showPollResults) {
                            addQueryParameter("mode", "show")
                        }
                        when (args) {
                            is ShowTopic.Page -> {
                                addQueryParameter("st", args.st.toString())
                                fragment(args.anchor?.value)
                            }

                            is ShowTopic.FindPost -> {
                                addQueryParameter("view", "findpost")
                                addQueryParameter("p", args.postId.toString())
                                fragment(args.anchor?.value)
                            }

                            is ShowTopic.GetLastPost -> {
                                addQueryParameter("view", "getlastpost")
                            }

                            is ShowTopic.GetNewPost -> {
                                addQueryParameter("view", "getnewpost")
                            }
                        }
                    }
                }
            }.build()
        }

        fun fromUrl(url: String): TopicUrl? {
            val httpUrl = url.toHttpUrlOrNull() ?: return null
            if (httpUrl.host != "4pda.to") {
                return null
            }

            if (httpUrl.queryParameter("act") == "findpost") {
                val postId = httpUrl.queryParameter("pid")?.toIntOrNull() ?: return null
                val anchor = parseAnchor(httpUrl)
                return FindPost(postId = postId, anchor = anchor)
            }

            return parseView(httpUrl)
        }

        private fun parseView(httpUrl: HttpUrl): ShowTopic? {
            val topicId = httpUrl.queryParameter("showtopic")?.toIntOrNull() ?: return null
            val showPollResults = httpUrl.queryParameter("mode") == "show"
            val queryView = httpUrl.queryParameter("view")
            val st = httpUrl.queryParameter("st")?.toIntOrNull() ?: 0
            val anchor = parseAnchor(httpUrl)

            val topicByView = when (queryView) {
                "getnewpost" -> {
                    ShowTopic.GetNewPost(
                        topicId = topicId,
                        showPollResults = showPollResults
                    )
                }

                "getlastpost" -> {
                    ShowTopic.GetLastPost(
                        topicId = topicId,
                        showPollResults = showPollResults
                    )
                }

                "findpost" -> {
                    httpUrl.queryParameter("p")?.toIntOrNull()?.let { postId ->
                        ShowTopic.FindPost(
                            topicId = topicId,
                            showPollResults = showPollResults,
                            postId = postId,
                            anchor = anchor
                        )
                    }
                }

                else -> {
                    null
                }
            }
            if (topicByView != null) {
                return topicByView
            }
            return ShowTopic.Page(
                topicId = topicId,
                showPollResults = showPollResults,
                st = st,
                anchor = anchor
            )
        }

        private fun parseAnchor(httpUrl: HttpUrl): Anchor? {
            val fragmentAnchor = httpUrl.fragment?.let { parseAnchor(it) }
            if (fragmentAnchor != null) return fragmentAnchor
            return httpUrl.queryParameter("anchor")?.let { parseAnchor(it) }
        }

        private fun parseAnchor(value: String): Anchor? {
            entryRegex.find(value)?.also {
                val postId = it.groupValues[1].toIntOrNull() ?: return@also
                return Anchor.Post(postId = postId)
            }
            nodeRegex.find(value)?.also {
                val name = it.groupValues[1]
                val postId = it.groupValues[2].toIntOrNull() ?: return@also
                val number = it.groupValues[3].toIntOrNull() ?: return@also
                return Anchor.Node(name = name, postId = postId, number = number)
            }
            return null
        }
    }
}
