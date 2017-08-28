package forpdateam.ru.forpda.rxapi.apiclasses;

import java.util.List;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import io.reactivex.Observable;

/**
 * Created by radiationx on 26.08.17.
 */

public class NewsRx {
    public Observable<List<NewsItem>> getNews(final String category, int pageNumber) {
        return Observable.fromCallable(() -> Api.NewsApi().getNews(category, pageNumber));
    }

    public Observable<DetailsPage> getDetails(final int id) {
        return Observable.fromCallable(() -> transform(Api.NewsApi().getDetails(id)));
    }

    public static DetailsPage transform(DetailsPage detailsPage) throws Exception {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_NEWS);
        t.setVariableOpt("style_type", App.getInstance().getCssStyleType());
        t.setVariableOpt("details_title", Utils.htmlEncode(detailsPage.getTitle()));
        t.setVariableOpt("body_type", "news");
        t.setVariableOpt("details_content", detailsPage.getHtml());
        detailsPage.setHtml(t.generateOutput());
        t.reset();

        return detailsPage;
    }
}
