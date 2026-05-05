package forpdateam.ru.forpda.ui.fragments.notes

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by radiationx on 06.09.17.
 */
class NotesAddPopup(context: Context, item: NoteItem?) {
    private val dialog = BottomSheetDialog(context)
    private val title: TextView
    private val addButton: ImageButton
    private val titleField: EditText
    private val linkField: EditText
    private val contentField: EditText
    private var editingMode = false
    private val notesRepository = get().Di().notesRepository
    private val compositeDisposable = CompositeDisposable()

    init {
        dialog.setOnShowListener { dialog1: DialogInterface? ->
            dialog.window!!
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        dialog.setOnDismissListener { dialog: DialogInterface? -> compositeDisposable.dispose() }
        val view = View.inflate(context, R.layout.notes_popup, null)
        title = view.findViewById(R.id.popup_title)
        addButton = view.findViewById(R.id.add_button)
        titleField = view.findViewById(R.id.title_field)
        linkField = view.findViewById(R.id.link_field)
        contentField = view.findViewById(R.id.content_field)
        editingMode = item != null

        if (editingMode) {
            title.setText(R.string.note_edit)
            titleField.setText(item!!.title)
            linkField.setText(item.link)
            contentField.setText(item.content)
            addButton.setImageDrawable(getVecDrawable(context, R.drawable.ic_toolbar_done))
        } else {
            title.setText(R.string.note_create)
        }

        addButton.setOnClickListener { v: View? ->
            val title = titleField.text.toString().trim { it <= ' ' }
            val link = linkField.text.toString().trim { it <= ' ' }
            val content = contentField.text.toString().trim { it <= ' ' }

            if (title.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.note_enter_title,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val result = NoteItem(
                id = item?.id ?: System.currentTimeMillis(),
                title = title,
                link = link,
                content = content
            )
            val disposable = notesRepository
                .addNote(result)
                .subscribe { dialog.dismiss() }
            compositeDisposable.add(disposable)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun setTitle(title: String?): NotesAddPopup {
        titleField.setText(title)
        return this
    }

    fun setLink(link: String?): NotesAddPopup {
        linkField.setText(link)
        return this
    }

    fun setContent(content: String?): NotesAddPopup {
        contentField.setText(content)
        return this
    }

    companion object {
        fun showAddNoteDialog(context: Context, title: String?, link: String?) {
            NotesAddPopup(context, null)
                .setTitle(title)
                .setLink(link)
        }

        fun showAddNoteDialog(context: Context, title: String?, link: String?, content: String?) {
            NotesAddPopup(context, null)
                .setTitle(title)
                .setLink(link)
                .setContent(content)
        }
    }
}
