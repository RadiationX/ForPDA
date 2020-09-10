package forpdateam.ru.forpda.presentation.devdb.device

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.devdb.Device

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface DeviceView : IBaseView {
    fun showData(data: Device)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(title: String, url: String)
}
