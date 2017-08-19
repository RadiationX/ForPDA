package forpdateam.ru.forpda.data.news.local;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.data.news.entity.News;

/**
 * Created by isanechek on 28.09.16.
 */

public class EntityMapping {

    public static List<News> mappingNews(String category, List<NewsItem> items) {
        ArrayList<News> cache = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            cache.add(mappingNews(category, items.get(i)));
        }
        return cache;
    }

    public static News mappingNews(String category, NewsItem item) {
        News news = new News();
        news.title = item.getTitle();
        news.url = item.getUrl();
        news.imgUrl = item.getImgUrl();
        news.author = item.getAuthor();
        news.date = item.getDate();
        news.commentsCount = item.getCommentsCount();
//        news.tags = item.getTags();
        news.category = category;
        news.description = item.getDescription();

        news.offline = false;
        news.favorite = false;
        news.readDone = false;
        news.newNews = false;
        return news;
    }

    public static News mappingNews(News news, NewsItem item) {
        if (news == null) {
            throw new IllegalArgumentException("Mapping news object null!");
        }

        news.body = item.html;
        if (news.newNews) {
            news.newNews = false;
            news.readDone = true;
        }
        // продолжение следует
        return news;
    }

    public static News mappingNews(NewsItem item) {
        News news = new News();
        news.body = item.html;
        return news;
    }
}
