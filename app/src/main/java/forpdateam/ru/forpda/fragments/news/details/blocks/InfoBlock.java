package forpdateam.ru.forpda.fragments.news.details.blocks;

/**
 * Created by isanechek on 8/19/17.
 * Херня, которая сразу идет хэдера с картинкой
 */

public class InfoBlock {
    private String title;
    private String author;
    private String date;

    public InfoBlock(String title, String author, String date) {
        this.title = title;
        this.author = author;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }
}
