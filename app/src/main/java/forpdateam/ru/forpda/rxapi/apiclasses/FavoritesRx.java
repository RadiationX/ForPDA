package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class FavoritesRx {

    public Observable<FavData> getFavorites(int st, boolean all, Sorting sorting) {
        return Observable.fromCallable(() -> Api.Favorites().getFavorites(st, all, sorting));
    }

    public Observable<Boolean> editFavorites(int act, int favId, int id, String type) {
        switch (act) {
            case Favorites.ACTION_EDIT_SUB_TYPE:
                return Observable.fromCallable(() -> Api.Favorites().editSubscribeType(type, favId));
            case Favorites.ACTION_EDIT_PIN_STATE:
                return Observable.fromCallable(() -> Api.Favorites().editPinState(type, favId));
            case Favorites.ACTION_DELETE:
                return Observable.fromCallable(() -> Api.Favorites().delete(favId));
            case Favorites.ACTION_ADD:
                return Observable.fromCallable(() -> Api.Favorites().add(id, type));
            default:
                return Observable.just(false);
        }
    }
}
