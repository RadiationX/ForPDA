package forpdateam.ru.forpda.presentation

import android.net.Uri
import android.util.Log
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.MimeTypeUtil
import java.net.URLDecoder
import java.util.regex.Pattern

/**
 * Created by radiationx on 03.02.18.
 */
class LinkHandler(
        private val systemLinkHandler: ISystemLinkHandler
) : ILinkHandler {

    companion object {
        const val LOG_TAG = "LinkHandler"
    }

    private val forumMediaPattern by lazy { Pattern.compile("https?:\\/\\/4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/([\\s\\S]*\\.([\\s\\S]*))") }

    private val supportImagePattern by lazy { Pattern.compile("\\/\\/.*?(4pda\\.to|4pda\\.ru|ggpht\\.com|googleusercontent\\.com|windowsphone\\.com|mzstatic\\.com|savepic\\.net|savepice\\.ru|savepic\\.ru|.*?\\.ibb\\.com?)\\/[\\s\\S]*?\\.(png|jpg|jpeg|gif)") }

    private val forumLofiPattern by lazy { Pattern.compile("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)\\/forum\\/lofiversion\\/[^\\?]*?\\?(t|f)(\\d+)(?:-(\\d+))?") }

    private val baseFourPdaPattern by lazy { Pattern.compile("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)[\\s\\S]*") }

    private val sitePattern by lazy { Pattern.compile("https?:\\/\\/4pda\\.ru\\/(?:.+?p=|\\d+\\/\\d+\\/\\d+\\/|[\\w\\/]*?\\/?(newer|older)\\/)(\\d+)(?:\\/#comment(\\d+))?") }


    private fun handleDownload(url: String, name: String? = null) {
        systemLinkHandler.handleDownload(url, name)
    }

    private fun externalIntent(url: String) {
        systemLinkHandler.handle(url)
    }

    private fun navigateTo(screen: Screen, router: TabRouter?, args: Map<String, String>) {
        router?.navigateTo(screen.apply {
            args[Screen.ARG_TITLE]?.let { screen.screenTitle = it }
            args[Screen.ARG_SUBTITLE]?.let { screen.screenSubTitle = it }
        })
    }

    override fun handle(inputUrl: String?, router: TabRouter?): Boolean {
        return handle(inputUrl, router, emptyMap())
    }

    override fun handle(inputUrl: String?, router: TabRouter?, args: Map<String, String>): Boolean {
        var someRouter = router
        var url = inputUrl.orEmpty()
        if (url.isBlank() || url == "#") {
            return false
        }
        if (url.substring(0, 2) == "//") {
            url = "https:$url"
        } else if (url.substring(0, 1) == "/") {
            url = "https://4pda.ru$url"
        }
        url = url.replace("&amp;", "&").replace("\"", "").trim()
        Log.d(LOG_TAG, "Corrected url $url")

        if (someRouter == null) {
            someRouter = App.get().Di().router
        }

        if (handleMedia(url, someRouter, args)) {
            return true
        }
        url = normalizeForumUrl(url)

        if (baseFourPdaPattern.matcher(url).matches()) {
            val uri = Uri.parse(url.toLowerCase())
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

    private fun handleForum(uri: Uri, router: TabRouter?, args: Map<String, String>): Boolean {
        uri.getQueryParameter("showuser")?.also { param ->
            navigateTo(Screen.Profile().apply {
                profileUrl = uri.toString()
            }, router, args)
            return true
        }
        uri.getQueryParameter("showtopic")?.also { param ->
            navigateTo(Screen.Theme().apply {
                themeUrl = uri.toString()
            }, router, args)
            return true
        }

        uri.getQueryParameter("showforum")?.also { param ->
            navigateTo(Screen.Topics().apply {
                forumId = param.toInt()
            }, router, args)
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
                            navigateTo(Screen.QmsChat().apply {
                                userId = qmsUserId.toInt()
                                themeId = qmsThemeId.toInt()
                            }, router, args)
                        } else {
                            navigateTo(Screen.QmsThemes().apply {
                                userId = qmsUserId.toInt()
                            }, router, args)
                        }
                    }
                    return true
                }
                "boardrules" -> {
                    navigateTo(Screen.ForumRules(), router, args)
                    return true
                }
                "announce" -> {
                    navigateTo(Screen.Announce().apply {
                        uri.getQueryParameter("st")?.also {
                            announceId = it.toInt()
                        }
                        uri.getQueryParameter("f")?.also {
                            forumId = it.toInt()
                        }
                    }, router, args)
                    return true
                }
                "search" -> {
                    navigateTo(Screen.Search().apply {
                        searchUrl = uri.toString()
                    }, router, args)
                    return true
                }
                "rep" -> {
                    navigateTo(Screen.Reputation().apply {
                        reputationUrl = uri.toString()
                    }, router, args)
                    return true
                }
                "findpost" -> {
                    navigateTo(Screen.Theme().apply {
                        themeUrl = uri.toString()
                    }, router, args)
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

    private fun handleSite(uri: Uri, router: TabRouter?, args: Map<String, String>): Boolean {
        val matcher = sitePattern.matcher(uri.toString())
        if (matcher.find()) {
            navigateTo(Screen.ArticleDetail().apply {
                matcher.group(2)?.also {
                    articleId = it.toInt()
                }
                matcher.group(3)?.also {
                    commentId = it.toInt()
                }
                articleUrl = uri.toString()
            }, router, args)
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

    private fun handlePages(uri: Uri, router: TabRouter?, args: Map<String, String>): Boolean {
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

    private fun handleDevDb(uri: Uri, router: TabRouter?, args: Map<String, String>): Boolean {
        if (uri.pathSegments.size > 1) {
            if (uri.pathSegments[1].matches("phones|pad|ebook|smartwatch".toRegex())) {
                if (uri.pathSegments.size > 2 && !uri.pathSegments[2].matches("new|select".toRegex())) {
                    navigateTo(Screen.DevDbDevices().apply {
                        categoryId = uri.pathSegments[1]
                        brandId = uri.pathSegments[2]
                    }, router, args)
                    return true
                }
                navigateTo(Screen.DevDbBrands().apply {
                    categoryId = uri.pathSegments[1]
                }, router, args)
                return true
            } else {
                navigateTo(Screen.DevDbDevice().apply {
                    deviceId = uri.pathSegments[1]
                }, router, args)
                return true
            }
        } else {
            navigateTo(Screen.DevDbBrands(), router, args)
            return true
        }
    }

    private fun handleMedia(url: String, router: TabRouter?, args: Map<String, String>): Boolean {
        val matcher = forumMediaPattern.matcher(url)
        if (matcher.find()) {
            var fullName = matcher.group(1)
            try {
                fullName = URLDecoder.decode(fullName, "CP1251")
            } catch (ignore: Exception) {
            }

            val extension = matcher.group(2)
            val isImage = MimeTypeUtil.isImage(extension)
            if (isImage) {
                navigateTo(Screen.ImageViewer().apply {
                    urls.add(url)
                }, router, args)
            } else {
                handleDownload(url, fullName)
            }
            return true
        } else if (supportImagePattern.matcher(url).find()) {
            navigateTo(Screen.ImageViewer().apply {
                urls.add(url)
            }, router, args)
            return true
        }
        return false
    }

    private fun normalizeForumUrl(inputUrl: String): String {
        val matcher = forumLofiPattern.matcher(inputUrl)
        if (matcher.find()) {
            var url = "https://4pda.ru/forum/index.php?"

            url += when (matcher.group(1)) {
                "t" -> "showtopic="
                "f" -> "showforum="
                else -> ""
            } + matcher.group(2)

            matcher.group(3)?.also {
                url += "&st=$it"
            }
            return url
        }
        return inputUrl
    }

}