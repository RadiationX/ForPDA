package forpdateam.ru.forpda.presentation.favorites

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface FavoritesView : IBaseView {
    fun initSorting(sorting: Sorting)
    fun onLoadFavorites(data: FavoritesData)
    fun onShowFavorite(items: List<Favorite>)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: Favorite)

    @StateStrategyType(SkipStrategy::class)
    fun showSubscribeDialog(item: Favorite)

    @StateStrategyType(SkipStrategy::class)
    fun onChangeFav(result: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun onMarkAllRead()

    fun setShowDot(enabled: Boolean)
    fun setUnreadTop(unreadTop: Boolean)
}
