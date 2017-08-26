package forpdateam.ru.forpda.api.news;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.api.regex.RegexStorage;
import io.reactivex.Single;

import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_SOFTWARE;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsApi {
    /*
    * 1. Дата курильщика (2017-08-24T07:00:00+00:00)
    * 2. Урл изображения
    * 3. Заголовок
    * 4. Дата нормального человека (24.08.17)
    * 5. Id автора
    * 6. Ник автора
    * 7. Сорсы тегов
    * 8. Вроде как контент
    * 9. Сорсы материалов по теме, их может не быть (null у group(9))
    * 10. Магический относительный id новости для навигации вперёд/назад
    * 11. Кол-во комментов
    * 12. Сорсы комментов
    * */
    private final Pattern detailsPattern = Pattern.compile("<section[^>]*>[^<]*?<article[^>]*?>[\\s\\S]*?<meta[^>]*?content=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div[^>]*?class=\"photo\"[^>]*?>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div[^>]*?class=\"description\"[^>]*?>[^<]*?<h1[^>]*?>(?:<span[^>]*?>)?([^<]*?)(?:<\\/span>)?<\\/h1>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[^<]*?<span[^>]*?class=\"name\"[^>]*?>[^<]*?<a[^>]*?href=\"[^\"]*?(\\d+)\"[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?class=\"more-box\"[^>]*?>[\\s\\S]*?<div[^>]*?class=\"meta\"[^>]*?>([\\s\\S]*?)<\\/div>[\\s\\S]*?<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"materials-box\"[^>]*?>[\\s\\S]*?<ul class=\"materials-slider\"[^>]*?>([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>)?[^<]*?<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>(\\d+)[^<]*?<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?<\\/ul>)[^<]*?<form");

    /*
    * 1. tag для ссылки
    * 2. Заголовок
    * */
    private final Pattern tagsPattern = Pattern.compile("<a[^>]*?href=\"\\/tag\\/([^\"\\/]*?)\\/\"[^>]*?>([^<]*?)<\\/a>");


    public String getLink(@Nullable String category, int pageNumber) {
        String link = getUrlCategory(category);
        if (pageNumber >= 2) {
            link = link + "page/" + pageNumber + "/";
        }
        return link;
    }

    public List<NewsItem> getNews(String category, int pageNumber) throws Exception {
        String url = getLink(category, pageNumber);
        String response = Api.getWebClient().get(url).getBody();
        ArrayList<NewsItem> cache = new ArrayList<>();
        final String regex = RegexStorage.News.List.getListPattern();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            NewsItem model = new NewsItem();
            if (matcher.group(1) == null) {
                model.setUrl(matcher.group(9));
                model.setImgUrl(matcher.group(10));
                model.setTitle(Utils.fromHtml(matcher.group(11)));
                model.setCommentsCount(matcher.group(12));
                model.setDate(matcher.group(14).replace('-', '.'));
                model.setAuthor(matcher.group(15));
                model.setDescription(matcher.group(17).trim());

            } else {
                model.setUrl(matcher.group(1));
                model.setImgUrl(matcher.group(3));
                model.setTitle(Utils.fromHtml(matcher.group(2)));
                model.setCommentsCount(matcher.group(4));
                model.setDate(matcher.group(5));
                model.setAuthor(Utils.fromHtml(matcher.group(6)));
                model.setDescription(Utils.fromHtml(matcher.group(7)));
//            model.setTags(matcher.group(8));
            }

            cache.add(model);
        }
        return cache;
    }


    public NewsItem getDetails(String url) throws Exception {
        String response = Api.getWebClient().get("https:" + url).getBody();
        Matcher matcher = detailsPattern.matcher(response);
        NewsItem item = new NewsItem();
        if (matcher.find()) {
            item.setImgUrl(matcher.group(2));
            item.setTitle(Utils.fromHtml(matcher.group(3)));
            item.setDate(matcher.group(4));
            item.setAuthor(matcher.group(6));
            item.setHtml(matcher.group(8));
            item.setMoreNews(matcher.group(9));
            item.setNavId(matcher.group(10));
            item.setCommentsCount(matcher.group(11));
            item.setComments(matcher.group(12));
        }
        return item;
    }


    private static String getUrlCategory(@Nullable String category) {
        Log.d("SUKA", "getUrlCategory " + category);
        if (category == null) return NEWS_URL_ALL;
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
}
