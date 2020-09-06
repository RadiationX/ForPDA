package forpdateam.ru.forpda.entity.remote.favorites

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting

/**
 * Created by radiationx on 22.09.16.
 */

class FavData {
    val items = mutableListOf<FavItem>()
    var pagination = Pagination()
    var sorting = Sorting()
}
