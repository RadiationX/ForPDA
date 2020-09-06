package forpdateam.ru.forpda.presentation.qms.chat

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage

/**
 * Created by radiationx on 01.01.18.
 */

//TODO добавить стратегии
interface QmsChatView : IBaseView {
    fun setStyleType(type: String)

    fun setFontSize(size: Int)

    fun setChatMode(mode: String)

    fun onShowSearchRes(res: List<ForumUser>)
    fun showChat(data: QmsChatModel)
    fun onNewThemeCreate(data: QmsChatModel)
    fun onSentMessage(items: List<QmsMessage>)
    fun onNewMessages(items: List<QmsMessage>)
    fun setMessageRefreshing(isRefreshing: Boolean)
    fun onBlockUser(res: Boolean)
    fun showCreateNote(name: String, nick: String, url: String)
    fun onUploadFiles(items: List<AttachmentItem>)
    fun showAvatar(avatarUrl: String)
    fun showMoreMessages(items: List<QmsMessage>, startIndex: Int, endIndex: Int)
    fun makeAllRead()
    fun setTitles(title: String, nick: String)
    fun temp_sendNewTheme()
    fun temp_sendMessage()
}
