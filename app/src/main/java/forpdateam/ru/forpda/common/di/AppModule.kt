package forpdateam.ru.forpda.common.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.client.AppCookieJar
import forpdateam.ru.forpda.client.AppImageDownloader
import forpdateam.ru.forpda.client.CookieStorage
import forpdateam.ru.forpda.client.NetworkObserver
import forpdateam.ru.forpda.client.WebClientImpl
import forpdateam.ru.forpda.client.websocket.WebSocketController
import forpdateam.ru.forpda.common.UriLinkUrlAdapter
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.apptheme.AppThemeController
import forpdateam.ru.forpda.common.apptheme.AppThemeControllerImpl
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.PatternsStorage
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.forumuser.UserSource
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.cache.notes.NotesCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.db.AppDatabase
import forpdateam.ru.forpda.model.data.db.FavoriteForumsDao
import forpdateam.ru.forpda.model.data.db.FavoriteIdsDao
import forpdateam.ru.forpda.model.data.db.FavoriteTopicsDao
import forpdateam.ru.forpda.model.data.db.FavoritesDao
import forpdateam.ru.forpda.model.data.db.ForumUsersDao
import forpdateam.ru.forpda.model.data.db.ForumsDao
import forpdateam.ru.forpda.model.data.db.HistoryDao
import forpdateam.ru.forpda.model.data.db.NotesDao
import forpdateam.ru.forpda.model.data.db.QmsContactsDao
import forpdateam.ru.forpda.model.data.db.QmsThemesDao
import forpdateam.ru.forpda.model.data.providers.UserSourceImpl
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsParser
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthParser
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerApi
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerParser
import forpdateam.ru.forpda.model.data.remote.api.common.CaptchaParser
import forpdateam.ru.forpda.model.data.remote.api.common.GlobalParser
import forpdateam.ru.forpda.model.data.remote.api.common.LinkHandlerParser
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbParser
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostParser
import forpdateam.ru.forpda.model.data.remote.api.events.WebSocketEventParser
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesParser
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumParser
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorApi
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorParser
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsParser
import forpdateam.ru.forpda.model.data.remote.api.news.ArticleParser
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
import forpdateam.ru.forpda.model.data.remote.api.patterns.PatternsApi
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileParser
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsParser
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationParser
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import forpdateam.ru.forpda.model.data.remote.api.search.SearchParser
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeParser
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsApi
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsParser
import forpdateam.ru.forpda.model.data.storage.ExternalStorage
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.interactors.events.EventsController
import forpdateam.ru.forpda.model.interactors.events.NotificationEventSender
import forpdateam.ru.forpda.model.interactors.events.handlers.CountersEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.FavoritesEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.NotificationEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.QmsEventsHandler
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.preferences.ListsPreferencesHolder
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.checker.CheckerRepository
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.model.repository.events.WebSocketEventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.model.repository.note.NotesRepository
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.search.SearchRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository
import forpdateam.ru.forpda.model.system.ExternalStorageImpl
import forpdateam.ru.forpda.model.system.PatternProviderImpl
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.ErrorHandlerImpl
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.LinkHandlerImpl
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.SystemLinkHandlerImpl
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.announce.AnnounceTemplate
import forpdateam.ru.forpda.presentation.articles.detail.ArticleTemplate
import forpdateam.ru.forpda.presentation.forumrules.ForumRulesTemplate
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatTemplate
import forpdateam.ru.forpda.presentation.search.SearchTemplate
import forpdateam.ru.forpda.presentation.theme.ThemeTemplate
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import ru.mintrocket.lib.mintpermissions.MintPermissions
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.radiationx.flowpreferences.FlowPreferences
import ru.radiationx.links.parser.LinkTransformer
import ru.radiationx.links.url.LinkUrlAdapter
import ru.radiationx.quill.QuillModule
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier
import kotlin.time.Duration.Companion.seconds

