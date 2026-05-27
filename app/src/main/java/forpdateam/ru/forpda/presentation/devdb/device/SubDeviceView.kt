package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.entity.remote.devdb.Device
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface SubDeviceView : MvpView {

    fun bindSpecs(items: List<Device.Specs>)

    fun bindArticles(items: List<Device.Article>)

    fun bindTopics(items: List<Device.Topic>)

    fun bindComments(items: List<Device.Comment>)
}
