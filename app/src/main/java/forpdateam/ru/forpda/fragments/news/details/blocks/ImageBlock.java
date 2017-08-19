package forpdateam.ru.forpda.fragments.news.details.blocks;

/**
 * Created by isanechek on 8/19/17.
 */

public class ImageBlock {

    private String title; // если есть
    private String imageUrl;

    public ImageBlock(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageBlock(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return "http:" + imageUrl;
    }
}
