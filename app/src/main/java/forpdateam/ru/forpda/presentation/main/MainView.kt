package forpdateam.ru.forpda.presentation.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView

interface MainView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showFirstStartAnimation()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun changeTheme(isDark: Boolean)
}