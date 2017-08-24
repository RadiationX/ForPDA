package forpdateam.ru.forpda.fragments.news.details.blocks;

/**
 * Created by isanechek on 8/19/17.
 */

public class YoutubeBlock {

    private String title;
    private String id;
    private String previewImgUrl;
    private String url;

    public YoutubeBlock(String id) {
        this.id = id;
    }

    public YoutubeBlock(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public YoutubeBlock(String id, String previewImgUrl, String url) {
        this.id = id;
        this.previewImgUrl = previewImgUrl;
        this.url = url;
    }

    public YoutubeBlock(String title, String id, String previewImgUrl, String url) {
        this.title = title;
        this.id = id;
        this.previewImgUrl = previewImgUrl;
        this.url = url;
    }

    public String getPreviewImgUrl() {
        return previewImgUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
