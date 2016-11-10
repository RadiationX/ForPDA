package forpdateam.ru.forpda.fragments.theme;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 11/2/16.
 */

public class ThemeModel extends RealmObject {

    @PrimaryKey
    private String themeId;
    private String themeLastPostUrl;

    public ThemeModel(String themeId, String themeLastPostUrl) {
        this.themeId = themeId;
        this.themeLastPostUrl = themeLastPostUrl;
    }

    public ThemeModel() {
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getThemeLastPostUrl() {
        return themeLastPostUrl;
    }

    public void setThemeLastPostUrl(String themeLastPostUrl) {
        this.themeLastPostUrl = themeLastPostUrl;
    }
}
