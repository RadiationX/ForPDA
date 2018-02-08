package forpdateam.ru.forpda.presentation.reputation;

import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 03.01.18.
 */

public interface ReputationView extends IBaseView {

    void showReputation(RepData repData);

    void onChangeReputation(String result);

    void showItemDialogMenu(RepItem item);
}
