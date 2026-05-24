package forpdateam.ru.forpda.presentation

import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import ru.radiationx.links.Link

sealed class Screen : com.github.terrakok.cicerone.Screen {
    companion object {
        const val ARG_TITLE = "arg_title"
        const val ARG_SUBTITLE = "arg_subtitle"
        private val NO_ID = -1
    }

    open var screenTitle: String? = null
    open var screenSubTitle: String? = null
    open var fromMenu: Boolean = false
    open var isAlone: Boolean = false

    fun getKey(): String = this::class.java.simpleName

    /* Activities */

    class Main : Screen()

    class UpdateChecker : Screen()

    class ImageViewer(
        val urls: List<String>,
        val selectedUrl: String? = null
    ) : Screen()

    class Settings : Screen()

    /* Fragments */
    class Auth : Screen() {
        override var isAlone: Boolean = true
    }

    class DevDbDevices(val link: Link.DevDb.Devices) : Screen()

    class DevDbBrands(val link: Link.DevDb.Brands?) : Screen() {
        override var isAlone: Boolean = true
    }

    class DevDbDevice(val link: Link.DevDb.Device) : Screen()

    class DevDbSearch(val link: Link.DevDb.Search) : Screen()

    sealed class EditPost : Screen() {
        class New(
            val editPostForm: EditPostForm,
            val themeName: String,
        ) : EditPost()

        class Existed(
            val postId: Int,
            val topicId: Int,
            val forumId: Int,
            val st: Int,
            val themeName: String,
        ) : EditPost()
    }

    class Favorites(val link: Link.Board.Favorite) : Screen() {
        override var isAlone: Boolean = true
    }

    class Forum(
        val forumId: Int = NO_ID
    ) : Screen()

    class History : Screen() {
        override var isAlone: Boolean = true
    }

    class Mentions(val link: Link.Board.Mentions) : Screen() {
        override var isAlone: Boolean = true
    }

    class ArticleList(val link: Link.Site.Page) : Screen() {
        override var isAlone: Boolean = true
    }

    sealed class ArticleDetail : Screen() {
        class FromLink(val link: Link.Site.Details) : ArticleDetail()

        class FromList(
            val articleId: Int,
            val articleTitle: String,
            val articleAuthorNick: String,
            val articleDate: String,
            val articleImageUrl: String,
            val articleCommentsCount: Int
        ) : ArticleDetail()
    }

    class Notes : Screen() {
        override var isAlone: Boolean = true
    }

    class Announce(val link: Link.Board.Announce) : Screen()

    class ForumRules : Screen() {
        override var isAlone: Boolean = true
    }

    class GoogleCaptcha : Screen()

    class Profile(val link: Link.Board.Profile) : Screen()

    class QmsContacts : Screen() {
        override var isAlone: Boolean = true
    }

    class QmsBlackList : Screen()

    class QmsThemes(val link: Link.Board.Qms.Threads) : Screen()

    sealed class QmsChat : Screen() {
        class Create(val link: Link.Board.Qms.CreateThread) : QmsChat()

        class Created(val link: Link.Board.Qms.Chat) : QmsChat()
    }

    class Reputation(
        val link: Link.Board.Reputation
    ) : Screen()

    sealed class Search : Screen() {
        class Site(val link: Link.Site.Search?) : Search()
        class Forum(val link: Link.Board.Search?) : Search()
    }

    class Theme(
        val link: Link.Board.Topic
    ) : Screen() {
        companion object {
            const val CODE_RESULT_SYNC = "10"
            const val CODE_RESULT_PAGE = "11"
        }
    }

    class Topics(val link: Link.Board.Forum) : Screen()

    class OtherMenu : Screen() {
        override var fromMenu = true
        override var isAlone = true
    }

}