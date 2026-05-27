package forpdateam.ru.forpda.presentation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.ActivityScreen
import com.github.terrakok.cicerone.androidx.FragmentScreen
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.activities.SettingsActivity
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.activities.updatechecker.UpdateCheckerActivity
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
import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Link

sealed class Screen : com.github.terrakok.cicerone.Screen {
    companion object {
        const val ARG_TITLE = "arg_title"
        const val ARG_SUBTITLE = "arg_subtitle"
        private val NO_ID = -1
    }

    open var screenTitle: String? = null
    open var screenSubTitle: String? = null
    open var fromMenu: Boolean = false
    open var isAlone: Boolean = false

    fun getKey(): String = this::class.java.simpleName

    /* Activities */

    class Main : Screen(), ActivityScreen {
        override fun createIntent(context: Context): Intent {
            return MainActivity.newIntent(context)
        }
    }

    class UpdateChecker : Screen(), ActivityScreen {
        override fun createIntent(context: Context): Intent {
            return UpdateCheckerActivity.newIntent(context)
        }
    }

    class ImageViewer(
        private val urls: List<String>,
        private val selectedUrl: String? = null
    ) : Screen(), ActivityScreen {
        override fun createIntent(context: Context): Intent {
            return ImageViewerActivity.createIntent(context, urls, selectedUrl)
        }
    }

    class Settings : Screen(), ActivityScreen {
        override fun createIntent(context: Context): Intent {
            return SettingsActivity.newIntent(context)
        }
    }

    /* Fragments */
    class Auth : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return AuthFragment.newInstance()
        }
    }

    class DevDbBrands(
        private val categoryId: DevDbCategoryId?
    ) : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return BrandsFragment.newInstance(categoryId)
        }
    }

    class DevDbDevices(
        private val devicesId: DevDbDevicesId
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return DevicesFragment.newInstance(devicesId)
        }
    }

    class DevDbDevice(
        private val deviceId: DevDbDeviceId
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return DeviceFragment.newInstance(deviceId)
        }
    }

    class DevDbSearch(
        private val text: String?
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return DevDbSearchFragment.newInstance(text)
        }
    }

    sealed class EditPost : Screen(), FragmentScreen {
        class Create(
            private val editPostForm: EditPostForm,
            private val themeName: String,
        ) : EditPost() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return EditPostFragment.newInstanceCreate(editPostForm, themeName)
            }
        }

        class Edit(
            private val postId: PostId,
            private val topicId: TopicId,
            private val forumId: ForumId,
            private val offset: PageOffset,
            private val themeName: String,
        ) : EditPost() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return EditPostFragment.newInstanceEdit(postId, topicId, forumId, offset, themeName)
            }
        }
    }

    class Favorites : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return FavoritesFragment.newInstance()
        }
    }

    class Forum(
        private val forumId: ForumId?
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return ForumFragment.newInstance(forumId)
        }
    }

    class History : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return HistoryFragment.newInstance()
        }
    }

    class Mentions : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return MentionsFragment.newInstance()
        }
    }

    class ArticleList : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return NewsMainFragment.newInstance()
        }
    }

    sealed class ArticleDetail : Screen(), FragmentScreen {
        class FromLink(
            private val link: Link.Site.Details
        ) : ArticleDetail() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return NewsDetailsFragment.newInstanceLink(link)
            }
        }

        class FromList(
            private val articleId: ArticleId,
            private val title: String,
            private val authorNick: String,
            private val date: String,
            private val imageUrl: String,
            private val commentsCount: Int
        ) : ArticleDetail() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return NewsDetailsFragment.newInstanceList(articleId, title, authorNick, date, imageUrl, commentsCount)
            }
        }
    }

    class Notes : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return NotesFragment.newInstance()
        }
    }

    class Announce(
        private val announceId: AnnounceId
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return AnnounceFragment.newInstance(announceId)
        }
    }

    class ForumRules : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return ForumRulesFragment.newInstance()
        }
    }

    class GoogleCaptcha : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return GoogleCaptchaFragment.newInstance()
        }
    }

    class Profile(
        private val userId: UserId
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return ProfileFragment.newInstance(userId)
        }
    }

    class QmsContacts : Screen(), FragmentScreen {
        override var isAlone: Boolean = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return QmsContactsFragment.newInstance()
        }
    }

    class QmsBlackList : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return QmsBlackListFragment.newInstance()
        }
    }

    class QmsThemes(
        private val userId: UserId
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return QmsThemesFragment.newInstance(userId)
        }
    }

    sealed class QmsChat : Screen(), FragmentScreen {
        class Create(
            private val userId: UserId?
        ) : QmsChat() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return QmsChatFragment.newInstanceCreate(userId)
            }
        }

        class Existed(
            private val chatId: QmsChatId
        ) : QmsChat() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return QmsChatFragment.newInstanceExisted(chatId)
            }
        }
    }

    class Reputation(
        private val link: Link.Board.Reputation.History
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return ReputationFragment.newInstance(link)
        }
    }

    sealed class Search : Screen(), FragmentScreen {
        class Default : Search() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return SearchFragment.newInstance()
            }
        }

        class Site(
            private val link: Link.Site.Search
        ) : Search() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return SearchFragment.newInstanceSite(link)
            }
        }

        class Forum(
            private val link: Link.Board.Search
        ) : Search() {
            override fun createFragment(factory: FragmentFactory): Fragment {
                return SearchFragment.newInstanceBoard(link)
            }
        }
    }

    class Theme(
        private val link: Link.Board.Topic
    ) : Screen(), FragmentScreen {
        companion object {
            const val CODE_RESULT_SYNC = "10"
            const val CODE_RESULT_PAGE = "11"
        }

        override fun createFragment(factory: FragmentFactory): Fragment {
            return ThemeFragmentWeb.newInstance(link)
        }
    }

    class Topics(
        private val link: Link.Board.Forum
    ) : Screen(), FragmentScreen {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return TopicsFragment.newInstance(link)
        }
    }

    class OtherMenu : Screen(), FragmentScreen {
        override var fromMenu = true
        override var isAlone = true
        override fun createFragment(factory: FragmentFactory): Fragment {
            return OtherFragment.newInstance()
        }
    }

}