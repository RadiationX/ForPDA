package forpdateam.ru.forpda.presentation.favorites

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FavoritesView : IBaseView {
    fun initSorting(sorting: Sorting)
    fun onLoadFavorites(data: FavData)
    fun onShowFavorite(items: List<FavItem>)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: FavItem)

    @StateStrategyType(SkipStrategy::class)
    fun showSubscribeDialog(item: FavItem)

    @StateStrategyType(SkipStrategy::class)
    fun onChangeFav(result: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun onMarkAllRead()

    fun setShowDot(enabled: Boolean)
    fun setUnreadTop(unreadTop: Boolean)
}
