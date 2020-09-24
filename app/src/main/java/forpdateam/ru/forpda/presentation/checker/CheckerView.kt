package forpdateam.ru.forpda.presentation.checker

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 28.01.18.
 */
interface CheckerView : IBaseView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showUpdateData(update: UpdateData)
}