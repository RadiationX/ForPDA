package forpdateam.ru.forpda

import android.content.Context
import android.preference.PreferenceManager
import forpdateam.ru.forpda.client.Client
import forpdateam.ru.forpda.common.DayNightHelper
import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.app.profile.UserHolder
import forpdateam.ru.forpda.model.*
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.cache.notes.NotesCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.providers.UserSourceProvider
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsParser
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerApi
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerParser
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthParser
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbParser
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostParser
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesParser
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumParser
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsParser
import forpdateam.ru.forpda.model.data.remote.api.news.ArticleParser
import forpdateam.ru.forpda.model.data.remote.api.news.NewsApi
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
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.model.preferences.*
import forpdateam.ru.forpda.model.repository.checker.CheckerRepository
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.history.HistoryRepository
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
import forpdateam.ru.forpda.model.system.*
import forpdateam.ru.forpda.presentation.*
import forpdateam.ru.forpda.presentation.announce.AnnounceTemplate
import forpdateam.ru.forpda.presentation.articles.detail.ArticleTemplate
import forpdateam.ru.forpda.presentation.forumrules.ForumRulesTemplate
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatTemplate
import forpdateam.ru.forpda.presentation.search.SearchTemplate
import forpdateam.ru.forpda.presentation.theme.ThemeTemplate
import forpdateam.ru.forpda.ui.DimensionsProvider
import forpdateam.ru.forpda.ui.TemplateManager
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder

/**
 * Created by radiationx on 01.01.18.
 */

