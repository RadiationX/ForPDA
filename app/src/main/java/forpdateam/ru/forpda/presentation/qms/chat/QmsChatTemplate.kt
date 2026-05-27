package forpdateam.ru.forpda.presentation.qms.chat

import biz.source_code.miniTemplator.MiniTemplator
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.TemplateManager
import javax.inject.Inject

class QmsChatTemplate @Inject constructor(
    private val templateManager: TemplateManager
) {

    fun mapEntity(chatModel: QmsChatModel): QmsChatModel {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_QMS_CHAT)
        val endIndex = chatModel.messages.size
        val startIndex = Math.max(endIndex - 30, 0)

        template.apply {
            templateManager.fillStaticStrings(this)
            setVariableOpt("style_type", templateManager.getThemeType())
            setVariableOpt("chat_title", ApiUtils.htmlEncode(chatModel.title))
            setVariableOpt("threadId", chatModel.id.threadId.id)
            setVariableOpt("userId", chatModel.id.userId.id)
            setVariableOpt("nick", chatModel.user.nick)
            setVariableOpt("avatarUrl", chatModel.user.avatar)

            val messTemplate = templateManager.getTemplate(TemplateManager.TEMPLATE_QMS_CHAT_MESS)
            templateManager.fillStaticStrings(messTemplate)
            generateMessages(messTemplate, chatModel.messages, startIndex, endIndex)
            setVariableOpt("messages", messTemplate.generateOutput())
            messTemplate.reset()
        }

        val result = template.generateOutput()
        template.reset()
        return chatModel.copy(
            html = result.asDeferredData(),
            showedMessIndex = startIndex
        )
    }

    fun generateHtmlBase(): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_QMS_CHAT)
        template.apply {
            templateManager.fillStaticStrings(this)
            setVariableOpt("style_type", templateManager.getThemeType())
            setVariableOpt("body_type", "qms")
            setVariableOpt("messages", "")
        }

        val result = template.generateOutput()
        template.reset()
        return result
    }

    fun generate(messages: List<QmsMessage>): String {
        return generate(messages, 0, messages.size)
    }

    fun generate(messages: List<QmsMessage>, startIndex: Int, endIndex: Int): String {
        val template = templateManager.getTemplate(TemplateManager.TEMPLATE_QMS_CHAT_MESS)
        template.apply {
            templateManager.fillStaticStrings(this)
            generateMessages(template, messages, startIndex, endIndex)
        }
        val result = template.generateOutput()
        template.reset()
        return result
    }

    private fun generateMessages(
        template: MiniTemplator,
        messages: List<QmsMessage>,
        start: Int,
        end: Int
    ): MiniTemplator {
        for (i in start until end) {
            generateMessage(template, messages[i])
        }
        return template
    }

    private fun generateMessage(template: MiniTemplator, mess: QmsMessage): MiniTemplator {
        template.apply {
            when (mess) {
                is QmsMessage.Date -> {
                    setVariableOpt("date", mess.date)
                    addBlockOpt("date")
                }

                is QmsMessage.Regular -> {
                    setVariableOpt("from_class", if (mess.isMyMessage) "our" else "his")
                    setVariableOpt("unread_class", if (mess.readStatus) "" else "unread")
                    setVariableOpt("mess_id", mess.id.id)
                    setVariableOpt("content", mess.content)
                    setVariableOpt("time", mess.time)
                    addBlockOpt("mess")
                }
            }
            addBlockOpt("item")
        }
        return template
    }

}