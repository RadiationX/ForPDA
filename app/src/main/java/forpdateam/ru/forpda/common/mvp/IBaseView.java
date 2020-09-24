package forpdateam.ru.forpda.common.mvp;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBaseView extends MvpView {
    @StateStrategyType(AddToEndSingleStrategy.class)
    void setRefreshing(boolean isRefreshing);
}
