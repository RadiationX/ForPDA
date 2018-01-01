package forpdateam.ru.forpda.presentation.history;

import java.util.List;

import forpdateam.ru.forpda.common.mvp.IBaseView;
import forpdateam.ru.forpda.entity.app.history.HistoryItem;

/**
 * Created by radiationx on 01.01.18.
 */

public interface HistoryView extends IBaseView {
    void showHistory(List<HistoryItem> items);
}
