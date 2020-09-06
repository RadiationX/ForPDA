package forpdateam.ru.forpda.presentation.topics

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData

/**
 * Created by radiationx on 03.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface TopicsView : IBaseView {
    fun showTopics(data: TopicsData)

    fun updateList()

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: TopicItem)

    @StateStrategyType(SkipStrategy::class)
    fun onMarkRead()

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)
}
