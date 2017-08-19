package forpdateam.ru.forpda.fragments.news.details.blocks;

import java.util.ArrayList;

/**
 * Created by isanechek on 8/19/17.
 */

public class GalleryBlock {
    private String title;
    private ArrayList<String> urls;

    public GalleryBlock(ArrayList<String> urls) {
        this.urls = urls;
    }

    public GalleryBlock(String title, ArrayList<String> urls) {
        this.title = title;
        this.urls = urls;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }
}
