package forpdateam.ru.forpda.presentation.favorites;

import java.util.List;

import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 01.01.18.
 */

public interface FavoritesView extends IBaseView {
    void onLoadFavorites(FavData data);

    void onShowFavorite(List<FavItem> items);

    void onHandleEvent(int count);
}
