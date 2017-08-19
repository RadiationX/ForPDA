package forpdateam.ru.forpda.api.news;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.news.html.NewsHtmlBuilder;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.api.regex.RegexStorage;
import forpdateam.ru.forpda.fragments.news.details.blocks.ContentBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.GalleryBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.ImageBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.TitleBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.YoutubeBlock;
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
import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsApi {

    public List<NewsItem> mappingNewsList(@Nullable String source) {
        if (source == null) return new ArrayList<>();
        ArrayList<NewsItem> cache = new ArrayList<>();
        final String regex = RegexStorage.News.List.getListPattern();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            NewsItem model = new NewsItem();
            model.setUrl(matcher.group(1));
            model.setImgUrl(matcher.group(3));
            model.setTitle(Utils.fromHtml(matcher.group(2)));
            model.setCommentsCount(matcher.group(4));
            model.setDate(matcher.group(5));
            model.setAuthor(Utils.fromHtml(matcher.group(6)));
            model.setDescription(Utils.fromHtml(matcher.group(7)));
//            model.setTags(matcher.group(8));
            cache.add(model);
        }
        return cache;
    }

    public String getSource(@Nullable String category, int pageNumber) {
        String link = getUrlCategory(category);
        if (pageNumber >= 2) {
            link = link + "page/" + pageNumber + "/";
        }
        String finalLink = link;
        String res = get(finalLink);
        if (res == null) {
            res = "";
        }
        return res;
    }

    public Single<String> getRxSource(@Nullable String category, int pageNumber) {
        return Single.fromCallable(() -> getSource(category, pageNumber));
    }

    public Single<String> getRxSource(@Nullable String url) {
        return Single.fromCallable(() -> get(url));
    }

    private String get(@NonNull String url) {
        String response = "";
        try {
            response = Api.getWebClient().get(url).getBody();
            Log.d("Repo", "Res " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


    private String getUrlCategory(@Nullable String category) {
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

    /*========================DETAILS=============================*/
    private final Pattern pattern1 = Pattern.compile("<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"materials-box\"[^>]*?>[\\s\\S]*?<ul class=\"materials-slider\"[^>]*?>([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>)?[^<]*?<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>(\\d+)[^<]*?<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?<\\/ul>)[^<]*?<form");


    public Single<NewsItem> loadTestDetails(String url) {
        return Single.fromCallable(() -> {
            String regex = RegexStorage.News.Details.getRootDetailsPattern();
            Pattern p = Pattern.compile(regex);
            final Pattern content = Pattern.compile("<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?");
            String response = Api.getWebClient().get("https:" +url).getBody();
            Matcher matcher = pattern1.matcher(response);
            NewsItem item = new NewsItem();
            if (matcher.find()) {
                item.html = matcher.group(1);
//                item.html = NewsHtmlBuilder.transformBody(matcher.group(1));
//                item.moreNews = matcher.group(2);
//                item.navId = matcher.group(3);
//                item.comments = matcher.group(5);
            }
            return item;
        });
    }

    public List<Object> action(String content) {
        log("Action " + content);
        ArrayList<Object> cache = new ArrayList<>();
        Document doc = Jsoup.parseBodyFragment(content);
        Element element = doc.body();
        log("Elements size " + element.children().size());
        log("Elements size " + element.children().get(0).toString());
        for (Element e : element.children()) {
            String tagName = e.tagName();
            if (tagName.equals("p")) {
                if (e.children().size() > 0) {
                    if (checkChild(e)) {
                        // Пока хз как правильно сделать, но как бы и не должно быть такого варианта.
                        if (e.text().length() > 0) {
                            log("Пиздос здесь.");

                        } else {
                            if (isYoutube(e.toString())) {
                                // Вообще тут надо только id видео
                                //  Короче позже надо будет заменить
                                String url = element.select("iframe").attr("src");
                                cache.add(new YoutubeBlock(url));
                            } else  if (isImage(e.toString())) {
                                String url = e.select("img").attr("src");
                                cache.add(new ImageBlock(url));
                            }
                        }
                    } else {
                        // Ну как бы тут в тексте вроде ссылки есть на другие ресурсы.
                        cache.add(new ContentBlock(e.toString()));
                    }
                } else {
                    // Внутри тега нет детей, значит просто текст.
                    String text = e.text();
                    cache.add(new ContentBlock(e.text()));
                }
            } else if (tagName.equals("h2")) {
                cache.add(new TitleBlock(e.text()));
            } else if (tagName.equals("ul")) {
                ArrayList<String> urls = new ArrayList<String>();
                for (Element li : e.children()) {
                    String url = li.select("img").attr("src");
                    if (url != null) {
                        urls.add(url);
                    }
                }

                log("gallery size " + urls.size());
                if (urls.size() > 0) {
                    cache.add(new GalleryBlock(urls));
                }
            }
        }
        log("Cache size " + cache.size());
        return cache;
    }

    private boolean checkChild(Element e) {
        return isImage(e.toString()) || isYoutube(e.toString());

    }

    private boolean isImage(@NonNull String source) {
        Pattern pattern = Pattern.compile("([a-z\\-_0-9\\/\\:\\.]*\\.(jpg|jpeg|png|gif))");
        Matcher matcher = pattern.matcher(source);
        return matcher.find();
    }

    private boolean isIFrame(@NonNull String source) {
        return source.matches("iframe");
    }

    private boolean isYoutube(@NonNull String source) {
        Pattern pattern = Pattern.compile("((http(s)?:\\/\\/)?)(www\\.)?((youtube\\.com\\/)|(youtu.be\\/))[\\S]+");
        Matcher matcher = pattern.matcher(source);
        return matcher.find();
    }

}
