package forpdateam.ru.forpda.ui.views.drawers.adapters

import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel

sealed class ListItem

class NoteListItem(val item: NoteItem) : ListItem()

class CloseableInfoListItem(val item: CloseableInfo) : ListItem()
class ProfileListItem(val profileItem: ProfileModel?) : ListItem()
class MenuListItem(val menuItem: DrawerMenuItem) : ListItem()
class DividerShadowListItem : ListItem()

class BottomTabListItem(val item: DrawerMenuItem, var selected: Boolean = false) : ListItem()

class AttachmentListItem(val item: AttachmentItem) : ListItem()
class AttachmentSelectorListItem(var isLinear: Boolean, var isReverse: Boolean) : ListItem()

