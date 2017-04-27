package forpdateam.ru.forpda.api.search.models;

import forpdateam.ru.forpda.api.BaseForumPost;
import forpdateam.ru.forpda.api.search.interfaces.ISearchItem;

/**
 * Created by radiationx on 01.02.17.
 */

public class SearchItem extends BaseForumPost implements ISearchItem {
    private String title, desc, imageUrl;

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
