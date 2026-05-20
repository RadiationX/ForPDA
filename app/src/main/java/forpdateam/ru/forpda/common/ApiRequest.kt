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

sealed class ApiRequest {

    private val pathSegments = mutableListOf<String>()
    private val queryMap = mutableMapOf<String, String>()
    private val requestBuilder = NetworkRequest.Builder()

    protected fun path(segment: String) {
        pathSegments.add(segment)
    }

    protected fun query(key: String, value: String) {
        queryMap[key] = value
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

        data class GetArticles(val page: Int) : Site() {
            init {
                path(page.toString())
            }
        }

        data class GetArticle(val articleId: Int) : Site() {
            init {
                query("p", articleId.toString())
            }
        }

        data class LikeComment(val articleId: Int, val commentId: Int) : Site() {
            init {
                path("pages")
                path("karma")
                query("p", articleId.toString())
                xhr()
            }
        }

        data class SendComment(val articleId: Int, val commentId: Int, val text: String) : Site() {
            init {
                path("wp-comments-post.php")
                form("comment_post_ID", articleId.toString())
                form("comment_reply_ID", commentId.toString())
                form("comment_reply_dp", if (commentId == 0) "0" else "1")
                form("comment", text)
            }
        }

        data class SendPoll(val pollId: Int, val answers: List<Int>, val fromUrl: String) : Site() {
            init {
                path("pages")
                path("poll")
                query("act", "vote")
                query("poll_id", pollId.toString())
                multipart()
                xhr()
                form("from", fromUrl)
                answers.forEach {
                    form("answer[]", it.toString())
                }
            }
        }
    }

    sealed class DevDb : ApiRequest() {

        init {
            path("devdb")
        }

        data class GetBrands(val categoryId: String) : DevDb() {
            init {
                path(categoryId)
                path("all")
            }
        }

        data class GetBrand(val categoryId: String, val brandId: String) : DevDb() {
            init {
                path(categoryId)
                path(brandId)
                path("all")
            }
        }

        data class GetDevice(val deviceId: String) : DevDb() {
            init {
                path(deviceId)
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

            data class Delete(val attachmentId: Int, val relId: Int, val relType: String?) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("code", "remove")
                    form("id", attachmentId.toString())
                    if (relId != -1) {
                        form("relId", relId.toString())
                    }
                    if (relType != null) {
                        form("relType", relType)
                    }
                }
            }

