package forpdateam.ru.forpda.entity.remote.inspector

import forpdateam.ru.forpda.entity.remote.others.user.User

sealed interface InspectorItem {

    data class Theme(
        val topicId: Int,
        val timeStamp: Long,
        val lastTimeStamp: Long,
        val msgCount: Int,
        val isImportant: Boolean,
        val sourceTitle: String,
        val user: User,
    ) : InspectorItem

    data class Qms(
        val themeId: Int,
        val timeStamp: Long,
        val msgCount: Int,
        val sourceTitle: String,
        val user: User,
    ) : InspectorItem

}