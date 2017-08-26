package forpdateam.ru.forpda.rxapi.apiclasses;

import java.util.List;
import java.util.regex.Matcher;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.api.theme.models.Poll;
import forpdateam.ru.forpda.api.theme.models.PollQuestion;
import forpdateam.ru.forpda.api.theme.models.PollQuestionItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;

/**
 * Created by radiationx on 26.08.17.
 */

public class NewsRx {
    public Observable<List<NewsItem>> getNews(final String category, int pageNumber) {
        return Observable.fromCallable(() -> Api.NewsApi().getNews(category, pageNumber));
    }

    public Observable<NewsItem> getDetails(final String url) {
        return Observable.fromCallable(() -> transform(Api.NewsApi().getDetails(url)));
    }

    public static NewsItem transform(NewsItem newsItem) throws Exception {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_NEWS);
        t.setVariableOpt("style_type", App.getInstance().getCssStyleType());
        t.setVariableOpt("details_title", Utils.htmlEncode(newsItem.getTitle()));
        t.setVariableOpt("body_type", "news");
        t.setVariableOpt("details_content", newsItem.getHtml());
        newsItem.setHtml(t.generateOutput());
        t.reset();

        return newsItem;
    }
}
