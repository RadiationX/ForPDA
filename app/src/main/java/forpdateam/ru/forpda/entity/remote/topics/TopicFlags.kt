package forpdateam.ru.forpda.entity.remote.topics

data class TopicFlags(
    val isPinned: Boolean,
    val isNew: Boolean,
    val isPoll: Boolean,
    val isClosed: Boolean,
)
