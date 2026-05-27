package forpdateam.ru.forpda.model.interactors.other

import android.util.Log
import ru.radiationx.flowpreferences.FlowPreferences
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.presentation.Screen
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class MenuRepository @Inject constructor(
    private val preferences: FlowPreferences,
    private val authHolder: AuthHolder,
    private val countersHolder: CountersHolder
) {

    companion object {

        const val group_main = 10
        const val group_system = 20
        const val group_link = 30

        const val item_auth = 110
        const val item_article_list = 120
        const val item_favorites = 130
        const val item_qms_contacts = 140
        const val item_mentions = 150
        const val item_dev_db = 160
        const val item_forum = 170
        const val item_search = 180
        const val item_history = 190
        const val item_notes = 200
        const val item_forum_rules = 210
        const val item_settings = 220

        const val item_other_menu = 230

        const val item_link_forum_author = 240
        const val item_link_chat_telegram = 250
        const val item_link_forum_topic = 260
        const val item_link_forum_faq = 270
        const val item_link_play_market = 280
        const val item_link_github = 290
        const val item_link_bitbucket = 300

        val GROUP_MAIN = arrayOf(
            item_auth,
            item_article_list,
            item_favorites,
            item_qms_contacts,
            item_search,
            item_mentions,
            item_forum,
            item_dev_db,
            item_history,
            item_notes,
            item_forum_rules
        )

        val GROUP_SYSTEM = arrayOf(
            item_settings
        )

        val GROUP_LINK = arrayOf<Int>(
            item_link_forum_author,
            item_link_forum_topic,
            item_link_forum_faq,
            item_link_chat_telegram,
            item_link_play_market,
            item_link_github,
            item_link_bitbucket
        )
    }

    private val allItems = listOf(
        //AppMenuItem(item_auth, Screen.Auth()),
        AppMenuItem(item_article_list, Screen.ArticleList()),
        AppMenuItem(item_favorites, Screen.Favorites()),
        AppMenuItem(item_qms_contacts, Screen.QmsContacts()),
        AppMenuItem(item_mentions, Screen.Mentions()),
        AppMenuItem(item_dev_db, Screen.DevDbBrands(categoryId = null)),
        AppMenuItem(item_forum, Screen.Forum(forumId = null)),
        AppMenuItem(item_search, Screen.Search.Default()),
        AppMenuItem(item_history, Screen.History()),
        AppMenuItem(item_notes, Screen.Notes()),
        AppMenuItem(item_forum_rules, Screen.ForumRules()),
        AppMenuItem(item_settings, Screen.Settings()),
        AppMenuItem(item_link_forum_author),
        AppMenuItem(item_link_chat_telegram),
        AppMenuItem(item_link_forum_topic),
        AppMenuItem(item_link_forum_faq),
        AppMenuItem(item_link_play_market),
        AppMenuItem(item_link_github),
        AppMenuItem(item_link_bitbucket)
    )

    private val mainGroupSequence = mutableListOf<Int>()

    private val blockedMenu = mutableListOf<Int>()

    private val blockUnAuth = listOf(
        item_favorites,
        item_qms_contacts,
        item_mentions
    )

    private val blockAuth = listOf(
        item_auth
    )

    private val mainMenu = mutableListOf<AppMenuItem>()
    private val systemMenu = mutableListOf<AppMenuItem>()
    private val linkMenu = mutableListOf<AppMenuItem>()

    private val menuState = MutableStateFlow<Map<Int, List<AppMenuItem>>>(emptyMap())

    private var localCounters: MessageCounters? = null


    private val menuSequence by lazy {
        preferences.getString("menu_items_sequence")
    }

    private val menuLastId by lazy {
        preferences.getInt("app_menu_last_id", -1)
    }

    init {
        allItems.forEach { it.screen?.fromMenu = true }

        loadMainMenuGroup()
        menuSequence
            .onEach {
                Log.e("kulolo", "menuSequence pref change")
                loadMainMenuGroup()
                updateMenuItems()
            }
            .launchIn(GlobalScope)


        authHolder
            .observe()
            .onEach {
                loadMainMenuGroup()
                Log.e("lplplp", "MenuRepository observe auth ${it}")
                updateMenuItems()
            }
            .launchIn(GlobalScope)

        countersHolder
            .observe()
            .onEach { counters ->
                localCounters = counters
                updateMenuItems()
            }
            .launchIn(GlobalScope)
        updateMenuItems()
    }

    private fun loadMainMenuGroup() {
        mainGroupSequence.clear()
        mainGroupSequence.addAll(GROUP_MAIN)

        menuSequence.get().also { savedArray ->
            if (!savedArray.isNullOrEmpty()) {
                val array =
                    savedArray.split(',').map { it.toInt() }.filter { GROUP_MAIN.contains(it) }
                val newItems = GROUP_MAIN.filterNot { array.contains(it) }
                val finalArray = newItems.plus(array)
                Log.e(
                    "lplplp",
                    "MainRepository init saved ${newItems.size}=${newItems.joinToString { it.toString() }}"
                )
                mainGroupSequence.clear()
                mainGroupSequence.addAll(finalArray)
            }
        }
    }

    fun observerMenu(): Flow<Map<Int, List<AppMenuItem>>> = menuState

    fun setMainMenuSequence(items: List<AppMenuItem>) {
        mainGroupSequence.clear()
        mainGroupSequence.addAll(items.map { it.id })
        menuSequence.set(mainGroupSequence.joinToString(",") { it.toString() })
        updateMenuItems()
    }

    fun setLastOpened(id: Int) {
        if (GROUP_MAIN.indexOfFirst { it == id } >= 0) {
            menuLastId.set(id)
        }
    }

    fun getLastOpened(): Int {
        val menuId = menuLastId.get()
        return if (GROUP_MAIN.indexOfFirst { it == menuId } >= 0) {
            menuId
        } else {
            -1
        }
    }

    fun getMenuItem(id: Int): AppMenuItem = allItems.first { it.id == id }

    fun menuItemContains(id: Int): Boolean = allItems.indexOfFirst { it.id == id } >= 0

    fun updateMenuItems() {
        mainMenu.clear()
        systemMenu.clear()
        linkMenu.clear()

        allItems.firstOrNull { it.id == item_qms_contacts }?.count = localCounters?.qms ?: 0
        allItems.firstOrNull { it.id == item_mentions }?.count = localCounters?.mentions ?: 0
        allItems.firstOrNull { it.id == item_favorites }?.count = localCounters?.favorites ?: 0

        if (authHolder.get().isAuth()) {
            blockedMenu.addAll(blockAuth)
            blockedMenu.removeAll(blockUnAuth)
        } else {
            blockedMenu.addAll(blockUnAuth)
            blockedMenu.removeAll(blockAuth)
        }

        mainGroupSequence.forEach {
            if (!blockedMenu.contains(it) && menuItemContains(it)) {
                mainMenu.add(getMenuItem(it))
            }
        }

        GROUP_SYSTEM.forEach {
            if (!blockedMenu.contains(it) && menuItemContains(it)) {
                systemMenu.add(getMenuItem(it))
            }
        }

        GROUP_LINK.forEach {
            if (!blockedMenu.contains(it) && menuItemContains(it)) {
                linkMenu.add(getMenuItem(it))
            }
        }

        menuState.value = mapOf(
            group_main to mainMenu,
            group_system to systemMenu,
            group_link to linkMenu
        )
    }

}