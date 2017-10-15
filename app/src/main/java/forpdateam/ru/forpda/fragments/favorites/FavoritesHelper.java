package forpdateam.ru.forpda.fragments.favorites;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.rxapi.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.03.17.
 */

public class FavoritesHelper {



    public static void add(@NonNull Consumer<Boolean> onNext, int id, String subType) {
        changeFav(onNext, Favorites.ACTION_ADD, -1, id, subType);
    }

    public static void delete(@NonNull Consumer<Boolean> onNext, int favId) {
        changeFav(onNext, Favorites.ACTION_DELETE, favId, -1, null);
    }

    /*public static void changePinState(@NonNull Consumer<Boolean> onNext, int favId, String pinState) {
        changeFav(onNext, Favorites.ACTION_EDIT_PIN_STATE, favId, -1, pinState);
    }

    public static void changeSubType(@NonNull Consumer<Boolean> onNext, int favId, String subType) {
        changeFav(onNext, Favorites.ACTION_EDIT_SUB_TYPE, favId, -1, subType);
    }*/

    public static void addWithDialog(Context context, @NonNull Consumer<Boolean> onNext, int id) {
        new AlertDialog.Builder(context)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    add(onNext, id, Favorites.SUB_TYPES[which1]);
                })
                .show();
    }

    public static void deleteWithDialog(Context context, @NonNull Consumer<Boolean> onNext, int favId) {
        new AlertDialog.Builder(context)
                .setItems(FavoritesFragment.SUB_NAMES, (dialog1, which1) -> {
                    delete(onNext, favId);
                })
                .show();
    }

    public static void changeFav(@NonNull Consumer<Boolean> onNext, int action, int favId, int id, String subType) {
        RxApi.Favorites().editFavorites(action, favId, id, subType).onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }
}
