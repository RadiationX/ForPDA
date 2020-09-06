package forpdateam.ru.forpda.model

import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem

object MenuMapper {

    fun mapToDrawer(item: AppMenuItem): DrawerMenuItem = DrawerMenuItem(
            getTitle(item),
            getIcon(item),
            item
    )

    fun getTitle(item: AppMenuItem): Int = when (item.id) {
        MenuRepository.item_auth -> R.string.fragment_title_auth
        MenuRepository.item_article_list -> R.string.fragment_title_news_list
        MenuRepository.item_favorites -> R.string.fragment_title_favorite
        MenuRepository.item_qms_contacts -> R.string.fragment_title_contacts
        MenuRepository.item_mentions -> R.string.fragment_title_mentions
        MenuRepository.item_dev_db -> R.string.fragment_title_devdb
        MenuRepository.item_forum -> R.string.fragment_title_forum
        MenuRepository.item_search -> R.string.fragment_title_search
        MenuRepository.item_history -> R.string.fragment_title_history
        MenuRepository.item_notes -> R.string.fragment_title_notes
        MenuRepository.item_forum_rules -> R.string.fragment_title_forum_rules
        MenuRepository.item_settings -> R.string.activity_title_settings
        MenuRepository.item_other_menu -> R.string.fragment_title_other_menu
        MenuRepository.item_link_forum_author -> R.string.menu_item_link_forum_author
        MenuRepository.item_link_chat_telegram -> R.string.menu_item_link_chat_telegram
        MenuRepository.item_link_forum_topic -> R.string.menu_item_link_forum_topic
        MenuRepository.item_link_forum_faq -> R.string.menu_item_link_forum_faq
        MenuRepository.item_link_play_market -> R.string.menu_item_link_play_market
        MenuRepository.item_link_github -> R.string.menu_item_link_github
        MenuRepository.item_link_bitbucket -> R.string.menu_item_link_bitbucket
        else -> R.string.error
    }

    fun getIcon(item: AppMenuItem): Int = when (item.id) {
        MenuRepository.item_auth -> R.drawable.ic_person_add
        MenuRepository.item_article_list -> R.drawable.ic_newspaper
        MenuRepository.item_favorites -> R.drawable.ic_star
        MenuRepository.item_qms_contacts -> R.drawable.ic_contacts
        MenuRepository.item_mentions -> R.drawable.ic_notifications
        MenuRepository.item_dev_db -> R.drawable.ic_devices_other
        MenuRepository.item_forum -> R.drawable.ic_forum
        MenuRepository.item_search -> R.drawable.ic_search
        MenuRepository.item_history -> R.drawable.ic_history
        MenuRepository.item_notes -> R.drawable.ic_bookmark
        MenuRepository.item_forum_rules -> R.drawable.ic_book_open
        MenuRepository.item_settings -> R.drawable.ic_settings
        MenuRepository.item_other_menu -> R.drawable.ic_toolbar_hamburger
        MenuRepository.item_link_forum_author -> R.drawable.ic_account_circle
        MenuRepository.item_link_chat_telegram -> R.drawable.ic_logo_telegram
        MenuRepository.item_link_forum_topic -> R.drawable.ic_logo_4pda
        MenuRepository.item_link_forum_faq -> R.drawable.ic_logo_4pda
        MenuRepository.item_link_play_market -> R.drawable.ic_logo_google_play
        MenuRepository.item_link_github -> R.drawable.ic_logo_github_circle
        MenuRepository.item_link_bitbucket -> R.drawable.ic_logo_bitbucket
        else -> R.drawable.ic_thumb_down
    }
}