package forpdateam.ru.forpda.apirx.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class SearchRx {
    public Observable<SearchResult> getSearch(SearchSettings settings) {
        return Observable.fromCallable(() -> Api.Search().getSearch(settings));
    }
}
