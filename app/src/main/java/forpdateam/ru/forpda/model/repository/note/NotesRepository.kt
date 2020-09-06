package forpdateam.ru.forpda.model.repository.note

import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.notes.NotesCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class NotesRepository(
        private val schedulers: SchedulersProvider,
        private val notesCache: NotesCache,
        private val externalStorage: ExternalStorageProvider
) : BaseRepository(schedulers) {

    fun observeItems(): Observable<List<NoteItem>> = notesCache
            .observeItems()
            .runInIoToUi()

    fun loadNotes(): Single<List<NoteItem>> = Single
            .fromCallable { notesCache.getItems() }
            .runInIoToUi()

    fun deleteNote(id: Long): Completable = Completable
            .fromCallable { notesCache.delete(id) }
            .runInIoToUi()

    fun updateNote(item: NoteItem): Completable = Completable
            .fromCallable { notesCache.update(item) }
            .runInIoToUi()

    fun addNote(item: NoteItem): Completable = Completable
            .fromCallable { notesCache.add(item) }
            .runInIoToUi()

    fun addNotes(items: List<NoteItem>): Completable = Completable
            .fromCallable { notesCache.add(items) }
            .runInIoToUi()

    fun importNotes(file: RequestFile) = Single
            .fromCallable {
                if (file.fileName.matches("[\\s\\S]*?\\.json$".toRegex())) {
                    externalStorage.getText(file.fileStream)
                } else {
                    throw Exception("Файл имеет неправильное расширение")
                }
            }
            .flatMap { importNotes(it) }
            .runInIoToUi()


    fun importNotes(jsonSource: String) = Single
            .fromCallable {
                val jsonBody = JSONArray(jsonSource)
                val noteItems = mutableListOf<NoteItem>()
                for (i in 0 until jsonBody.length()) {
                    try {
                        val jsonItem = jsonBody.getJSONObject(i)
                        noteItems.add(NoteItem().apply {
                            id = jsonItem.getLong("id")
                            title = jsonItem.getString("title")
                            link = jsonItem.getString("link")
                            content = jsonItem.getString("content")
                        })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                return@fromCallable noteItems
            }
            .doOnSuccess { notesCache.add(it) }
            .runInIoToUi()

    fun exportNotes() = Single
            .fromCallable {
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
                val date = SimpleDateFormat("MMddyyy-HHmmss", Locale.getDefault()).format(Date(System.currentTimeMillis()))
                val fileName = "ForPDA_Notes_$date.json"
                externalStorage.saveTextDefault(jsonBody.toString(), fileName)

            }
            .runInIoToUi()

}