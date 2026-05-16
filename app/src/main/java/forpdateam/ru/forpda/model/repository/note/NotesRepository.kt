package forpdateam.ru.forpda.model.repository.note

import android.content.Context
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.model.data.cache.notes.NotesCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesRepository(
    private val context: Context,
    private val notesCache: NotesCache,
    private val externalStorage: ExternalStorageProvider
) {

    fun observeItems(): Flow<List<NoteItem>> {
        return notesCache.observeItems()
    }

    suspend fun deleteNote(id: Long) {
        notesCache.delete(id)
    }

    suspend fun addNote(item: NoteItem) {
        notesCache.add(item)
    }

    suspend fun addNotes(items: List<NoteItem>) {
        notesCache.add(items)
    }

    suspend fun importNotes(file: RequestFile): List<NoteItem> {
        val metaData = file.getMetaData(context)
        val jsonSource = if (metaData.name.matches("[\\s\\S]*?\\.json$".toRegex())) {
            externalStorage.getText(file.openInputStream(context))
        } else {
            throw Exception("Файл имеет неправильное расширение")
        }
        return importNotes(jsonSource)
    }

    suspend fun importNotes(jsonSource: String): List<NoteItem> {
        val jsonBody = JSONArray(jsonSource)
        val noteItems = mutableListOf<NoteItem>()
        for (i in 0 until jsonBody.length()) {
            try {
                val jsonItem = jsonBody.getJSONObject(i)
                noteItems.add(
                    NoteItem(
                        id = jsonItem.getLong("id"),
                        title = jsonItem.getString("title"),
                        link = jsonItem.getString("link"),
                        content = jsonItem.getString("content"),
                    )
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        notesCache.add(noteItems)
        return noteItems
    }

    suspend fun exportNotes() {
        val jsonBody = JSONArray()
        notesCache.getItems().forEach {
            try {
                jsonBody.put(JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("link", it.link)
                    put("content", it.content)
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val date = SimpleDateFormat(
            "MMddyyy-HHmmss",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))
        val fileName = "ForPDA_Notes_$date.json"
        return externalStorage.saveTextDefault(jsonBody.toString(), fileName)
    }

}