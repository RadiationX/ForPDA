package forpdateam.ru.forpda.model.repository.inspector

import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorApi
import forpdateam.ru.forpda.model.data.remote.api.inspector.InspectorParser
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder

class InspectorRepository(
    private val inspectorApi: InspectorApi,
    private val preferences: NotificationPreferencesHolder,
    private val inspectorParser: InspectorParser
) {

    suspend fun getFavoritesDiff(): InspectorDiff<InspectorItem.Favorite> {
        val loadedItems = inspectorApi.getFavorites()
        val savedItems = getSavedFavorites()
        return InspectorDiff(loadedItems, savedItems)
    }

    fun saveFavorites(diff: InspectorDiff<InspectorItem.Favorite>) {
        val response = diff.loadedItems.map { it.rawContent }.toSet()
        preferences.setDataFavoritesEvents(response)
    }

    suspend fun getQmsDiff(): InspectorDiff<InspectorItem.Qms> {
        val loadedItems = inspectorApi.getQms()
        val savedItems = getSavedQms()
        return InspectorDiff(loadedItems, savedItems)
    }

    fun saveQms(diff: InspectorDiff<InspectorItem.Qms>) {
        val response = diff.loadedItems.map { it.rawContent }.toSet()
        preferences.setDataQmsEvents(response)
    }

    private fun getSavedQms(): List<InspectorItem.Qms> {
        val savedEvents = preferences.getDataFavoritesEvents() ?: return emptyList()
        val response = buildString {
            savedEvents.forEach(::append)
        }
        return inspectorParser.parseQmsEvents(response)
    }

    private fun getSavedFavorites(): List<InspectorItem.Favorite> {
        val savedEvents = preferences.getDataFavoritesEvents() ?: return emptyList()
        val response = buildString {
            savedEvents.forEach(::append)
        }
        return inspectorParser.parseFavoritesEvents(response)
    }
}