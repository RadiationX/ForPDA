package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class QmsEventsHandler @Inject constructor(
    private val qmsCache: QmsCache,
) {

    suspend fun handle(event: WebSocketEvent) {
        if (event !is WebSocketEvent.Qms) {
            return
        }
        updateCounter(event.themeId) { count ->
            when (event.type) {
                is WebSocketEvent.Qms.Type.New -> count + 1
                is WebSocketEvent.Qms.Type.Read -> count
                is WebSocketEvent.Qms.Type.ReadAll -> 0
                WebSocketEvent.Qms.Type.Typing -> count
                WebSocketEvent.Qms.Type.Uploading -> count
            }
        }
    }

    suspend fun handle(diff: InspectorDiff.Qms) {
        diff.loadedItems.forEach { item ->
            updateCounter(item.themeId) { item.msgCount }
        }
    }

    private suspend fun updateCounter(threadId: QmsThreadId, block: (Int) -> Int) {
        val target = findTarget(threadId) ?: return
        val newThemeCount = block(target.theme.countNew)
        if (newThemeCount == target.theme.countNew) {
            return
        }
        val updatedThemes = target.themes.themes.map {
            if (it.id == threadId) {
                it.copy(countNew = newThemeCount)
            } else {
                it
            }
        }
        val updatedTarget = target.copy(
            themes = target.themes.copy(
                themes = updatedThemes
            )
        )
        qmsCache.saveThemes(updatedTarget.themes)

        updateContact(target.themes.user.id)
    }

    private suspend fun findTarget(themeId: QmsThreadId): Target? {
        val themesList = qmsCache.getAllThemes()
        for (themes in themesList) {
            for (theme in themes.themes) {
                if (theme.id == themeId) {
                    return Target(themes, theme)
                }
            }
        }
        return null
    }

    private suspend fun updateContact(userId: UserId) {
        qmsCache.getContact(userId)?.also { contact ->
            val newContactCount = qmsCache.getThemes(userId).themes.sumOf { it.countNew }
            val newContact = contact.copy(count = newContactCount)
            qmsCache.updateContact(newContact)
        }
    }

    private data class Target(
        val themes: QmsThemes,
        val theme: QmsTheme
    )
}