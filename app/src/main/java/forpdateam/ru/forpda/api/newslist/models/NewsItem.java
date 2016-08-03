package forpdateam.ru.forpda.api.newslist.models;

import forpdateam.ru.forpda.api.newslist.interfaces.INewsItem;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsItem implements INewsItem {
    private String link, title, imageUrl, commentsCount, date, author, description;

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getCommentsCount() {
        return commentsCount;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
