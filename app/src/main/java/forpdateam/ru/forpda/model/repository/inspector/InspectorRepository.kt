package forpdateam.ru.forpda.model.repository.inspector

import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.inspector.InspectorMention
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorApi
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder

class InspectorRepository(
    private val inspectorApi: InspectorApi,
    private val preferences: NotificationPreferencesHolder
) {

    suspend fun getFavoritesDiff(): InspectorDiff<InspectorItem.Favorite> {
        val loadedItems = inspectorApi.getFavorites()
        val savedItems = preferences.dataFavoritesEvents.get()
        return InspectorDiff(loadedItems, savedItems)
    }

    fun saveFavorites(diff: InspectorDiff<InspectorItem.Favorite>) {
        preferences.dataFavoritesEvents.set(diff.loadedItems)
    }

    suspend fun getQmsDiff(): InspectorDiff<InspectorItem.Qms> {
        val loadedItems = inspectorApi.getQms()
        val savedItems = preferences.dataQmsEvents.get()
        return InspectorDiff(loadedItems, savedItems)
    }

    fun saveQms(diff: InspectorDiff<InspectorItem.Qms>) {
        preferences.dataQmsEvents.set(diff.loadedItems)
    }

    suspend fun getMentionsCount(): InspectorMention {
        return inspectorApi.getMentionsCount()
    }
}