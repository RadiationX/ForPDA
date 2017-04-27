package forpdateam.ru.forpda.api.theme.interfaces;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.theme.models.ThemePostBase;

/**
 * Created by radiationx on 04.08.16.
 */
public interface IThemePage {
    String getTitle();

    String getDesc();

    boolean isInFavorite();

    ArrayList<ThemePostBase> getPosts();

    String getHtml();
}