            data class GetExisted(val relId: Int, val md5: String, val size: Long, val name: String) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("forum-attach-files", "")
                    form("code", "check")
                    form("md5", md5)
                    form("size", size.toString())
                    form("name", name)
                    if (relId != -1) {
                        form("relId", relId.toString())
                    }
                }
            }

            data class Upload(val relId: Int, val relType: String?, val file: NetworkRequest.File) : Attachments() {
                init {
                    xhr()
                    form("index", "1")
                    form("maxSize", "134217728")
                    form("allowExt", "")
                    form("forum-attach-files", "")
                    form("code", "upload")
                    file("FILE_UPLOAD[]", file)

                    if (relId != -1) {
                        form("relId", relId.toString())
                    }
                    if (relType != null) {
                        form("relType", relType)
                    }
                }
            }

            data class GetAttachedToPost(val postId: Int) : Attachments() {
                init {
                    query("index", "1")
                    query("relId", postId.toString())
                    query("maxSize", "134217728")
                    query("allowExt", "")
                    query("code", "init")
                    query("unliked", "")
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

            data class Edit(val postId: Int) : Post() {
                init {
                    path("index.php")
                    query("act", "post")
                    query("do", "edit")
                    query("p", postId.toString())
                }
            }

            data class Send(val form: EditPostForm, val authKey: String?) : Post() {
                init {
                    path("index.php")
                    multipart()
                    form("act", "Post")
                    form("CODE", if (form.type == EditPostForm.TYPE_NEW_POST) "03" else "9")
                    form("f", form.forumId.toString())
                    form("t", form.topicId.toString())
                    form("auth_key", authKey.orEmpty())
                    form("Post", form.message)
                    form("enablesig", "yes")
                    form("enableemo", "yes")
                    form("st", form.st.toString())
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

                    form("file-list", form.attachments.joinToString(separator = ","))

                    if (form.postId != 0) {
                        form("p", form.postId.toString())
                    }
                }

                private fun String.replaceNewLine(): String {
                    return replace('\n', ' ')
                }
            }

            data class Delete(val postId: Int, val authKey: String?) : Post() {
                init {
                    path("index.php")
                    query("act", "zmod")
                    query("auth_key", authKey.orEmpty())
                    query("code", "postchoice")
                    query("tact", "delete")
                    query("selectedpids", postId.toString())
                    xhr()
                }
            }

            data class Report(val topicId: Int, val postId: Int, val message: String) : Post() {
                init {
                    path("index.php")
                    query("act", "report")
                    query("send", "1")
                    query("t", topicId.toString())
                    query("p", postId.toString())
                    form("message", message)
                }
            }

            data class Vote(val postId: Int, val value: String) : Post() {
                init {
                    path("zka.php")
                    query("i", postId.toString())
                    query("v", value)
                }
            }
        }

        sealed class Favorite : Forum() {
            init {
                path("index.php")
                query("act", "fav")
            }

            data class GetList(val st: Int, val sorting: Sorting) : Favorite() {
                init {
                    query("type", "all")
                    query("st", st.toString())
                    query(Sorting.Key.HEADER, sorting.key)
                    query(Sorting.Order.HEADER, sorting.order)
                }
            }

            sealed class Add(trackType: String) : Favorite() {
                init {
                    query("type", "add")
                    query("track_type", trackType)
                }

                data class Topic(val topicId: Int, val trackType: String) : Add(trackType) {
                    init {
                        query("t", topicId.toString())
                    }
                }

                data class Forum(val forumId: Int, val trackType: String) : Add(trackType) {
                    init {
                        query("f", forumId.toString())
                    }
                }
            }

            data class Delete(val favId: Int) : Favorite() {
                init {
                    xhr()
                    form("selectedtids", favId.toString())
                    form("tact", "delete")
                }
            }

            data class EditTrackType(val favId: Int, val trackType: String) : Favorite() {
                init {
                    query("sort_key", "")
                    query("sort_by", "")
                    query("type", "all")
                    query("st", "0")
                    query("tact", trackType)
                    query("selectedtids", favId.toString())
                }
            }

            data class EditPinState(val favId: Int, val state: String) : Favorite() {
                init {
                    form("selectedtids", favId.toString())
                    form("tact", state)
                }
            }
        }

        sealed class Forums : Forum() {
            init {
                path("index.php")
            }

            data class GetAnnounce(val forumId: Int, val announceId: Int) : Forums() {
                init {
                    query("act", "announce")
                    query("f", forumId.toString())
                    query("st", announceId.toString())
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

            data class MarkRead(val forumId: Int) : Forums() {
                init {
                    query("act", "auth")
                    query("action", "markforum")
                    query("f", forumId.toString())
                    query("fromforum", forumId.toString())
                    withoutBody()
                }
            }

            data class GetTopics(val forumId: Int, val st: Int) : Forums() {
                init {
                    query("showforum", forumId.toString())
                    query("st", st.toString())
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

            data class LoadPage(val st: Int) : Mentions() {
                init {
                    query("act", "mentions")
                    query("st", st.toString())
                }
            }
        }

        sealed class Profile : Forum() {
            init {
                path("index.php")
            }

            data class Load(val userId: Int) : Profile() {
                init {
                    query("showuser", userId.toString())
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

            data class DeleteThreads(val userId: Int) : Qms() {
                init {
                    form("act", "qms-xhr")
                    form("action", "del-member")
                    form("del-mid", userId.toString())
                }
            }

            data class DeleteThread(val userId: Int, val threadId: Int) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", userId.toString())
                    query("xhr", "body")
                    query("do", "1")
                    form("xhr", "body")
                    form("action", "delete-threads")
                    form("thread-id[$threadId]", threadId.toString())
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

            data class GetChat(val userId: Int, val threadId: Int) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", userId.toString())
                    query("t", threadId.toString())
                    form("xhr", "body")
                }
            }

            data object GetContacts : Qms() {
                init {
                    query("act", "qms-xhr")
                    query("action", "userlist")
                }
            }

            data class GetMessagesAfter(val userId: Int, val threadId: Int, val lastMessageId: Int) : Qms() {
                init {
                    query("act", "qms-xhr")
                    xhr()
                    form("action", "get-thread-messages")
                    form("mid", userId.toString())
                    form("t", threadId.toString())
                    form("after-message", lastMessageId.toString())
                }
            }

            data class GetMessageInfo(val threadId: Int, val messageId: Int, val lastMessageId: Int) : Qms() {
                init {
                    query("act", "qms-xhr")
                    form("action", "message-info")
                    form("t", threadId.toString())
                    form("msg-id", messageId.toString())
                }
            }

            data class GetThreads(val userId: Int) : Qms() {
                init {
                    query("act", "qms")
                    query("mid", userId.toString())
                    form("xhr", "body")
                }
            }

            data class SendMessage(val userId: Int, val threadId: Int, val text: String, val attachmentIds: List<Int>) : Qms() {
                init {
                    form("act", "qms-xhr")
                    form("action", "send-message")
                    form("message", text)
                    form("mid", Integer.toString(userId))
                    form("t", Integer.toString(threadId))
                    form("attaches", attachmentIds.joinToString())
                }
            }

            data class CreateThread(val nick: String, val title: String, val text: String, val attachmentIds: List<Int>) : Qms() {
                init {
                    query("act", "qms")
                    query("action", "action=create-thread")
                    query("xhr", "body")
                    query("do", "1")
                    form("username", nick)
                    form("title", title)
                    form("message", text)
                    form("attaches", attachmentIds.joinToString())
                }
            }

            data class UnblockUser(val userId: Int) : Qms() {
                init {
                    query("act", "qms")
                    query("settings", "blacklist")
                    query("xhr", "blacklist-form")
                    query("do", "1")
                    form("action", "delete-users")
                    form("user-id[$userId]", userId.toString())
                }
            }
        }

        sealed class Reputation : Forum() {
            init {
                path("index.php")
            }

            data class Edit(val postId: Int, val userId: Int, val type: String, val message: String) : Reputation() {
                init {
                    form("act", "rep")
                    form("mid", userId.toString())
                    form("type", type)
                    form("message", message)
                    if (postId > 0) {
                        form("p", postId.toString())
                    }
                }
            }

            data class GetPage(val userId: Int, val mode: String, val order: String, val st: Int) : Reputation() {
                init {
                    query("act", "rep")
                    query("view", "history")
                    query("mid", userId.toString())
                    query("mode", mode)
                    query("order", order)
                    query("st", st.toString())
                }
            }
        }
    }

    data class Search(val settings: SearchSettings) : ApiRequest() {

        init {
            if (settings.resourceType == RESOURCE_NEWS.first) {
                path("page")
                path(settings.st.toString())
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
                    query("forums[]", it.toString())
                }
                settings.topics.forEach {
                    query("topics[]", it.toString())
                }
                settings.subforums?.also {
                    query("subforums", it)
                }
                query("noform", "1")
                query("st", settings.st.toString())
                query(ARG_EXCLUDE_TRASH, settings.excludeTrash.toString())
            }
        }
    }

}