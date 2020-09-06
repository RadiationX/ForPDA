package forpdateam.ru.forpda.presentation.devdb.search

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.devdb.Brand

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface SearchDevicesView : IBaseView {
    fun showData(data: Brand, query: String)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(title: String, url: String)
}
