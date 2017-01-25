package forpdateam.ru.forpda.fragments.news.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsModel extends RealmObject {
    public static final String CATEGORY = "category";

    @PrimaryKey
    private String link;
    private String category;
    private String imgLink;
    private String title;
    private String commentsCount;
    private String date;
    private String author;
    private String description;
    private boolean read;
    private boolean newNews;


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isNewNews() {
        return newNews;
    }

    public void setNewNews(boolean newNews) {
        this.newNews = newNews;
    }
}
