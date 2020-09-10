package forpdateam.ru.forpda.presentation.announce

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.Announce

/**
 * Created by radiationx on 02.01.18.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AnnounceView : IBaseView {
    fun showData(data: Announce)
    fun setStyleType(type: String)
}
