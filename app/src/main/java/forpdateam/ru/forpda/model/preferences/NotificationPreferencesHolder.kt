package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import ru.radiationx.flowpreferences.FlowPreferences
import ru.radiationx.flowpreferences.mapping
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorParser
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class NotificationPreferencesHolder @Inject constructor(
    private val preferences: FlowPreferences,
    private val inspectorParser: InspectorParser
) {


    val mainEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.ENABLED, true)
    }

    val mainSoundEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.SOUND_ENABLED, true)
    }

    val mainVibrationEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.VIBRATION_ENABLED, true)
    }

    val mainIndicatorEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.INDICATOR_ENABLED, true)
    }

    val mainAvatarsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.AVATARS_ENABLED, true)
    }

    val mainPeriodDuration by lazy {
        preferences.getInt(Preferences.Notifications.Main.PERIOD_SEC, 10).mapping(
            transformGet = { it.seconds },
            transformSet = { it.inWholeSeconds.toInt() }
        )
    }

    val favEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Favorites.ENABLED, true)
    }

    val favOnlyImportant by lazy {
        preferences.getBoolean(Preferences.Notifications.Favorites.ONLY_IMPORTANT, false)
    }

    val qmsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Qms.ENABLED, true)
    }

    val topicMentionsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.TopicMentions.ENABLED, true)
    }

    val siteMentionsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.SiteMentions.ENABLED, true)
    }

    val forumsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Forums.ENABLED, true)
    }

    val updateEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Update.ENABLED, true)
    }

    val dataQmsEvents by lazy {
        preferences.getStringSet(Preferences.Notifications.Data.QMS_EVENTS, emptySet()).mapping(
            transformGet = { savedResponse ->
                val response = buildString {
                    savedResponse?.forEach(::append)
                }
                inspectorParser.parseQmsEvents(response)
            },
            transformSet = { qmsItems ->
                qmsItems.map { it.rawContent }.toSet()
            }
        )
    }

    val dataFavoritesEvents by lazy {
        preferences.getStringSet(Preferences.Notifications.Data.FAVORITES_EVENTS, emptySet()).mapping(
            transformGet = { savedResponse ->
                val response = buildString {
                    savedResponse?.forEach(::append)
                }
                inspectorParser.parseFavoritesEvents(response)
            },
            transformSet = { favoriteItems ->
                favoriteItems.map { it.rawContent }.toSet()
            }
        )
    }

}