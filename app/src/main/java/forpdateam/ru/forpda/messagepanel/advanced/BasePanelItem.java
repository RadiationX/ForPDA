package forpdateam.ru.forpda.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.FrameLayout;

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
        recyclerView = (RecyclerView) findViewById(R.id.auto_fit_recycler_view);
    }

    public String getTitle() {
        return title;
    }


}
