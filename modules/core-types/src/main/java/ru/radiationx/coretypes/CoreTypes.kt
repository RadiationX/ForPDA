package ru.radiationx.coretypes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface CoreType : Parcelable

@Parcelize
data class PageNumber(val value: Int) : CoreType {
    companion object {
        val default = PageNumber(1)
    }
}

@Parcelize
data class PageOffset(val value: Int) : CoreType {
    companion object {
        val default = PageOffset(0)
    }
}

@Parcelize
data class ArticleId(val id: Int) : CoreType

@Parcelize
data class CommentId(val id: Int) : CoreType

@Parcelize
data class DevDbCategoryId(val id: String) : CoreType

@Parcelize
data class DevDbBrandId(val id: String) : CoreType

@Parcelize
data class DevDbDevicesId(val categoryId: DevDbCategoryId, val brandId: DevDbBrandId) : CoreType

@Parcelize
data class DevDbDeviceId(val id: String) : CoreType

@Parcelize
data class UserId(val id: Int) : CoreType

@Parcelize
data class ForumId(val id: Int) : CoreType

@Parcelize
data class FavoriteId(val id: Int) : CoreType

@Parcelize
data class AnnounceId(val forumId: ForumId, val st: Int) : CoreType

@Parcelize
data class QmsThreadId(val id: Int) : CoreType

@Parcelize
data class QmsChatId(val userId: UserId, val threadId: QmsThreadId) : CoreType

@Parcelize
data class TopicId(val id: Int) : CoreType

@Parcelize
data class PostId(val id: Int) : CoreType
