package forpdateam.ru.forpda.api.profile;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.api.profile.interfaces.IProfileApi;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by radiationx on 03.08.16.
 */
public class Profile implements IProfileApi {
    public Observable<ProfileModel> getRx(final String url) {
        return Observable.create(new Observable.OnSubscribe<ProfileModel>() {
            @Override
            public void call(Subscriber<? super ProfileModel> subscriber) {
                try {
                    subscriber.onNext(ProfileParser.get(url));
                    subscriber.onCompleted();
                }catch (Exception e){
                    subscriber.onError(e);
                }
            }
        });
    }
}
