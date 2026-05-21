package forpdateam.ru.forpda.presentation

import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.reputation.RepArgs
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.TopicUrl

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

    class DevDbDevices(
        val categoryId: String,
        val brandId: String,
    ) : Screen()

    class DevDbBrands(
        val categoryId: String? = null
    ) : Screen() {
        override var isAlone: Boolean = true
    }

    class DevDbDevice(
        val deviceId: String
    ) : Screen()

    class DevDbSearch : Screen()

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

    class Favorites : Screen() {
        override var isAlone: Boolean = true
    }

    class Forum(
        val forumId: Int = NO_ID
    ) : Screen()

    class History : Screen() {
        override var isAlone: Boolean = true
    }

    class Mentions : Screen() {
        override var isAlone: Boolean = true
    }

    class ArticleList : Screen() {
        override var isAlone: Boolean = true
    }

    sealed class ArticleDetail : Screen() {
        class FromLink(
            val articleId: Int,
            val commentId: Int?
        ) : ArticleDetail()

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

    class Announce(
        val forumId: Int,
        val announceId: Int
    ) : Screen()

    class ForumRules : Screen() {
        override var isAlone: Boolean = true
    }

    class GoogleCaptcha : Screen()

    class Profile(val userId: Int) : Screen()

    class QmsContacts : Screen() {
        override var isAlone: Boolean = true
    }

    class QmsBlackList : Screen()

    class QmsThemes(
        val userId: Int,
        val avatarUrl: String? = null
    ) : Screen()

    sealed class QmsChat : Screen() {
        class Create : QmsChat()

        class CreateWithUser(
            val userId: Int,
            val userNick: String,
            val avatarUrl: String?
        ) : QmsChat()

        class FromLink(
            val userId: Int,
            val themeId: Int,
        ) : QmsChat()

        class FromList(
            val userId: Int,
            val themeId: Int,
            val userNick: String,
            val themeTitle: String,
            val avatarUrl: String?
        ) : QmsChat()
    }

    class Reputation(
        val args: RepArgs
    ) : Screen()

    class Search(
        val settings: SearchSettings? = null
    ) : Screen()

    class Theme(
        val topicUrl: TopicUrl
    ) : Screen() {
        companion object {
            const val CODE_RESULT_SYNC = "10"
            const val CODE_RESULT_PAGE = "11"
        }

        var themeUrl: String? = null
    }

    class Topics(
        val forumId: Int
    ) : Screen()

    class OtherMenu : Screen() {
        override var fromMenu = true
        override var isAlone = true
    }

}