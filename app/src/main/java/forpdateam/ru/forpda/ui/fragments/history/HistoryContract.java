package forpdateam.ru.forpda.ui.fragments.history;


import java.util.List;

import forpdateam.ru.forpda.common.mvp.IBasePresenter;
import forpdateam.ru.forpda.common.mvp.IBaseView;
import forpdateam.ru.forpda.data.realm.history.HistoryItemBd;

/**
 * Created by radiationx on 11.11.17.
 */

public interface HistoryContract {
    interface View extends IBaseView {
        void showHistory(List<HistoryItemBd> history);
    }

    interface Presenter extends IBasePresenter<View> {
        void getHistory();

        void remove(int id);

        void clear();
    }
}
