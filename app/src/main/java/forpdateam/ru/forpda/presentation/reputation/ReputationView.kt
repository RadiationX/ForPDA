package forpdateam.ru.forpda.presentation.reputation

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 03.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ReputationView : IBaseView {
    fun showReputation(repData: RepData)

    @StateStrategyType(SkipStrategy::class)
    fun onChangeReputation()

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: RepItem)

    fun showAvatar(avatarUrl: String)
}
