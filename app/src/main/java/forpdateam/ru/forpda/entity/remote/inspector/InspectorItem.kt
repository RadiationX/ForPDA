package forpdateam.ru.forpda.entity.remote.inspector

import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId

sealed interface InspectorItem<ID> {

    val baseId: ID
    val baseTimeStamp: Long
    val baseRawContent: String

    data class Favorite(
        val topicId: TopicId,
        val timeStamp: Long,
        val lastTimeStamp: Long,
        val msgCount: Int,
        val isImportant: Boolean,
        val sourceTitle: String,
        val user: User,
        val rawContent: String
    ) : InspectorItem<TopicId> {

        override val baseId: TopicId = topicId
        override val baseTimeStamp: Long = timeStamp
        override val baseRawContent: String = rawContent
    }

    data class Qms(
        val themeId: QmsThreadId,
        val timeStamp: Long,
        val msgCount: Int,
        val sourceTitle: String,
        val user: User,
        val messageId: QmsMessageId,
        val rawContent: String
    ) : InspectorItem<QmsThreadId> {

        override val baseId: QmsThreadId = themeId
        override val baseTimeStamp: Long = timeStamp
        override val baseRawContent: String = rawContent
    }

}