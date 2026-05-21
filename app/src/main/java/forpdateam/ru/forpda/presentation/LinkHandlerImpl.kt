package forpdateam.ru.forpda.presentation

import android.net.Uri
import android.util.Log
import forpdateam.ru.forpda.common.MimeTypeUtil
import forpdateam.ru.forpda.entity.remote.reputation.RepArgs
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.TopicUrl
import forpdateam.ru.forpda.model.data.remote.api.common.LinkHandlerParser
import java.net.URLDecoder
import java.util.Locale
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class LinkHandlerImpl @Inject constructor(
    private val systemLinkHandler: SystemLinkHandler,
    private val router: TabRouter,
    private val linkHandlerParser: LinkHandlerParser
) : LinkHandler {

    companion object {
        const val LOG_TAG = "LinkHandler"
    }

    private fun handleDownload(url: String, name: String? = null) {
        systemLinkHandler.handleDownload(url, name)
    }

    private fun externalIntent(url: String) {
        systemLinkHandler.handle(url)
    }

    private fun navigateTo(screen: Screen, router: TabRouter?, args: Map<String, String?>) {
        router?.navigateTo(screen.apply {
            args[Screen.ARG_TITLE]?.let { screen.screenTitle = it }
            args[Screen.ARG_SUBTITLE]?.let { screen.screenSubTitle = it }
        })
    }

    override fun handle(inputUrl: String?, router: TabRouter?): Boolean {
        return handle(inputUrl, router, emptyMap())
    }

    override fun handle(inputUrl: String?, router: TabRouter?, args: Map<String, String?>): Boolean {
        var someRouter = router ?: this.router
        var url = inputUrl.orEmpty()
        if (url.isBlank() || url == "#") {
            return false
        }
        if (url.substring(0, 2) == "//") {
            url = "https:$url"
        } else if (url.substring(0, 1) == "/") {
            url = "https://4pda.to$url"
        }
        url = url.replace("&amp;", "&").replace("\"", "").trim()
        Log.d(LOG_TAG, "Corrected url $url")


        if (handleMedia(url, someRouter, args)) {
            return true
        }
        url = normalizeForumUrl(url)

        if (linkHandlerParser.basicMatches(url)) {
            val uri = Uri.parse(url.lowercase(Locale.getDefault()))
            Log.d(LOG_TAG, "Compare uri/url " + uri.toString() + " : " + url)

            if (!uri.pathSegments.isEmpty()) {
                when (uri.pathSegments[0]) {
                    "pages" -> if (handlePages(uri, someRouter, args)) {
                        return true
                    }

                    "forum" -> if (handleForum(uri, someRouter, args)) {
                        return true
                    }

                    "devdb" -> if (handleDevDb(uri, someRouter, args)) {
                        return true
                    }

                    else -> if (handleSite(uri, someRouter, args)) {
                        return true
                    }
                }
            } else {
                if (handleSite(uri, someRouter, args)) {
                    return true
                }
            }

        }

        externalIntent(url)

        return false
    }

    override fun findScreen(url: String): String? {
        return null
    }

    private fun handleForum(uri: Uri, router: TabRouter?, args: Map<String, String?>): Boolean {
        TopicUrl.fromUrl(uri.toString())?.also {
            navigateTo(Screen.Theme(topicUrl = it), router, args)
            return true
        }
        uri.getQueryParameter("showuser")?.also { param ->
            navigateTo(Screen.Profile(param.toInt()), router, args)
            return true
        }

        uri.getQueryParameter("showforum")?.also { param ->
            navigateTo(Screen.Topics(forumId = param.toInt()), router, args)
            return true
        }

        uri.getQueryParameter("act")?.also { param ->
            when (param) {
                "idx" -> {
                    navigateTo(Screen.Forum(), router, args)
                }

                "qms" -> {
                    val qmsUserId = uri.getQueryParameter("mid")
                    val qmsThemeId = uri.getQueryParameter("t")

                    if (qmsUserId == null) {
                        navigateTo(Screen.QmsContacts(), router, args)
                    } else {
                        if (qmsThemeId != null) {
                            navigateTo(
                                Screen.QmsChat.FromLink(
                                    userId = qmsUserId.toInt(),
                                    themeId = qmsThemeId.toInt()
                                ), router, args
                            )
                        } else {
                            navigateTo(Screen.QmsThemes(userId = qmsUserId.toInt()), router, args)
                        }
                    }
                    return true
                }

                "boardrules" -> {
                    navigateTo(Screen.ForumRules(), router, args)
                    return true
                }

                "announce" -> {
                    navigateTo(
                        Screen.Announce(
                            forumId = requireNotNull(uri.getQueryParameter("f")).toInt(),
                            announceId = requireNotNull(uri.getQueryParameter("st")).toInt()
                        ), router, args
                    )
                    return true
                }

                "search" -> {
                    navigateTo(Screen.Search(settings = SearchSettings.parseSettings(uri.toString())), router, args)
                    return true
                }

                "rep" -> {
                    navigateTo(Screen.Reputation(args = RepArgs.fromUrl(uri.toString())), router, args)
                    return true
                }

                "fav" -> {
                    navigateTo(Screen.Favorites(), router, args)
                    return true
                }

                "mentions" -> {
                    navigateTo(Screen.Mentions(), router, args)
                    return true
                }
            }
        }
        return false
    }

    private fun handleSite(uri: Uri, router: TabRouter?, args: Map<String, String?>): Boolean {
        val site = linkHandlerParser.site(uri.toString())
        if (site != null) {
            navigateTo(
                Screen.ArticleDetail.FromLink(
                    articleId = site.articleId,
                    commentId = site.commentId
                ), router, args
            )
            return true
        }
        if (!uri.pathSegments.isEmpty() && uri.pathSegments[0].contains("special")) {
            return false
        }
        if (uri.pathSegments.isEmpty()) {
            navigateTo(Screen.ArticleList(), router, args)
            return true
        } else if (uri.pathSegments[0].matches("news|articles|reviews|tag|software|games|review".toRegex())) {
            navigateTo(Screen.ArticleList(), router, args)
            return true
        }

        return false
    }

    private fun handlePages(uri: Uri, router: TabRouter?, args: Map<String, String?>): Boolean {
        if (uri.pathSegments.size > 1 && uri.pathSegments[1].equals("go", ignoreCase = true)) {
            uri.getQueryParameter("u")?.let {
                try {
                    URLDecoder.decode(it, "UTF-8")
                } catch (ignore: Exception) {
                    it
                }
            }?.also {
                externalIntent(it)
                return true
            }
        }
        return false
    }

    private fun handleDevDb(uri: Uri, router: TabRouter?, args: Map<String, String?>): Boolean {
        if (uri.pathSegments.size > 1) {
            if (uri.pathSegments[1].matches("phones|pad|ebook|smartwatch".toRegex())) {
                if (uri.pathSegments.size > 2 && !uri.pathSegments[2].matches("new|select".toRegex())) {
                    navigateTo(Screen.DevDbDevices(categoryId = uri.pathSegments[1], brandId = uri.pathSegments[2]), router, args)
                    return true
                }
                navigateTo(Screen.DevDbBrands(categoryId = uri.pathSegments[1]), router, args)
                return true
            } else {
                navigateTo(Screen.DevDbDevice(deviceId = uri.pathSegments[1]), router, args)
                return true
            }
        } else {
            navigateTo(Screen.DevDbBrands(categoryId = null), router, args)
            return true
        }
    }

    private fun handleMedia(url: String, router: TabRouter?, args: Map<String, String?>): Boolean {
        val forumMedia = linkHandlerParser.forumMedia(url)
        if (forumMedia != null) {
            val isImage = MimeTypeUtil.isImage(forumMedia.extension)
            if (isImage) {
                navigateTo(Screen.ImageViewer(urls = listOf(url)), router, args)
            } else {
                handleDownload(url, forumMedia.fileName)
            }
            return true
        }
        if (linkHandlerParser.isSupportImage(url)) {
            navigateTo(Screen.ImageViewer(urls = listOf(url)), router, args)
            return true
        }
        return false
    }

    private fun normalizeForumUrl(inputUrl: String): String {
        val forumLoFi = linkHandlerParser.forumLoFi(inputUrl)
        if (forumLoFi != null) {
            return buildString {
                append("https://4pda.to/forum/index.php?")
                when (forumLoFi.type) {
                    "t" -> append("showtopic=")
                    "f" -> append("showforum=")
                }
                append(forumLoFi.id)
                if (forumLoFi.st != null) {
                    append("&st=")
                    append(forumLoFi.st)
                }
            }
        }
        return inputUrl
    }

}