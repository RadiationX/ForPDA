package forpdateam.ru.forpda.presentation.qms.chat

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

//TODO добавить стратегии
@StateStrategyType(SkipStrategy::class)
interface QmsChatView : IBaseView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStyleType(type: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setFontSize(size: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setChatMode(mode: String)

    @StateStrategyType(SkipStrategy::class)
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
