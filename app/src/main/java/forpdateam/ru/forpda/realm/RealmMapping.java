package forpdateam.ru.forpda.realm;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.news.models.NewsNetworkModel;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;

/**
 * Created by isanechek on 28.09.16.
 */

public class RealmMapping {

    public static NewsModel mappingNews(NewsNetworkModel item) {
        NewsModel newsModel = new NewsModel();
        newsModel.setLink(item.getLink());
        newsModel.setImgLink(item.getImageUrl());
        newsModel.setAuthor(item.getAuthor());
        newsModel.setCommentsCount(item.getCommentsCount());
        newsModel.setDate(item.getDate());
        newsModel.setDescription(item.getDescription());
        newsModel.setTitle(item.getTitle());
        newsModel.setCategory(item.getCategory());
        return newsModel;
    }

    public static List<NewsModel> getMappingNewsList(ArrayList<NewsNetworkModel> networkModels) {
        ArrayList<NewsModel> cache = new ArrayList<>();
        Stream.of(networkModels).map(RealmMapping::mappingNews).forEach(cache::add);
        return cache;
    }
}
