package forpdateam.ru.forpda.fragments.news.models;

import java.util.List;

/**
 * Created by isanechek on 12.01.17.
 */

public class NewsCallbackModel {

    private List<NewsModel> cache;
    private boolean fromNetwork;
    private boolean showMore;
    private NewsExceptionModel model;

    public NewsCallbackModel(List<NewsModel> cache, boolean fromNetwork, boolean showMore) {
        this.cache = cache;
        this.fromNetwork = fromNetwork;
        this.showMore = showMore;
    }

    public NewsCallbackModel(List<NewsModel> cache) {
        this.cache = cache;
    }

    public NewsCallbackModel(NewsExceptionModel model) {
        this.model = model;
    }

    public NewsCallbackModel() {
    }

    public boolean isShowMore() {
        return showMore;
    }

    public void setShowMore(boolean showMore) {
        this.showMore = showMore;
    }

    public List<NewsModel> getCache() {
        return cache;
    }

    public void setCache(List<NewsModel> cache) {
        this.cache = cache;
    }

    public boolean isFromNetwork() {
        return fromNetwork;
    }

    public void setFromNetwork(boolean fromNetwork) {
        this.fromNetwork = fromNetwork;
    }

    public NewsExceptionModel getModel() {
        return model;
    }

    public void setModel(NewsExceptionModel model) {
        this.model = model;
    }
}
