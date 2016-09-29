package forpdateam.ru.forpda.realm;

import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.fragments.news.News;

/**
 * Created by isanechek on 28.09.16.
 */

public class RealmMapping {

    public static News mappingNews(NewsItem item) {
        News news = new News();
        news.setLink(item.getLink());
        news.setImgLink(item.getImageUrl());
        news.setAuthor(item.getAuthor());
        news.setCommentsCount(item.getCommentsCount());
        news.setDate(item.getDate());
        news.setDescription(item.getDescription());
        news.setTitle(item.getTitle());
        return news;
    }
}
