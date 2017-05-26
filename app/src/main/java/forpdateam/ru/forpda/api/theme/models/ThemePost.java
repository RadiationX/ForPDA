package forpdateam.ru.forpda.api.theme.models;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.BaseForumPost;
import forpdateam.ru.forpda.api.theme.interfaces.IThemePost;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePost extends BaseForumPost implements IThemePost {
    private List<Pair<String, String>> attachImages = new ArrayList<>();

    public ThemePost() {
    }

    public List<Pair<String, String>> getAttachImages() {
        return attachImages;
    }

    public void addAttachImage(String urlId, String name) {
        attachImages.add(new Pair<>("http://".concat(urlId), name));
    }
}
