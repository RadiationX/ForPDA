package forpdateam.ru.forpda.common.mvp;

import com.arellomobile.mvp.MvpView;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBaseView extends MvpView {
    void setRefreshing(boolean isRefreshing);
}