class Dependencies internal constructor(
        context: Context
) {

    val dimensionsProvider = DimensionsProvider()

    val defaultIsNight = DayNightHelper.isUiModeNight(context.resources.configuration)
    val dayNightHelper = DayNightHelper(defaultIsNight)

    private val cicerone: Cicerone<TabRouter> by lazy { Cicerone.create(TabRouter()) }
    val router: TabRouter by lazy { cicerone.router }
    val navigatorHolder: NavigatorHolder by lazy { cicerone.navigatorHolder }

    val systemLinkHandler: ISystemLinkHandler by lazy { SystemLinkHandler(context, mainPreferencesHolder, router, authHolder) }
    val linkHandler: ILinkHandler by lazy { LinkHandler(systemLinkHandler) }

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val dataStoragePreferences = context.getSharedPreferences("${context.packageName}_data_storage", Context.MODE_PRIVATE)

    val errorHandler: IErrorHandler by lazy { ErrorHandler(router) }
    val networkState: NetworkStateProvider by lazy { AppNetworkState(context) }
    val schedulers: SchedulersProvider by lazy { AppSchedulers() }

    val externalStorage: ExternalStorageProvider by lazy { ExternalStorage() }

    val authHolder: AuthHolder by lazy { AuthHolder(preferences, schedulers) }
    val countersHolder: CountersHolder by lazy { CountersHolder(preferences, schedulers) }
    val userHolder: IUserHolder by lazy { UserHolder(dataStoragePreferences) }
    val closeableInfoHolder: CloseableInfoHolder by lazy { CloseableInfoHolder(preferences, schedulers) }

    val templateManager by lazy { TemplateManager(context, dayNightHelper) }
    val themeTemplate by lazy { ThemeTemplate(templateManager, authHolder, topicPreferencesHolder) }
    val articleTemplate by lazy { ArticleTemplate(templateManager) }
    val searchTemplate by lazy { SearchTemplate(templateManager, authHolder, topicPreferencesHolder) }
    val forumRulesTemplate by lazy { ForumRulesTemplate(templateManager) }
    val announceTemplate by lazy { AnnounceTemplate(templateManager) }
    val qmsChatTemplate by lazy { QmsChatTemplate(templateManager) }

    val webClient: IWebClient by lazy { Client(context, authHolder, countersHolder) }

    val patternProvider: IPatternProvider by lazy { PatternProvider(context, dataStoragePreferences) }

    val authParser by lazy { AuthParser(patternProvider) }
    val devDbParser by lazy { DevDbParser(patternProvider) }
    val themeParser by lazy { ThemeParser(patternProvider) }
    val editPostParser by lazy { EditPostParser(patternProvider) }
    val favoritesParser by lazy { FavoritesParser(patternProvider) }
    val forumParser by lazy { ForumParser(patternProvider) }
    val mentionsParser by lazy { MentionsParser(patternProvider) }
    val articleParser by lazy { ArticleParser(patternProvider) }
    val profileParser by lazy { ProfileParser(patternProvider) }
    val qmsParser by lazy { QmsParser(patternProvider) }
    val reputationParser by lazy { ReputationParser(patternProvider) }
    val searchParser by lazy { SearchParser(patternProvider) }
    val topicsParser by lazy { TopicsParser(patternProvider) }
    val checkerParser by lazy { CheckerParser() }
    val attachmentsParser by lazy { AttachmentsParser(patternProvider) }

    val authApi by lazy { AuthApi(webClient, authParser) }
    val devDbApi by lazy { DevDbApi(webClient, devDbParser) }
    val themeApi by lazy { ThemeApi(webClient, themeParser) }
    val editPostApi by lazy { EditPostApi(webClient, themeApi, editPostParser, attachmentsParser, themeParser) }
    val eventsApi by lazy { NotificationEventsApi(webClient) }
    val favoritesApi by lazy { FavoritesApi(webClient, favoritesParser) }
    val forumApi by lazy { ForumApi(webClient, forumParser) }
    val mentionsApi by lazy { MentionsApi(webClient, mentionsParser) }
    val newsApi by lazy { NewsApi(webClient, articleParser) }
    val profileApi by lazy { ProfileApi(webClient, profileParser) }
    val qmsApi by lazy { QmsApi(webClient, qmsParser) }
    val reputationApi by lazy { ReputationApi(webClient, reputationParser) }
    val searchApi by lazy { SearchApi(webClient, searchParser) }
    val topicsApi by lazy { TopicsApi(webClient, topicsParser) }
    val checkerApi by lazy { CheckerApi(webClient, checkerParser) }
    val attachmentsApi by lazy { AttachmentsApi(webClient, attachmentsParser) }

    val userSource by lazy { UserSourceProvider(qmsApi) }
    val forumUsersCache by lazy { ForumUsersCache(userSource) }
    val favoritesCache by lazy { FavoritesCache() }
    val forumCache by lazy { ForumCache() }
    val historyCache by lazy { HistoryCache() }
    val qmsCache by lazy { QmsCache() }
    val notesCache by lazy { NotesCache() }

    val avatarRepository by lazy { AvatarRepository(forumUsersCache, schedulers) }
    val favoritesRepository by lazy { FavoritesRepository(schedulers, favoritesApi, favoritesCache, authHolder, countersHolder, listsPreferencesHolder, notificationPreferencesHolder) }
    val historyRepository by lazy { HistoryRepository(schedulers, historyCache) }
    val mentionsRepository by lazy { MentionsRepository(schedulers, mentionsApi) }
    val authRepository by lazy { AuthRepository(schedulers, authApi, authHolder, countersHolder, userHolder) }
    val profileRepository by lazy { ProfileRepository(schedulers, profileApi, userHolder, authHolder, forumUsersCache) }
    val reputationRepository by lazy { ReputationRepository(schedulers, reputationApi) }
    val forumRepository by lazy { ForumRepository(schedulers, forumApi, forumCache) }
    val topicsRepository by lazy { TopicsRepository(schedulers, topicsApi) }
    val themeRepository by lazy { ThemeRepository(schedulers, themeApi, historyCache, forumUsersCache) }
    val qmsRepository by lazy { QmsRepository(schedulers, qmsApi, attachmentsApi, qmsCache, forumUsersCache, countersHolder) }
    val searchRepository by lazy { SearchRepository(schedulers, searchApi, forumUsersCache) }
    val newsRepository by lazy { NewsRepository(schedulers, newsApi, forumUsersCache) }
    val devDbRepository by lazy { DevDbRepository(schedulers, devDbApi) }
    val editPostRepository by lazy { PostEditorRepository(schedulers, editPostApi, attachmentsApi, forumUsersCache) }
    val notesRepository by lazy { NotesRepository(schedulers, notesCache, externalStorage) }
    val eventsRepository by lazy { EventsRepository(context, webClient, eventsApi, schedulers, networkState, authHolder, notificationPreferencesHolder) }
    val menuRepository by lazy { MenuRepository(preferences, authHolder, countersHolder) }
    val checkerRepository by lazy { CheckerRepository(schedulers, checkerApi, patternProvider) }

    val otherPreferencesHolder by lazy { OtherPreferencesHolder(preferences) }
    val mainPreferencesHolder by lazy { MainPreferencesHolder(preferences) }
    val topicPreferencesHolder by lazy { TopicPreferencesHolder(preferences) }
    val listsPreferencesHolder by lazy { ListsPreferencesHolder(preferences) }
    val notificationPreferencesHolder by lazy { NotificationPreferencesHolder(preferences) }

    val crossScreenInteractor by lazy { CrossScreenInteractor() }
    val qmsInteractor by lazy { QmsInteractor(qmsRepository, eventsRepository) }

}
