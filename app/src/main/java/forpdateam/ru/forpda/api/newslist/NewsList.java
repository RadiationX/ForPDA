package forpdateam.ru.forpda.api.newslist;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsList {
    /* Groups:
    * 1. Link
    * 2. Title
    * 3. Image Url
    * 4. Comments Count
    * 5. Date
    * 6. Author
    * 7. Description */
    private static final Pattern pattern = Pattern.compile("<article[^>]*?class=\"post\"[^>]*?data-ztm=\"[^ ]+\"[^>]*>[\\s\\S]*?<a[^>]*?href=\"([^\"]*)\"[^>]*?title=\"([^\"]*?)\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\"[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?itemprop=\"description\">([\\s\\S]*?)<\\/div>[\\s\\S]*?<\\/article>");

    public ArrayList<NewsItem> get(String url) throws Exception {
        ArrayList<NewsItem> list = new ArrayList<>();
        String response = Client.getInstance().get(url);

        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            NewsItem item = new NewsItem();
            item.setLink(matcher.group(1));
            item.setTitle(matcher.group(2));
            item.setImageUrl(matcher.group(3));
            item.setCommentsCount(matcher.group(4));
            item.setDate(matcher.group(5));
            Log.e("News", "Test date: " + matcher.group(5));
            item.setAuthor(matcher.group(6));
            item.setDescription(matcher.group(7));
            list.add(item);
        }
        return list;
    }

    public Observable<ArrayList<NewsItem>> getNews(final String url) {
        return Observable.fromCallable(() -> get(url));
    }
}
