package forpdateam.ru.forpda.model.interactors.other

import android.content.SharedPreferences
import android.util.Log
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.presentation.Screen
import io.reactivex.Observable

class MenuRepository(
        private val preferences: SharedPreferences,
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
            AppMenuItem(item_dev_db, Screen.DevDbBrands()),
            AppMenuItem(item_forum, Screen.Forum()),
            AppMenuItem(item_search, Screen.Search()),
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

    private val menuRelay = BehaviorRelay.create<Map<Int, List<AppMenuItem>>>()

    private var localCounters = MessageCounters()

    private val rxPreferences = RxSharedPreferences.create(preferences)

    private val menuSequence by lazy {
        rxPreferences.getString("menu_items_sequence")
    }

    init {
        allItems.forEach { it.screen?.fromMenu = true }

        loadMainMenuGroup()
        menuSequence
                .asObservable()
                .subscribe {
                    Log.e("kulolo", "menuSequence pref change")
                    loadMainMenuGroup()
                    updateMenuItems()
                }

        authHolder
                .observe()
                .subscribe {
                    loadMainMenuGroup()
                    Log.e("lplplp", "MenuRepository observe auth ${it.state.toString()}")
                    updateMenuItems()
                }

        countersHolder
                .observe()
                .subscribe { counters ->
                    localCounters = counters
                    updateMenuItems()
                }
        updateMenuItems()
    }

    private fun loadMainMenuGroup() {
        mainGroupSequence.clear()
        mainGroupSequence.addAll(GROUP_MAIN)

        menuSequence.get().also { savedArray ->
            if(savedArray.isNotEmpty()){
                val array = savedArray.split(',').map { it.toInt() }.filter { GROUP_MAIN.contains(it) }
                val newItems = GROUP_MAIN.filterNot { array.contains(it) }
                val finalArray = newItems.plus(array)
                Log.e("lplplp", "MainRepository init saved ${newItems.size}=${newItems.joinToString { it.toString() }}")
                mainGroupSequence.clear()
                mainGroupSequence.addAll(finalArray)
            }
        }
    }

    fun observerMenu(): Observable<Map<Int, List<AppMenuItem>>> = menuRelay.hide()

    fun setMainMenuSequence(items: List<AppMenuItem>) {
        mainGroupSequence.clear()
        mainGroupSequence.addAll(items.map { it.id })
        menuSequence.set(mainGroupSequence.joinToString(",") { it.toString() })
        updateMenuItems()
    }

    fun setLastOpened(id: Int) {
        if (GROUP_MAIN.indexOfFirst { it == id } >= 0) {
            preferences.edit().putInt("app_menu_last_id", id).apply()
        }
    }

    fun getLastOpened(): Int {
        val menuId = preferences.getInt("app_menu_last_id", -1)
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

        allItems.firstOrNull { it.id == item_qms_contacts }?.count = localCounters.qms
        allItems.firstOrNull { it.id == item_mentions }?.count = localCounters.mentions
        allItems.firstOrNull { it.id == item_favorites }?.count = localCounters.favorites

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

        menuRelay.accept(mapOf(
                group_main to mainMenu,
                group_system to systemMenu,
                group_link to linkMenu
        ))
    }

}