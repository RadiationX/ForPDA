package forpdateam.ru.forpda.realm;

import com.annimon.stream.Stream;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.fragments.news.NewsModel;
import io.realm.RealmList;

/**
 * Created by isanechek on 28.09.16.
 */

public class RealmMapping {

    public static NewsModel mappingNews(NewsItem item) {
        NewsModel newsModel = new NewsModel();
        newsModel.setLink(item.getLink());
        newsModel.setImgLink(item.getImageUrl());
        newsModel.setAuthor(item.getAuthor());
        newsModel.setCommentsCount(item.getCommentsCount());
        newsModel.setDate(item.getDate());
        newsModel.setDescription(item.getDescription());
        newsModel.setTitle(item.getTitle());
        return newsModel;
    }

    public static RealmList<NewsModel> mappingNews(ArrayList<NewsItem> items) {
        RealmList<NewsModel> results = new RealmList<>();
        Stream.of(items).map(RealmMapping::mappingNews).forEach(results::add);
        return results;
    }
}