class AppModule(
    application: App
) : QuillModule() {
    init {
        val linkUrlAdapter = UriLinkUrlAdapter()

        instance<App> { application }
        instance<Application> { application }
        instance<Context> { application }

        single<CustomWebViewClient>()

        instance<MintPermissionsController> { MintPermissions.controller }
        singleImpl<AppThemeController, AppThemeControllerImpl>()


        val cicerone: Cicerone<TabRouter> by lazy { Cicerone.create(TabRouter()) }
        instance<TabRouter> { cicerone.router }
        instance<NavigatorHolder> { cicerone.getNavigatorHolder() }

        single<Utils>()

        instance<LinkUrlAdapter> { linkUrlAdapter }
        instance { LinkTransformer(linkUrlAdapter) }
        singleImpl<SystemLinkHandler, SystemLinkHandlerImpl>()
        singleImpl<LinkHandler, LinkHandlerImpl>()

        singleProvider<SharedPreferences, PreferencesProvider>()
        singleProvider<SharedPreferences, DataPreferencesProvider>(DataPreferences::class)

        singleProvider<FlowPreferences, FlowPreferencesProvider>()
        singleProvider<FlowPreferences, FlowDataPreferencesProvider>(DataPreferences::class)

        singleImpl<ErrorHandler, ErrorHandlerImpl>()
        singleImpl<ExternalStorage, ExternalStorageImpl>()
        single<CookieStorage>()
        single<AppCookieJar>()
        single<AuthHolder>()
        single<CountersHolder>()
        single<CloseableInfoHolder>()
        single<TemplateManager>()
        single<ThemeTemplate>()
        single<ArticleTemplate>()
        single<SearchTemplate>()
        single<ForumRulesTemplate>()
        single<AnnounceTemplate>()
        single<QmsChatTemplate>()

        singleProvider<OkHttpClient, MainOkHttpProvider>()
        singleProvider<OkHttpClient, ImagesOkHttpProvider>(ImagesOkHttpClient::class)

        single<AppImageDownloader>()
        singleImpl<WebClient, WebClientImpl>()

        instance {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
        }

        single<PatternsStorage>()
        singleImpl<PatternProvider, PatternProviderImpl>()

        single<AuthParser>()
        single<DevDbParser>()
        single<ThemeParser>()
        single<EditPostParser>()
        single<FavoritesParser>()
        single<ForumParser>()
        single<MentionsParser>()
        single<ArticleParser>()
        single<ProfileParser>()
        single<QmsParser>()
        single<ReputationParser>()
        single<SearchParser>()
        single<TopicsParser>()
        single<CheckerParser>()
        single<AttachmentsParser>()
        single<CaptchaParser>()
        single<GlobalParser>()
        single<LinkHandlerParser>()
        single<PaginationParser>()

        single<AuthApi>()
        single<DevDbApi>()
        single<ThemeApi>()
        single<EditPostApi>()
        single<FavoritesApi>()
        single<ForumApi>()
        single<MentionsApi>()
        single<NewsApi>()
        single<ProfileApi>()
        single<QmsApi>()
        single<ReputationApi>()
        single<SearchApi>()
        single<TopicsApi>()
        single<CheckerApi>()
        single<AttachmentsApi>()
        single<PatternsApi>()

        val database by lazy {
            Room
                .databaseBuilder(
                    context = application,
                    klass = AppDatabase::class.java,
                    name = "forpda-room"
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build()
        }
        instance<AppDatabase> { database }
        instance<RoomDatabase> { database }
        instance<FavoritesDao> { database.favoritesDao() }
        instance<FavoriteIdsDao> { database.favoritesIdsDao() }
        instance<FavoriteTopicsDao> { database.favoriteTopicsDao() }
        instance<FavoriteForumsDao> { database.favoriteForumsDao() }
        instance<ForumsDao> { database.forumsDao() }
        instance<ForumUsersDao> { database.forumUsersDao() }
        instance<HistoryDao> { database.historyDao() }
        instance<NotesDao> { database.notesDao() }
        instance<QmsContactsDao> { database.qmsContactsDao() }
        instance<QmsThemesDao> { database.qmsThemesDao() }

        singleImpl<UserSource, UserSourceImpl>()
        single<ForumUsersCache>()
        single<FavoritesCache>()
        single<ForumCache>()
        single<HistoryCache>()
        single<QmsCache>()
        single<NotesCache>()


        single<AvatarRepository>()
        single<FavoritesRepository>()
        single<HistoryRepository>()
        single<MentionsRepository>()
        single<AuthRepository>()
        single<ProfileRepository>()
        single<ReputationRepository>()
        single<ForumRepository>()
        single<TopicsRepository>()
        single<ThemeRepository>()
        single<QmsRepository>()
        single<SearchRepository>()
        single<NewsRepository>()
        single<DevDbRepository>()
        single<PostEditorRepository>()
        single<NotesRepository>()
        single<MenuRepository>()
        single<CheckerRepository>()
        single<NetworkObserver>()
        single<WebSocketController>()
        single<WebSocketEventParser>()
        single<WebSocketEventsRepository>()
        single<InspectorParser>()
        single<InspectorApi>()

        single<InspectorRepository>()

        single<NotificationEventSender>()

        single<CountersEventsHandler>()
        single<FavoritesEventsHandler>()
        single<QmsEventsHandler>()
        single<NotificationEventsHandler>()
        single<EventsController>()

        single<OtherPreferencesHolder>()
        single<MainPreferencesHolder>()
        single<TopicPreferencesHolder>()
        single<ListsPreferencesHolder>()
        single<NotificationPreferencesHolder>()

        single<CrossScreenInteractor>()
    }

    class PreferencesProvider @Inject constructor(
        private val context: Context,
    ) : Provider<SharedPreferences> {
        @Suppress("DEPRECATION")
        override fun get(): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    class DataPreferencesProvider @Inject constructor(
        private val context: Context,
    ) : Provider<SharedPreferences> {
        override fun get(): SharedPreferences {
            return context.getSharedPreferences(
                "${context.packageName}_data_storage",
                Context.MODE_PRIVATE
            )
        }
    }

    class FlowPreferencesProvider @Inject constructor(
        private val sharedPreferences: SharedPreferences,
    ) : Provider<FlowPreferences> {
        @Suppress("DEPRECATION")
        override fun get(): FlowPreferences {
            return FlowPreferences(GlobalScope, sharedPreferences)
        }
    }

    class FlowDataPreferencesProvider @Inject constructor(
        @param:DataPreferences private val sharedPreferences: SharedPreferences,
    ) : Provider<FlowPreferences> {
        override fun get(): FlowPreferences {
            return FlowPreferences(GlobalScope, sharedPreferences)
        }
    }

    class MainOkHttpProvider @Inject constructor(
        private val cookieJar: AppCookieJar
    ) : Provider<OkHttpClient> {
        override fun get(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(45.seconds)
                .writeTimeout(45.seconds)
                .readTimeout(45.seconds)
                .cookieJar(cookieJar)
                .build()
        }
    }

    class ImagesOkHttpProvider @Inject constructor(
        private val cookieJar: AppCookieJar
    ) : Provider<OkHttpClient> {
        override fun get(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(45.seconds)
                .writeTimeout(45.seconds)
                .readTimeout(45.seconds)
                .cookieJar(cookieJar)
                .build()
        }
    }
}


@Qualifier
annotation class DataPreferences

@Qualifier
annotation class ImagesOkHttpClient