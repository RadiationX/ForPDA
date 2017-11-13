package forpdateam.ru.forpda.ui.fragments.mentions;

import android.os.Bundle;

import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.data.realm.history.HistoryItemBd;
import io.reactivex.functions.Consumer;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 11.11.17.
 */

public class MentionsPresenter extends BasePresenter<MentionsContract.View> implements MentionsContract.Presenter {

    MentionsPresenter(MentionsContract.View view) {
        super(view);
    }


    @Override
    public void getMentions(int st) {
        view.setRefreshing(true);
        subscribe(RxApi.Mentions().getMentions(st), data -> {
            view.setRefreshing(false);
            view.showMentions(data);
        }, new MentionsData(), v -> getMentions(st));
    }
}
