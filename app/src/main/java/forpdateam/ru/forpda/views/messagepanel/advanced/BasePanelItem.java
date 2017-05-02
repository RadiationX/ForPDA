package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import forpdateam.ru.forpda.views.messagepanel.AutoFitRecyclerView;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;

/**
 * Created by radiationx on 08.01.17.
 */

@SuppressLint("ViewConstructor")
public class BasePanelItem extends FrameLayout {
    private String title;
    protected MessagePanel messagePanel;
    protected RecyclerView recyclerView;

    public BasePanelItem(Context context, MessagePanel messagePanel, String title) {
        super(context);
        this.messagePanel = messagePanel;
        this.title = title;
        recyclerView = new AutoFitRecyclerView(context);
        addView(recyclerView);
    }

    public String getTitle() {
        return title;
    }


}
