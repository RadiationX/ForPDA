package forpdateam.ru.forpda.fragments.news.details.blocks;

/**
 * Created by isanechek on 8/19/17.
 */

public class YoutubeBlock {

    private String title;
    private String id;

    public YoutubeBlock(String id) {
        this.id = id;
    }

    public YoutubeBlock(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
