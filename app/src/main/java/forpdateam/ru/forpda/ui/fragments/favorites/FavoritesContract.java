package forpdateam.ru.forpda.ui.fragments.favorites;


import java.util.List;

import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.common.mvp.IBasePresenter;
import forpdateam.ru.forpda.common.mvp.IBaseView;
import forpdateam.ru.forpda.data.models.TabNotification;

/**
 * Created by radiationx on 11.11.17.
 */

public interface FavoritesContract {
    interface View extends IBaseView {
        void onLoadFavorites(FavData data);

        void onShowFavorite(List<FavItem> items);

        void onHandleEvent(int count);
    }

    interface Presenter extends IBasePresenter<View> {
        void getFavorites(int st, boolean all, Sorting sorting);

        void saveFavorites(List<FavItem> items);

        void showFavorites();

        void handleEvent(TabNotification event, Sorting sorting, int count);

        void markRead(int topicId);
    }
}
