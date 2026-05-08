package forpdateam.ru.forpda.entity.remote.inspector

import forpdateam.ru.forpda.entity.remote.others.user.User

sealed interface InspectorItem {

    val baseId: Int
    val baseTimeStamp: Long
    val baseRawContent: String

    data class Favorite(
        val topicId: Int,
        val timeStamp: Long,
        val lastTimeStamp: Long,
        val msgCount: Int,
        val isImportant: Boolean,
        val sourceTitle: String,
        val user: User,
        val rawContent: String
    ) : InspectorItem {

        override val baseId: Int = topicId
        override val baseTimeStamp: Long = timeStamp
        override val baseRawContent: String = rawContent
    }

    data class Qms(
        val themeId: Int,
        val timeStamp: Long,
        val msgCount: Int,
        val sourceTitle: String,
        val user: User,
        val rawContent: String
    ) : InspectorItem {

        override val baseId: Int = themeId
        override val baseTimeStamp: Long = timeStamp
        override val baseRawContent: String = rawContent
    }

}