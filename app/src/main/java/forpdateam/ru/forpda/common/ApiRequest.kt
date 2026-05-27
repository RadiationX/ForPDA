package forpdateam.ru.forpda.common

import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.search.SearchSettings.Companion.ARG_EXCLUDE_TRASH
import forpdateam.ru.forpda.entity.remote.search.SearchSettings.Companion.RESOURCE_NEWS
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import okhttp3.HttpUrl
import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.coretypes.ArticleAnswerId
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.ArticlePollId
import ru.radiationx.coretypes.AttachmentId
import ru.radiationx.coretypes.AttachmentRelation
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageNumber
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId

sealed class ApiRequest {

    private val pathSegments = mutableListOf<String>()
    private val queryMap = mutableListOf<Pair<String, String>>()
    private val requestBuilder = NetworkRequest.Builder()

    protected fun path(segment: String) {
        pathSegments.add(segment)
    }

    protected fun path(segment: Int) {
        path(segment.toString())
    }

    protected fun query(key: String, value: String) {
        queryMap.add(key to value)
    }

    protected fun query(key: String, value: Int) {
        query(key, value.toString())
    }

    protected fun xhr() {
        requestBuilder.xhrHeader()
    }

    protected fun header(key: String, value: String) {
        requestBuilder.addHeader(key, value)
    }

    protected fun form(key: String, value: String) {
        requestBuilder.formHeader(key, value)
    }

    protected fun form(key: String, value: Int) {
        form(key, value.toString())
    }

    protected fun form(key: String, value: Long) {
        form(key, value.toString())
    }

    protected fun file(key: String, file: NetworkRequest.File) {
        requestBuilder.file(key, file)
    }

    protected fun multipart() {
        requestBuilder.multipart()
    }

    protected fun withoutBody() {
        requestBuilder.withoutBody()
    }

    fun buildHttpUrl(): HttpUrl {
        return HttpUrl.Builder().apply {
            scheme("https")
            host("4pda.to")
            pathSegments.forEach { segment ->
                addPathSegment(segment)
            }
            queryMap.onEach { (key, value) ->
                addQueryParameter(key, value)
            }
        }.build()
    }

    fun buildNetworkRequest(): NetworkRequest {
        requestBuilder.url(buildHttpUrl().toString())
        return requestBuilder.build()
    }

    sealed class Site : ApiRequest() {

        data class GetArticles(val page: PageNumber) : Site() {
            init {
                path(page.value)
            }
        }

        data class GetArticle(val articleId: ArticleId) : Site() {
            init {
                query("p", articleId.id)
            }
        }

        data class LikeComment(val articleId: ArticleId, val commentId: CommentId) : Site() {
            init {
                path("pages")
                path("karma")
                query("p", articleId.id)
                query("c", commentId.id)
                query("v", "1")
                xhr()
            }
        }

        data class SendComment(val articleId: ArticleId, val commentId: CommentId?, val text: String) : Site() {
            init {
                path("wp-comments-post.php")
                form("comment_post_ID", articleId.id)
                form("comment_reply_ID", commentId?.id ?: 0)
                form("comment_reply_dp", if (commentId == null) "0" else "1")
                form("comment", text)
            }
        }

        data class SendPoll(val pollId: ArticlePollId, val answers: List<ArticleAnswerId>, val fromUrl: String) : Site() {
            init {
                path("pages")
                path("poll")
                query("act", "vote")
                query("poll_id", pollId.id)
                multipart()
                xhr()
                form("from", fromUrl)
                answers.forEach {
                    form("answer[]", it.id)
                }
            }
        }
    }

    sealed class DevDb : ApiRequest() {

        init {
            path("devdb")
        }

        data class GetBrands(val categoryId: DevDbCategoryId) : DevDb() {
            init {
                path(categoryId.id)
                path("all")
            }
        }

        data class GetDevices(val devicesId: DevDbDevicesId) : DevDb() {
            init {
                path(devicesId.categoryId.id)
                path(devicesId.brandId.id)
                path("all")
            }
        }

        data class GetDevice(val deviceId: DevDbDeviceId) : DevDb() {
            init {
                path(deviceId.id)
            }
        }

        data class Search(val text: String) : DevDb() {
            init {
                path("search")
                query("s", text)
            }
        }
    }

    sealed class Forum : ApiRequest() {

        init {
            path("forum")
        }

        sealed class Attachments : Forum() {

            init {
                path("index.php")
                query("act", "attach")
            }

