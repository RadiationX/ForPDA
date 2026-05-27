package forpdateam.ru.forpda.ui.views.drawers.adapters

import androidx.annotation.StringRes
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.topics.TopicItem

sealed class ListItem

class NoteListItem(val item: NoteItem) : ListItem()

class CloseableInfoListItem(val item: CloseableInfo) : ListItem()
class ProfileListItem(val user: ForumUser?) : ListItem()
class MenuListItem(val menuItem: DrawerMenuItem) : ListItem()
class DividerShadowListItem : ListItem()

class BottomTabListItem(val item: DrawerMenuItem, var selected: Boolean = false) : ListItem()

class AttachmentListItem(val item: AttachmentItem) : ListItem()
class AttachmentSelectorListItem(var isLinear: Boolean, var isReverse: Boolean) : ListItem()

sealed class SectionListItem(val topDivider: Boolean) : ListItem() {
    class String(val title: kotlin.String, topDivider: Boolean) : SectionListItem(topDivider)
    class Res(@param:StringRes val titleRes: Int, topDivider: Boolean) : SectionListItem(topDivider)
}

class TopicListItem(val item: TopicItem.Topic) : ListItem()
class TopicAnnounceListItem(val item: TopicItem.Announce) : ListItem()
class TopicForumListItem(val item: TopicItem.Forum) : ListItem()
class FavoriteListItem(val item: Favorite, val showDot: Boolean) : ListItem()
class BrandListItem(val item: Brands.Item) : ListItem()

class ForumListItem(val item: ForumItemFlat, val level: Int, val isLeaf: Boolean, val expanded: Boolean) : ListItem()

class DeviceSpecsListItem(val group: Device.Specs) : ListItem()
class DeviceCommentListItem(val comment: Device.Comment) : ListItem()
class DeviceArticleListItem(val article: Device.Article) : ListItem()
class DeviceTopicListItem(val topic: Device.Topic) : ListItem()

