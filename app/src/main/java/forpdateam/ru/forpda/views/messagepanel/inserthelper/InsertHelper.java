package forpdateam.ru.forpda.views.messagepanel.inserthelper;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 27.05.17.
 */

public class InsertHelper {
    private ArrayList<Pair<String, String>> headers = new ArrayList<>();
    private ArrayList<EditText> headersLayout = new ArrayList<>();
    private EditText bodyLayout;
    private Pair<String, String> body;
    private String title;
    private Context context;
    private LayoutInflater inflater;
    private ScrollView layoutContainer;
    private LinearLayout itemsContainer;
    private InsertListener insertListener;

    public InsertHelper(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutContainer = (ScrollView) inflater.inflate(R.layout.insert_helper_body, null);
        itemsContainer = (LinearLayout) layoutContainer.findViewById(R.id.insert_helper_items_container);
    }

    public void addHeader(String title, String code) {
        headers.add(new Pair<>(title, code));
        TextInputLayout inputLayout = (TextInputLayout) inflater.inflate(R.layout.insert_helper_item, null);
        inputLayout.setHint(title);
        headersLayout.add(inputLayout.getEditText());
        itemsContainer.addView(inputLayout);
    }

    public void setBody(String title, String value) {
        if (false) {
            this.body = new Pair<>(title, value);
            TextInputLayout inputLayout = (TextInputLayout) inflater.inflate(R.layout.insert_helper_item, null);
            inputLayout.setHint(title);
            bodyLayout = inputLayout.getEditText();
            itemsContainer.addView(inputLayout);
        }

    }

    public void setInsertListener(InsertListener insertListener) {
        this.insertListener = insertListener;
    }

    public void show() {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(layoutContainer)
                .setPositiveButton("Вставить", (dialog, which) -> {
                    if (insertListener != null) {
                        ArrayList<Pair<String, String>> resultHeaders = new ArrayList<>();
                        for (int i = 0; i < headers.size(); i++) {
                            String value = null;
                            Editable editable = headersLayout.get(i).getText();
                            if (editable != null) {
                                value = editable.toString();
                                if (value.length() == 0) {
                                    value = null;
                                }
                            }
                            resultHeaders.add(new Pair<>(headers.get(i).second, value));
                        }
                        if (bodyLayout != null) {
                            insertListener.onInsert(resultHeaders, bodyLayout.getText().toString());
                        } else {
                            insertListener.onInsert(resultHeaders, null);
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public interface InsertListener {
        void onInsert(ArrayList<Pair<String, String>> resultHeaders, String bodyResult);
    }
}