            data class Delete(val attachmentId: AttachmentId, val relation: AttachmentRelation) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("code", "remove")
                    form("id", attachmentId.id)
                    form(relation)
                }
            }

            data class GetExisted(val relation: AttachmentRelation, val md5: String, val size: Long, val name: String) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("forum-attach-files", "")
                    form("code", "check")
                    form("md5", md5)
                    form("size", size)
                    form("name", name)
                    form(relation)
                }
            }

            data class Upload(val relation: AttachmentRelation, val file: NetworkRequest.File) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("forum-attach-files", "")
                    form("code", "upload")
                    file("FILE_UPLOAD[]", file)
                    form(relation)
                }
            }

            data class GetAttachedToPost(val postId: PostId) : Attachments() {
                init {
                    query("index", "1")
                    query("relId", postId.id)
                    query("maxSize", "134217728")
                    query("allowExt", "")
                    query("code", "init")
                    query("unliked", "")
                }
            }

            protected fun form(relation: AttachmentRelation) {
                when (relation) {
                    is AttachmentRelation.Post -> {
                        val relId = relation.postId?.id ?: 0
                        form("relId", relId)
                    }

                    AttachmentRelation.Qms -> {
                        form("relId", 0)
                        form("relType", "MSG")
                    }
                }
            }
        }

        sealed class Auth : Forum() {
            init {
                path("index.php")
                query("act", "auth")
            }

            data object GetCaptcha : Auth()

            data class Login(val captcha: AuthCaptcha, val form: AuthForm) : Auth() {
                init {
                    form("captcha-time", captcha.captchaTime)
                    form("captcha-sig", captcha.captchaSig)
                    form("captcha", form.captcha)
                    form("return", "https://4pda.to/forum/index.php?showforum=200#afterauth")
                    form("login", form.nick)
                    form("password", form.password)
                    form("remember", "1")
                    form("hidden", if (form.isHidden) "1" else "0")
                }
            }

            data class Logout(val authKey: String?) : Auth() {
                init {
                    query("act", "logout")
                    query("CODE", "03")
                    query("k", authKey.orEmpty())
                }
            }
        }

        sealed class Post : Forum() {

            data class Edit(val postId: PostId) : Post() {
                init {
                    path("index.php")
                    query("act", "post")
                    query("do", "edit")
                    query("p", postId.id)
                }
            }

            data class Send(val form: EditPostForm, val authKey: String?) : Post() {
                init {
                    path("index.php")
                    multipart()
                    form("act", "Post")
                    form("CODE", if (form.type == EditPostForm.TYPE_NEW_POST) "03" else "9")
                    form("f", form.forumId.id)
                    form("t", form.topicId.id)
                    form("auth_key", authKey.orEmpty())
                    form("Post", form.message)
                    form("enablesig", "yes")
                    form("enableemo", "yes")
                    form("st", form.st.value)
                    form("removeattachid", "0")
                    form("MAX_FILE_SIZE", "0")
                    form("parent_id", "0")
                    form("ed-0_wysiwyg_used", "0")
                    form("editor_ids[]", "ed-0")
                    form("iconid", "0")
                    form("_upload_single_file", "1")

                    form.poll?.also { poll ->
                        form("poll_question", poll.title.replaceNewLine())
                        poll.getQuestions().forEachIndexed { qIndex, question ->
                            form("question[${qIndex + 1}]", question.title.replaceNewLine())
                            form("multi[${qIndex + 1}]", if (question.isMulti) "1" else "0")
                            question.getChoices().forEachIndexed { cIndex, choice ->
                                form("choice[${qIndex + 1}${'_'}${cIndex + 1}]", choice.title.replaceNewLine())
                            }
                        }
                    }

                    if (form.type == EditPostForm.TYPE_EDIT_POST) {
                        form("post_edit_reason", form.editReason)
                    }

                    form("file-list", form.attachments.joinToString(separator = ",") { it.id.id.toString() })

                    form.postId?.id?.also {
                        form("p", it)
                    }
                }

                private fun String.replaceNewLine(): String {
                    return replace('\n', ' ')
                }
            }

            data class Delete(val postId: PostId, val authKey: String?) : Post() {
                init {
                    path("index.php")
                    query("act", "zmod")
                    query("auth_key", authKey.orEmpty())
                    query("code", "postchoice")
                    query("tact", "delete")
                    query("selectedpids", postId.id)
                    xhr()
                }
            }

            data class Report(val topicId: TopicId, val postId: PostId, val message: String) : Post() {
                init {
                    path("index.php")
                    query("act", "report")
                    query("send", "1")
                    query("t", topicId.id)
                    query("p", postId.id)
                    form("message", message)
                }
            }

            data class Vote(val postId: PostId, val value: String) : Post() {
                init {
                    path("zka.php")
                    query("i", postId.id)
                    query("v", value)
                }
            }
        }

        sealed class Favorite : Forum() {
            init {
                path("index.php")
                query("act", "fav")
            }

            data class GetList(val offset: PageOffset, val sorting: Sorting) : Favorite() {
                init {
                    query("type", "all")
                    query("st", offset.value)
                    query(Sorting.Key.HEADER, sorting.key)
                    query(Sorting.Order.HEADER, sorting.order)
                }
            }

            sealed class Add(trackType: String) : Favorite() {
                init {
                    query("type", "add")
                    query("track_type", trackType)
                }

                data class Topic(val topicId: TopicId, val trackType: String) : Add(trackType) {
                    init {
                        query("t", topicId.id)
                    }
                }

                data class Forum(val forumId: ForumId, val trackType: String) : Add(trackType) {
                    init {
                        query("f", forumId.id)
                    }
                }
            }

            data class Delete(val favoriteId: FavoriteId) : Favorite() {
                init {
                    xhr()
                    form("selectedtids", favoriteId.id)
                    form("tact", "delete")
                }
            }

            data class EditTrackType(val favoriteId: FavoriteId, val trackType: String) : Favorite() {
                init {
                    query("sort_key", "")
                    query("sort_by", "")
                    query("type", "all")
                    query("st", "0")
                    query("tact", trackType)
                    query("selectedtids", favoriteId.id)
                }
            }

            data class EditPinState(val favoriteId: FavoriteId, val state: String) : Favorite() {
                init {
                    form("selectedtids", favoriteId.id)
                    form("tact", state)
                }
            }
        }

        sealed class Forums : Forum() {
            init {
                path("index.php")
            }

            data class GetAnnounce(val announceId: AnnounceId) : Forums() {
                init {
                    query("act", "announce")
                    query("f", announceId.forumId.id)
                    query("st", announceId.st)
                }
            }

            data object GetAllForums : Forums() {
                init {
                    query("act", "search")
                }
            }

            data object GetRules : Forums() {
                init {
                    query("act", "boardrules")
                }
            }

            data object MarkAllRead : Forums() {
                init {
                    query("act", "auth")
                    query("action", "markboard")
                    withoutBody()
                }
            }

            data class MarkRead(val forumId: ForumId) : Forums() {
                init {
                    query("act", "auth")
                    query("action", "markforum")
                    query("f", forumId.id)
                    query("fromforum", forumId.id)
                    withoutBody()
                }
            }

            data class GetTopics(val forumId: ForumId, val offset: PageOffset) : Forums() {
                init {
                    query("showforum", forumId.id)
                    query("st", offset.value)
                }
            }
        }

        sealed class Inspector(code: String) : Forum() {
            init {
                path("index.php")
                query("act", "inspector")
                query("CODE", code)
            }

            data object Favorites : Inspector("fav")
            data object Mentions : Inspector("mentions")
            data object Qms : Inspector("qms")
        }

        sealed class Mentions : Forum() {
            init {
                path("index.php")
            }

            data class LoadPage(val offset: PageOffset) : Mentions() {
                init {
                    query("act", "mentions")
                    query("st", offset.value)
                }
            }
        }

        sealed class Profile : Forum() {
            init {
                path("index.php")
            }

            data class Load(val userId: UserId) : Profile() {
                init {
                    query("showuser", userId.id)
                }
            }

            data class SaveNote(val note: String) : Profile() {
                init {
                    query("act", "profile-xhr")
                    query("action", "save-note")
                    form("note", note)
                }
            }
        }

        sealed class Qms : Forum() {
            init {
                path("index.php")
            }

            data class BlockUser(val nick: String) : Qms() {
                init {
                    query("act", "qms")
                    query("settings", "blacklist")
                    query("xhr", "blacklist-form")
                    query("do", "1")
                    form("action", "add-user")
                    form("username", nick)
                }
            }

            data class DeleteThreads(val userId: UserId) : Qms() {
                init {
                    form("act", "qms-xhr")
                    form("action", "del-member")
                    form("del-mid", userId.id)
                }
            }

            data class DeleteThread(val chatId: QmsChatId) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", chatId.userId.id)
                    query("xhr", "body")
                    query("do", "1")
                    form("xhr", "body")
                    form("action", "delete-threads")
                    chatId.threadId.id.also {
                        form("thread-id[$it]", it)
                    }
                }
            }

            data class FindUser(val nick: String) : Qms() {
                init {
                    query("act", "qms-xhr")
                    query("action", "autocomplete-username")
                    query("q", nick)
                    xhr()
                }
            }

            data object GetBlackList : Qms() {
                init {
                    query("act", "qms")
                    query("setting", "blacklist")
                    form("xhr", "body")
                }
            }

            data class GetChat(val chatId: QmsChatId) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", chatId.userId.id)
                    query("t", chatId.threadId.id)
                    form("xhr", "body")
                }
            }

            data object GetContacts : Qms() {
                init {
                    query("act", "qms-xhr")
                    query("action", "userlist")
                }
            }

            data class GetMessagesAfter(val chatId: QmsChatId, val lastMessageId: QmsMessageId?) : Qms() {
                init {
                    query("act", "qms-xhr")
                    xhr()
                    form("action", "get-thread-messages")
                    form("mid", chatId.userId.id)
                    form("t", chatId.threadId.id)
                    lastMessageId?.id?.also {
                        form("after-message", it)
                    }
                }
            }

            data class GetMessageInfo(val threadId: QmsThreadId, val messageId: QmsMessageId) : Qms() {
                init {
                    query("act", "qms-xhr")
                    form("action", "message-info")
                    form("t", threadId.id)
                    form("msg-id", messageId.id)
                }
            }

            data class GetThreads(val userId: UserId) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", userId.id)
                    form("xhr", "body")
                }
            }

            data class SendMessage(val chatId: QmsChatId, val text: String, val attachmentIds: List<AttachmentId>) : Qms() {
                init {
                    form("act", "qms-xhr")
                    form("action", "send-message")
                    form("message", text)
                    form("mid", chatId.userId.id)
                    form("t", chatId.threadId.id)
                    form("attaches", attachmentIds.joinToString { it.id.toString() })
                }
            }

            data class CreateThread(val nick: String, val title: String, val text: String, val attachmentIds: List<AttachmentId>) : Qms() {
                init {
                    query("act", "qms")
                    query("action", "action=create-thread")
                    query("xhr", "body")
                    query("do", "1")
                    form("username", nick)
                    form("title", title)
                    form("message", text)
                    form("attaches", attachmentIds.joinToString { it.id.toString() })
                }
            }

            data class UnblockUser(val userId: UserId) : Qms() {
                init {
                    query("act", "qms")
                    query("settings", "blacklist")
                    query("xhr", "blacklist-form")
                    query("do", "1")
                    form("action", "delete-users")
                    userId.id.also {
                        form("user-id[$it]", it)
                    }
                }
            }
        }

        sealed class Reputation : Forum() {
            init {
                path("index.php")
            }

            data class Edit(val postId: PostId?, val userId: UserId, val type: String, val message: String) : Reputation() {
                init {
                    form("act", "rep")
                    form("mid", userId.id)
                    form("type", type)
                    form("message", message)
                    if (postId != null) {
                        form("p", postId.id)
                    }
                }
            }

            data class GetPage(val userId: UserId, val mode: String, val order: String, val offset: PageOffset) : Reputation() {
                init {
                    query("act", "rep")
                    query("view", "history")
                    query("mid", userId.id)
                    query("mode", mode)
                    query("order", order)
                    query("st", offset.value)
                }
            }
        }
    }

    data class Search(val settings: SearchSettings) : ApiRequest() {

        init {
            if (settings.resourceType == RESOURCE_NEWS.first) {
                path("page")
                path(settings.st)
                query("s", settings.query.orEmpty())
            } else {
                path("forum")
                path("index.php")
                query("act", "search")
                query("result", settings.result.orEmpty())
                query("sort", settings.sort.orEmpty())
                query("source", settings.source.orEmpty())
                query("query", settings.query.orEmpty())
                query("username", settings.nick.orEmpty())
                settings.forums.forEach {
                    query("forums[]", it)
                }
                settings.topics.forEach {
                    query("topics[]", it)
                }
                settings.subforums?.also {
                    query("subforums", it)
                }
                query("noform", "1")
                query("st", settings.st)
                query(ARG_EXCLUDE_TRASH, settings.excludeTrash)
            }
        }
    }

}