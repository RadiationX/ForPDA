package forpdateam.ru.forpda.presentation

import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm

sealed class Screen {
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

    class Main : Screen() {
        var checkWebView = true
    }

    class WebViewNotFound : Screen()
    class UpdateChecker : Screen() {
        var jsonSource: String? = null
    }

    class ImageViewer : Screen() {
        var urls: MutableList<String> = mutableListOf()
        var selected: Int = NO_ID
    }

    class Settings : Screen() {
        var fragment: String? = null
    }

    /* Fragments */
    class Auth : Screen() {
        override var isAlone: Boolean = true
    }

    class DevDbDevices : Screen() {
        var brandId: String? = null
        var categoryId: String? = null
    }

    class DevDbBrands : Screen() {
        override var isAlone: Boolean = true
        var categoryId: String? = null
    }

    class DevDbDevice : Screen() {
        var deviceId: String? = null
    }

    class DevDbSearch : Screen()

    class EditPost : Screen() {
        var editPostForm: EditPostForm? = null
        var postId: Int = NO_ID
        var topicId: Int = NO_ID
        var forumId: Int = NO_ID
        var st: Int = 0
        var themeName: String? = null
    }

    class Favorites : Screen() {
        override var isAlone: Boolean = true
    }

    class Forum : Screen() {
        var forumId: Int = NO_ID
    }

    class History : Screen() {
        override var isAlone: Boolean = true
    }

    class Mentions : Screen() {
        override var isAlone: Boolean = true
    }

    class ArticleList : Screen() {
        override var isAlone: Boolean = true
    }

    class ArticleDetail : Screen() {
        var articleId: Int = NO_ID
        var commentId: Int = NO_ID
        var articleUrl: String? = null
        var articleTitle: String? = null
        var articleAuthorNick: String? = null
        var articleDate: String? = null
        var articleImageUrl: String? = null
        var articleCommentsCount: Int = 0
    }

    class Notes : Screen() {
        override var isAlone: Boolean = true
    }

    class Announce : Screen() {
        var forumId: Int = NO_ID
        var announceId: Int = NO_ID
    }

    class ForumRules : Screen() {
        override var isAlone: Boolean = true
    }

    class GoogleCaptcha : Screen()

    class Profile : Screen() {
        var profileUrl: String? = null
    }

    class QmsContacts : Screen() {
        override var isAlone: Boolean = true
    }

    class QmsBlackList : Screen()
    class QmsThemes : Screen() {
        var userId: Int = NO_ID
        var avatarUrl: String? = null
    }

    class QmsChat : Screen() {
        var userId: Int = NO_ID
        var themeId: Int = NO_ID
        var userNick: String? = null
        var themeTitle: String? = null
        var avatarUrl: String? = null
    }

    class Reputation : Screen() {
        var reputationUrl: String? = null
    }

    class Search : Screen() {
        var searchUrl: String? = null
    }

    class Theme : Screen() {
        companion object {
            const val CODE_RESULT_SYNC = 10
            const val CODE_RESULT_PAGE = 11
        }

        var themeUrl: String? = null
    }

    class Topics : Screen() {
        var forumId: Int = NO_ID
    }

    class OtherMenu() : Screen() {
        override var fromMenu = true
        override var isAlone = true
    }

}