package forpdateam.ru.forpda.ui.navigation

import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.androidx.FragmentScreen
import forpdateam.ru.forpda.extensions.putExtra
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.auth.AuthFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brands.BrandsFragment
import forpdateam.ru.forpda.ui.fragments.devdb.device.DeviceFragment
import forpdateam.ru.forpda.ui.fragments.devdb.search.DevDbSearchFragment
import forpdateam.ru.forpda.ui.fragments.editpost.EditPostFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.fragments.forum.ForumFragment
import forpdateam.ru.forpda.ui.fragments.history.HistoryFragment
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsFragment
import forpdateam.ru.forpda.ui.fragments.news.details.NewsDetailsFragment
import forpdateam.ru.forpda.ui.fragments.news.main.NewsMainFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesFragment
import forpdateam.ru.forpda.ui.fragments.other.AnnounceFragment
import forpdateam.ru.forpda.ui.fragments.other.ForumRulesFragment
import forpdateam.ru.forpda.ui.fragments.other.GoogleCaptchaFragment
import forpdateam.ru.forpda.ui.fragments.other.OtherFragment
import forpdateam.ru.forpda.ui.fragments.profile.ProfileFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsBlackListFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsContactsFragment
import forpdateam.ru.forpda.ui.fragments.qms.QmsThemesFragment
import forpdateam.ru.forpda.ui.fragments.qms.chat.QmsChatFragment
import forpdateam.ru.forpda.ui.fragments.reputation.ReputationFragment
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment

object TabHelper {

    fun fillTabInfo(screen: FragmentScreen, fragment: Fragment) {
        if (screen !is Screen) return
        if (fragment !is TabFragment) return
        fragment.putExtra {
            putString(TabFragment.ARG_TAB_TITLE, screen.screenTitle)
            putString(TabFragment.ARG_TAB_SUBTITLE, screen.screenSubTitle)
        }
        fragment.apply {
            configuration.isMenu = screen.fromMenu
            configuration.isAlone = screen.isAlone
        }
    }

    fun findClassByScreen(screen: Screen): Class<out TabFragment> {
        return when (screen) {
            is Screen.Auth -> AuthFragment::class.java
            is Screen.DevDbDevices -> DevicesFragment::class.java
            is Screen.DevDbBrands -> BrandsFragment::class.java
            is Screen.DevDbDevice -> DeviceFragment::class.java
            is Screen.DevDbSearch -> DevDbSearchFragment::class.java
            is Screen.EditPost -> EditPostFragment::class.java
            is Screen.Favorites -> FavoritesFragment::class.java
            is Screen.Forum -> ForumFragment::class.java
            is Screen.History -> HistoryFragment::class.java
            is Screen.Mentions -> MentionsFragment::class.java
            is Screen.ArticleList -> NewsMainFragment::class.java
            is Screen.ArticleDetail -> NewsDetailsFragment::class.java
            is Screen.Notes -> NotesFragment::class.java
            is Screen.Announce -> AnnounceFragment::class.java
            is Screen.ForumRules -> ForumRulesFragment::class.java
            is Screen.GoogleCaptcha -> GoogleCaptchaFragment::class.java
            is Screen.Profile -> ProfileFragment::class.java
            is Screen.QmsContacts -> QmsContactsFragment::class.java
            is Screen.QmsBlackList -> QmsBlackListFragment::class.java
            is Screen.QmsThemes -> QmsThemesFragment::class.java
            is Screen.QmsChat -> QmsChatFragment::class.java
            is Screen.Reputation -> ReputationFragment::class.java
            is Screen.Search -> SearchFragment::class.java
            is Screen.Theme -> ThemeFragmentWeb::class.java
            is Screen.Topics -> TopicsFragment::class.java
            is Screen.OtherMenu -> OtherFragment::class.java
            else -> {
                throw Exception("Not found class by screen: \"$screen\"")
            }
        }
    }

    fun findScreenByFragment(fragment: TabFragment): Class<out Screen> {
        return when (fragment) {
            is AuthFragment -> Screen.Auth::class.java
            is DevicesFragment -> Screen.DevDbDevices::class.java
            is BrandsFragment -> Screen.DevDbBrands::class.java
            is DeviceFragment -> Screen.DevDbDevice::class.java
            is DevDbSearchFragment -> Screen.DevDbSearch::class.java
            is EditPostFragment -> Screen.EditPost::class.java
            is FavoritesFragment -> Screen.Favorites::class.java
            is ForumFragment -> Screen.Forum::class.java
            is HistoryFragment -> Screen.History::class.java
            is MentionsFragment -> Screen.Mentions::class.java
            is NewsMainFragment -> Screen.ArticleList::class.java
            is NewsDetailsFragment -> Screen.ArticleDetail::class.java
            is NotesFragment -> Screen.Notes::class.java
            is AnnounceFragment -> Screen.Announce::class.java
            is ForumRulesFragment -> Screen.ForumRules::class.java
            is GoogleCaptchaFragment -> Screen.GoogleCaptcha::class.java
            is ProfileFragment -> Screen.Profile::class.java
            is QmsContactsFragment -> Screen.QmsContacts::class.java
            is QmsBlackListFragment -> Screen.QmsBlackList::class.java
            is QmsThemesFragment -> Screen.QmsThemes::class.java
            is QmsChatFragment -> Screen.QmsChat::class.java
            is ReputationFragment -> Screen.Reputation::class.java
            is SearchFragment -> Screen.Search::class.java
            is ThemeFragmentWeb -> Screen.Theme::class.java
            is TopicsFragment -> Screen.Topics::class.java
            is OtherFragment -> Screen.OtherMenu::class.java
            else -> {
                throw Exception("Not found class by fragment: \"$fragment\"")
            }
        }
    }
}