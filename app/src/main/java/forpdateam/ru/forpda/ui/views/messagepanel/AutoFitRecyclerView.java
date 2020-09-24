package forpdateam.ru.forpda.ui.views.messagepanel;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 08.01.17.
 */

public class AutoFitRecyclerView extends RecyclerView {
    private GridLayoutManager manager;
    private int columnWidth = App.px48; //default value
    private boolean isLinear = false;

    public AutoFitRecyclerView(Context context) {
        super(context);
        init();
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        manager = new GridLayoutManager(getContext(), 1);
        setLayoutManager(manager);
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
        invalidate();
    }

    public void setFakeLinear(boolean linear) {
        isLinear = linear;
        invalidate();
    }

    public GridLayoutManager getManager() {
        return manager;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (isLinear || columnWidth <= 0) {
            manager.setSpanCount(1);
        } else {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }

    }
}
