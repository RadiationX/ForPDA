package forpdateam.ru.forpda.presentation.main

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView

interface MainView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showFirstStartAnimation()
}