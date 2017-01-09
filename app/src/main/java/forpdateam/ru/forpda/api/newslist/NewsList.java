package forpdateam.ru.forpda.api.newslist;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.news.NewsModel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static forpdateam.ru.forpda.Constants.NEWS_CATEGORY_ALL;
import static forpdateam.ru.forpda.Constants.NEWS_CATEGORY_ARTICLES;
import static forpdateam.ru.forpda.Constants.NEWS_CATEGORY_GAMES;
import static forpdateam.ru.forpda.Constants.NEWS_CATEGORY_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_CATEGORY_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_ANDROID_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_HOW_TO_WP;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_IOS_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_WP7_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ALL;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ANDROID_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_URL_ARTICLES;
import static forpdateam.ru.forpda.Constants.NEWS_URL_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.Constants.NEWS_URL_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_URL_GAMES;
import static forpdateam.ru.forpda.Constants.NEWS_URL_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.Constants.NEWS_URL_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.Constants.NEWS_URL_HOW_TO_IOS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_HOW_TO_WP;
import static forpdateam.ru.forpda.Constants.NEWS_URL_IOS_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_URL_IOS_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_URL_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_SOFTWARE;
import static forpdateam.ru.forpda.Constants.NEWS_URL_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.Constants.NEWS_URL_WP7_GAME;
import static forpdateam.ru.forpda.Constants.NEWS_URL_WP7_SOFTWARE;

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
            Log.e("NewsModel", "Test date: " + matcher.group(5));
            item.setAuthor(matcher.group(6));
            item.setDescription(matcher.group(7));
            list.add(item);
        }
        return list;
    }

    public Observable<ArrayList<NewsItem>> getNews(final String url) {
        return Observable.fromCallable(() -> get(url));
    }

    public Observable<String> getNewsSource(@NonNull String urlNews) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(Client.getInstance().get(urlNews));
                    e.onComplete();
                }
            }
        });
    }



    /*Experimental*/
    public static List<NewsModel> getNewsItems(@NonNull String category, int number) {
        List<NewsModel> cache = new ArrayList<>();
        String url = getUrlCategory(category);
        if (number >= 2) {
            url = url + "page/" + number + "/";
        }
        String response = null;
        try {
            response = Client.getInstance().get(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response == null) return new ArrayList<>();

        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            NewsModel model = new NewsModel();
            model.setLink(matcher.group(1));
            model.setCategory(category);
            model.setImgLink(matcher.group(3));
            model.setTitle(matcher.group(2));
            model.setCommentsCount(matcher.group(4));
            model.setDate(matcher.group(5));
            model.setAuthor(matcher.group(6));
            model.setDescription(matcher.group(7));
            model.setRead(false);
            model.setOffline(false);
            cache.add(model);

        }
        return cache;
    }

    private static String getUrlCategory(@NonNull String category) {
        switch (category) {
            case NEWS_CATEGORY_ALL:
                return NEWS_URL_ALL;
            case NEWS_CATEGORY_ARTICLES:
                return NEWS_URL_ARTICLES;
            case NEWS_CATEGORY_REVIEWS:
                return NEWS_URL_REVIEWS;
            case NEWS_CATEGORY_SOFTWARE:
                return NEWS_URL_SOFTWARE;
            case NEWS_CATEGORY_GAMES:
                return NEWS_URL_GAMES;
            case NEWS_SUBCATEGORY_DEVSTORY_GAMES:
                return NEWS_URL_DEVSTORY_GAMES;
            case NEWS_SUBCATEGORY_WP7_GAME:
                return NEWS_URL_WP7_GAME;
            case NEWS_SUBCATEGORY_IOS_GAME:
                return NEWS_URL_IOS_GAME;
            case NEWS_SUBCATEGORY_ANDROID_GAME:
                return NEWS_URL_ANDROID_GAME;
            case NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE:
                return NEWS_URL_DEVSTORY_SOFTWARE;
            case NEWS_SUBCATEGORY_WP7_SOFTWARE:
                return NEWS_URL_WP7_SOFTWARE;
            case NEWS_SUBCATEGORY_IOS_SOFTWARE:
                return NEWS_URL_IOS_SOFTWARE;
            case NEWS_SUBCATEGORY_ANDROID_SOFTWARE:
                return NEWS_URL_ANDROID_SOFTWARE;
            case NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS:
                return NEWS_URL_SMARTPHONES_REVIEWS;
            case NEWS_SUBCATEGORY_TABLETS_REVIEWS:
                return NEWS_URL_TABLETS_REVIEWS;
            case NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS:
                return NEWS_URL_SMART_WATCH_REVIEWS;
            case NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS:
                return NEWS_URL_ACCESSORIES_REVIEWS;
            case NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS:
                return NEWS_URL_NOTEBOOKS_REVIEWS;
            case NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS:
                return NEWS_URL_ACOUSTICS_REVIEWS;
            case NEWS_SUBCATEGORY_HOW_TO_ANDROID:
                return NEWS_URL_HOW_TO_ANDROID;
            case NEWS_SUBCATEGORY_HOW_TO_IOS:
                return NEWS_URL_HOW_TO_IOS;
            case NEWS_SUBCATEGORY_HOW_TO_WP:
                return NEWS_URL_HOW_TO_WP;
            case NEWS_SUBCATEGORY_HOW_TO_INTERVIEW:
                return NEWS_URL_HOW_TO_INTERVIEW;
        }
        return NEWS_URL_ALL;
    }

    public static String getDetailsNewsItem(@NonNull String url) throws Exception {
        return Client.getInstance().get(url);
    }
}
