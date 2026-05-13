package forpdateam.ru.forpda.entity.remote.favorites

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting

/**
 * Created by radiationx on 22.09.16.
 */

data class FavoritesData(
    val items: List<Favorite>,
    val pagination: Pagination,
    val sorting: Sorting
)
