package forpdateam.ru.forpda.presentation

import forpdateam.ru.forpda.common.MimeTypeUtil
import forpdateam.ru.forpda.model.data.remote.api.common.LinkHandlerParser
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class LinkHandlerImpl @Inject constructor(
    private val systemLinkHandler: SystemLinkHandler,
    private val router: TabRouter,
    private val linkHandlerParser: LinkHandlerParser,
    private val linkTransformer: LinkTransformer
) : LinkHandler {

    companion object {
        const val LOG_TAG = "LinkHandler"
    }

    private fun handleDownload(url: String) {
        systemLinkHandler.handleDownload(url)
    }

    private fun externalIntent(url: String) {
        systemLinkHandler.handle(url)
    }

    private fun navigateTo(screen: Screen, args: Map<String, String?>) {
        router.navigateTo(screen.apply {
            args[Screen.ARG_TITLE]?.let { screen.screenTitle = it }
            args[Screen.ARG_SUBTITLE]?.let { screen.screenSubTitle = it }
        })
    }

    override fun handle(inputUrl: String): Boolean {
        return handle(inputUrl, emptyMap())
    }

    override fun handle(inputUrl: String, args: Map<String, String?>): Boolean {
        val url = when {
            inputUrl.startsWith("//") -> "https:$inputUrl"
            inputUrl.startsWith("/") -> "https://4pda.to$inputUrl"
            else -> inputUrl
        }

        val link = linkTransformer.parse(url)
        if (link != null) {
            if (handleLink(link, args)) {
                return true
            }
            if (link is Link.Other.ExternalLink) {
                externalIntent(link.url)
                return true
            }
        }

        if (handleMedia(url, args)) {
            return true
        }

        externalIntent(url)

        return false
    }

    private fun handleLink(link: Link, args: Map<String, String?>): Boolean {
        val screen = when (link) {
            is Link.Board.Announce -> Screen.Announce(announceId = link.announceId)
            is Link.Board.Auth.LoginForm -> Screen.Auth()
            is Link.Board.Favorite -> Screen.Favorites()
            is Link.Board.Forum -> Screen.Topics(link = link)
            is Link.Board.Mentions -> Screen.Mentions()
            is Link.Board.Profile -> Screen.Profile(userId = link.userId)
            is Link.Board.Qms.BlackList -> Screen.QmsBlackList()
            is Link.Board.Qms.Chat -> Screen.QmsChat.Existed(chatId = link.chatId)
            is Link.Board.Qms.Contacts -> Screen.QmsContacts()
            is Link.Board.Qms.CreateThread -> Screen.QmsChat.Create(userId = link.userId)
            is Link.Board.Qms.Threads -> Screen.QmsThemes(userId = link.userId)
            is Link.Board.Reputation.History -> Screen.Reputation(link = link)
            is Link.Board.Reputation.Rating -> null // not supported
            is Link.Board.Root -> Screen.Forum(null)
            is Link.Board.Rules -> Screen.ForumRules()
            is Link.Board.Search -> Screen.Search.Forum(link = link)
            is Link.Board.Topic -> Screen.Theme(link = link)
            is Link.DevDb.Devices -> Screen.DevDbDevices(devicesId = link.devicesId)
            is Link.DevDb.Brands -> Screen.DevDbBrands(categoryId = link.categoryId)
            is Link.DevDb.Categories -> Screen.DevDbBrands(categoryId = null)
            is Link.DevDb.Device -> Screen.DevDbDevice(deviceId = link.deviceId)
            is Link.DevDb.Search -> Screen.DevDbSearch(text = link.text)
            is Link.Site.Details -> Screen.ArticleDetail.FromLink(link = link)
            is Link.Site.Page -> Screen.ArticleList()
            is Link.Site.RelativePage -> null // not supported
            is Link.Site.Search -> Screen.Search.Site(link = link)
            is Link.Other.ExternalLink -> null // handle separated
        }
        if (screen != null) {
            navigateTo(screen, args)
            return true
        }
        return false
    }

    private fun handleMedia(url: String, args: Map<String, String?>): Boolean {
        val forumMedia = linkHandlerParser.forumMedia(url)
        if (forumMedia != null) {
            val isImage = MimeTypeUtil.isImage(forumMedia.extension)
            if (isImage) {
                navigateTo(Screen.ImageViewer(urls = listOf(url)), args)
            } else {
                handleDownload(url)
            }
            return true
        }
        if (linkHandlerParser.isSupportImage(url)) {
            navigateTo(Screen.ImageViewer(urls = listOf(url)), args)
            return true
        }
        return false
    }

}