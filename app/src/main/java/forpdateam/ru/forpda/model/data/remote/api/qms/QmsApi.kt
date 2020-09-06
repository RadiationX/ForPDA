package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern


/**
 * Created by radiationx on 29.07.16.
 */
class QmsApi(
        private val webClient: IWebClient,
        private val qmsParser: QmsParser
) {

    private val imgBbPattern = Pattern.compile("PF\\.obj\\.config\\.json_api=\"([^\"]*?)\"[\\s\\S]*?PF\\.obj\\.config\\.auth_token=\"([^\"]*?)\"")

    fun getBlackList(): List<QmsContact> {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&settings=blacklist")
                .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    fun getContactList(): List<QmsContact> {
        val response = webClient.request(NetworkRequest.Builder().url("https://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist").build())
        return qmsParser.parseContacts(response.body)
    }

    fun unBlockUsers(id: Int): List<QmsContact> {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "delete-users")
        val strId = Integer.toString(id)
        builder.formHeader("user-id[$strId]", strId)
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    fun blockUser(nick: String): List<QmsContact> {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "add-user")
                .formHeader("username", nick)
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    fun getThemesList(id: Int): QmsThemes {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&mid=$id")
                .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseThemes(response.body, id)
    }

    fun deleteTheme(id: Int, themeId: Int): QmsThemes {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&mid=$id&xhr=body&do=1")
                .formHeader("xhr", "body")
                .formHeader("action", "delete-threads")
                .formHeader("thread-id[$themeId]", themeId.toString())
        val response = webClient.request(builder.build())
        return qmsParser.parseThemes(response.body, id)
    }

    fun getChat(userId: Int, themeId: Int): QmsChatModel {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&mid=$userId&t=$themeId")
                .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseChat(response.body)
    }

    fun findUser(nick: String): List<ForumUser> {
        val encodedNick = URLEncoder.encode(nick, "UTF-8")
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=$encodedNick")
                .xhrHeader()
        val response = webClient.request(builder.build())
        return qmsParser.parseSearch(response.body)
    }

    fun sendNewTheme(nick: String, title: String, mess: String, files: List<AttachmentItem>): QmsChatModel {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1")
                .formHeader("username", nick)
                .formHeader("title", title)
                .formHeader("message", mess)
                .formHeader("attaches", files.joinToString { it.id.toString() })
        val response = webClient.request(builder.build())
        return qmsParser.parseChat(response.body)
    }

    fun sendMessage(userId: Int, themeId: Int, text: String, files: List<AttachmentItem>): List<QmsMessage> {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "send-message")
                .formHeader("message", text)
                .formHeader("mid", Integer.toString(userId))
                .formHeader("t", Integer.toString(themeId))
                .formHeader("attaches", files.joinToString { it.id.toString() })
        val response = webClient.request(builder.build())
        return qmsParser.sendMessage(response.body)
    }

    fun getMessagesFromWs(themeId: Int, messageId: Int, afterMessageId: Int): List<QmsMessage> {
        val messInfoBuilder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms-xhr&")
                .formHeader("action", "message-info")
                .formHeader("t", Integer.toString(themeId))
                .formHeader("msg-id", Integer.toString(messageId))
        val messInfoResponse = webClient.request(messInfoBuilder.build())
        val userId = qmsParser.parseUserFromWebSocket(messInfoResponse.body)
        return getMessagesAfter(userId, themeId, afterMessageId)
    }

    fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): List<QmsMessage> {
        val threadMessagesBuilder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=qms-xhr&")
                .xhrHeader()
                .formHeader("action", "get-thread-messages")
                .formHeader("mid", Integer.toString(userId))
                .formHeader("t", Integer.toString(themeId))
                .formHeader("after-message", Integer.toString(afterMessageId))
        val response = webClient.request(threadMessagesBuilder.build())
        return qmsParser.parseMoreMessages(response.body)
    }

    fun deleteDialog(mid: Int): String {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "del-member")
                .formHeader("del-mid", Integer.toString(mid))
        return webClient.request(builder.build()).body
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>): List<AttachmentItem> {
        val baseUrl = "https://ru.imgbb.com/"
        var uploadUrl = "https://ru.imgbb.com/json"
        var authToken = "null"

        val baseResponse = webClient.get(baseUrl)
        val baseMatcher = imgBbPattern.matcher(baseResponse.body)
        if (baseMatcher.find()) {
            uploadUrl = baseMatcher.group(1)
            authToken = baseMatcher.group(2)
        }


        val headers = HashMap<String, String>()
        headers["type"] = "file"
        headers["action"] = "upload"
        headers["privacy"] = "undefined"
        headers["timestamp"] = java.lang.Long.toString(System.currentTimeMillis())
        headers["auth_token"] = authToken
        headers["nsfw"] = "0"
        //Matcher matcher = null;
        for (i in files.indices) {
            val file = files[i]
            val item = pending[i]

            file.requestName = "source"
            val builder = NetworkRequest.Builder()
                    .url(uploadUrl)
                    .formHeaders(headers)
                    .file(file)
            val response = webClient.request(builder.build(), item.itemProgressListener)

            val responseJson = JSONObject(response.body)
            forpdateam.ru.forpda.common.Utils.longLog(responseJson.toString(4))
            if (responseJson.getInt("status_code") == 200) {
                val imageJson = responseJson.getJSONObject("image")
                item.name = imageJson.getString("filename")
                item.id = 0
                item.extension = imageJson.getString("extension")
                item.weight = imageJson.getString("size_formatted")
                item.typeFile = AttachmentItem.TYPE_IMAGE
                item.loadState = AttachmentItem.STATE_LOADED
                item.imageUrl = imageJson.getJSONObject("medium").getString("url")
                item.url = imageJson.getJSONObject("image").getString("url")
            }
        }

        return pending
    }

}
