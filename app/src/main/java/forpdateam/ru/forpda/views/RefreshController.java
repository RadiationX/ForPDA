package forpdateam.ru.forpda.views;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

/**
 * Created by radiationx on 05.10.17.
 */
/*
* Для управления рефрешем
* */
public class RefreshController {
    private View additionalRefresh;
    //private View additionalContent;
    private View mainRefresh;
    private View mainContent;
    private boolean firstLoad = true;

    public RefreshController(View additionalRefresh, View mainContent) {
        this.additionalRefresh = additionalRefresh;
        this.mainContent = mainContent;
    }

    public RefreshController(View additionalRefresh, View mainRefresh, View mainContent) {
        this.additionalRefresh = additionalRefresh;
        this.mainRefresh = mainRefresh;
        this.mainContent = mainContent;
    }

    public void setMainRefresh(View mainRefresh) {
        this.mainRefresh = mainRefresh;
    }

    public void startLoad() {
        if (firstLoad) {
            mainContent.setVisibility(View.INVISIBLE);
            additionalRefresh.setVisibility(View.VISIBLE);
        } else if (mainRefresh != null) {
            if (mainRefresh instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) mainRefresh).setRefreshing(true);
            }
        }
    }

    public void endLoad() {
        if (firstLoad) {
            mainContent.setVisibility(View.VISIBLE);
            additionalRefresh.setVisibility(View.GONE);
            firstLoad = false;
        } else if (mainRefresh != null) {
            if (mainRefresh instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) mainRefresh).setRefreshing(false);
            }
        }
    }
}
