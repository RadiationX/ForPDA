package forpdateam.ru.forpda.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class BasePanelItem extends FrameLayout {
    private String title;
    protected EditText messageField;
    protected RecyclerView recyclerView;

    public BasePanelItem(Context context, EditText editText, String title) {
        super(context);
        messageField = editText;
        this.title = title;
        inflate(getContext(), R.layout.testbasepanel, this);
        recyclerView = (RecyclerView) findViewById(R.id.afrv);
    }

    public String getTitle() {
        return title;
    }


}
