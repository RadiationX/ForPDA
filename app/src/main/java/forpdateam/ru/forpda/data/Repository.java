package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.data.local.LocalRepository;
import forpdateam.ru.forpda.fragments.news.models.NewsCallbackModel;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import forpdateam.ru.forpda.realm.RealmMapping;
import forpdateam.ru.forpda.utils.rx.RxSchedulers;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 13.01.17.
 */

public class Repository implements IRepository {

    private static final String TAG = "Repository";
    private static Repository INSTANCE;

    private LocalRepository localRepository;
    private RxSchedulers rxSchedulers = new RxSchedulers();

    public static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Repository();
        }
    }

    public static Repository getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("No repository instance available");
        }
        return INSTANCE;
    }

    public static void removeInstance() {
        LocalRepository.removeInstance();
        INSTANCE = null;
    }

    private Repository() {
        localRepository = LocalRepository.getInstance(Realm.getDefaultInstance());
    }

    // ===========================================NEWS=========================================== //

    /**
     *
     * @param category - категория новостей
     * @return - объект со списком новостей либо из базы, либо из сети и сохраняет в базу.
     * Плюс первый boolean - откуда пришли данные. True - сеть, false - база.
     * Второй не используется
     *
     * ps. Надо шоб показать прогресс и для запуска обновления.
     */
    @Override
    public Single<NewsCallbackModel> getNewsList(@NonNull String category) {
        List<NewsModel> list = localRepository.getLocalNewsList2(category);
        log(TAG + " getNewsList -> fromDb " +  list.size());
        if (list.size() > 0) {
            log(TAG+ " getNewsList ->  return from db");
            return Single.fromCallable(() -> new NewsCallbackModel(list, false, false));
        }
        log(TAG + " getNewsList -> dot two");
        return getNewsAndSaveFromNetwork(category)
                .flatMap(list12 -> Single.fromCallable(() -> new NewsCallbackModel(list12, true, false)));
    }

    /**
     *
     * @param category - категория новостей
     * @return - возращает список новых новостей в категории и обновляет базу.
     */
    @Override
    public Single<NewsCallbackModel> updateNewsListData(@NonNull String category) {
        return  Api.NewsList().getNewsListFromNetwork1(category, 0)
                .map(RealmMapping::getMappingNewsList)
                .compose(rxSchedulers.getIOToMainTransformerSingle())
                .flatMap(list -> {
                    List<NewsModel> cache = checkList(list, category);
//                    localRepository.deleteNewsFromRealm(category);
                    localRepository.saveNewsToRealm2(list);
                    return Single.fromCallable(() -> new NewsCallbackModel(cache));
                });
    }

    private List<NewsModel> checkList(List<NewsModel> list, String category) {
        List<NewsModel> cache = new ArrayList<>();
        List<NewsModel> cache2 = localRepository.getLocalNewsList2(category);
        Stream.of(list).filterNot(newModel -> Stream.of(cache2)
                        .anyMatch(oldModel -> newModel.getLink().equals(oldModel.getLink())))
                .forEach(cache::add);
        return cache;
    }

    /**
     * НУЖНО ДЛЯ ХИТРОВЫЕБНОЙ ПОГИНАЦИИ
     * @param category - категория новостей
     * @param pageNumber - номер страницы новостей
     * @param lastUrl - трудно объяснить, см ниже по коду.
     * @return - возращает объект со списком новостей и boolean полем.
     * Если true - значит сслыка не найдена в базе и есть чего еще грузить.
     * Если false - значит ссылка в базе найдена и нахер с пляжа.
     *
     * ps. Так же присутствует обновление базы до актуального списка.
     * pss. Если не понятно что происходит ниже, то не переживай - я тоже с трудом понимаю.)
     */
    @Override
    public Single<NewsCallbackModel> getLoadMoreNewsListData(@NonNull String category, int pageNumber, String lastUrl) {
        return Api.NewsList().getNewsListFromNetwork1(category, pageNumber)
                .map(RealmMapping::getMappingNewsList)
                .compose(rxSchedulers.getIOToMainTransformerSingle())
                .flatMap(new Function<List<NewsModel>, SingleSource<? extends NewsCallbackModel>>() {
                    @Override
                    public SingleSource<? extends NewsCallbackModel> apply(List<NewsModel> list) throws Exception {

                        // Логика чуть позже будет

                        return Single.fromCallable(() -> new NewsCallbackModel(list, false, false));
                    }
                });
    }


    /**
     * ЭТО ПРОСТО ПОГИНАЦИЯ
     * @param category - категория новостей
     * @param pageNumber - номер страницы новостей
     * @return - возращает список новостей cо страницы 2+.
     */
    @Override
    public Single<List<NewsModel>> loadMoreNewsItems(@NonNull String category, int pageNumber) {
        return  Api.NewsList()
                .getNewsListFromNetwork1(category, pageNumber)
                .map(RealmMapping::getMappingNewsList)
                .compose(rxSchedulers.getIOToMainTransformerSingle());
    }

    /**
     *
     * @param category - категория новостей
     * @return - список новостей. Работает когда база пуста.
     */
    private Single<List<NewsModel>> getNewsAndSaveFromNetwork(String category) {
        return  Api.NewsList().getNewsListFromNetwork1(category, 0)
                .map(RealmMapping::getMappingNewsList)
                .compose(rxSchedulers.getIOToMainTransformerSingle())
                .flatMap(list -> {
                    log(TAG + " getNewsAndSaveFromNetwork ->> " + list.size());
                    localRepository.saveNewsToRealm2(list);
                    return Single.fromCallable(() -> list);
                });
    }

//    private Single<ArrayList<NewsNetworkModel>> getFromNetwork(String category, int pageNumber) {
//        return Single.fromCallable(() -> NewsParser.getNewsListFromNetwork1(category, pageNumber))
//                .compose(rxSchedulers.getIOToMainTransformerSingle());
//    }

    // ========================================NEWS END========================================== //

    // ==========================================DEVDB=========================================== //
    // ========================================DEVDB END========================================= //
}
