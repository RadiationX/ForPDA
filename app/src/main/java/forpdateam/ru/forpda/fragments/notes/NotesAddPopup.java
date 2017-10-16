package forpdateam.ru.forpda.fragments.notes;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.data.models.notes.NoteItem;

/**
 * Created by radiationx on 06.09.17.
 */

public class NotesAddPopup {
    private BottomSheetDialog dialog;
    private NoteActionListener noteActionListener;
    private TextView title;
    private ImageButton addButton;
    private EditText titleField, linkField, contentField;

    public NotesAddPopup(Context context, NoteItem item, NoteActionListener noteActionListener) {
        this.noteActionListener = noteActionListener;
        dialog = new BottomSheetDialog(context);
        View view = View.inflate(context, R.layout.notes_popup, null);
        title = (TextView) view.findViewById(R.id.popup_title);
        addButton = (ImageButton) view.findViewById(R.id.add_button);
        titleField = (EditText) view.findViewById(R.id.title_field);
        linkField = (EditText) view.findViewById(R.id.link_field);
        contentField = (EditText) view.findViewById(R.id.content_field);

        if (item != null) {
            title.setText(R.string.note_edit);
            titleField.setText(item.getTitle());
            linkField.setText(item.getLink());
            contentField.setText(item.getContent());
            addButton.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_toolbar_done));

        } else {
            title.setText(R.string.note_create);
        }

        addButton.setOnClickListener(v -> {
            if (NotesAddPopup.this.noteActionListener != null) {
                String title = titleField.getText().toString().trim();
                String link = linkField.getText().toString().trim();
                String content = contentField.getText().toString().trim();

                if (title.length() == 0) {
                    Toast.makeText(context, R.string.note_enter_title, Toast.LENGTH_SHORT).show();
                    return;
                }

                NoteItem result = item;
                if (result == null) {
                    result = new NoteItem();
                    result.setId(System.currentTimeMillis());
                }
                result.setTitle(title);
                result.setLink(link);
                result.setContent(content);
                NotesAddPopup.this.noteActionListener.onAddNote(result);
            }
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    public NotesAddPopup setTitle(String title) {
        titleField.setText(title);
        return this;
    }

    public NotesAddPopup setLink(String link) {
        linkField.setText(link);
        return this;
    }

    public NotesAddPopup setContent(String content) {
        contentField.setText(content);
        return this;
    }

    public NotesAddPopup setNoteActionListener(NoteActionListener noteActionListener) {
        this.noteActionListener = noteActionListener;
        return this;
    }

    public interface NoteActionListener {
        void onAddNote(NoteItem item);
    }

    public static void showAddNoteDialog(Context context, String title, String link) {
        new NotesAddPopup(context, null, null)
                .setTitle(title)
                .setLink(link)
                .setNoteActionListener(NotesFragment::addNote);
    }

    public static void showAddNoteDialog(Context context, String title, String link, String content) {
        new NotesAddPopup(context, null, null)
                .setTitle(title)
                .setLink(link)
                .setContent(content)
                .setNoteActionListener(NotesFragment::addNote);
    }
}
