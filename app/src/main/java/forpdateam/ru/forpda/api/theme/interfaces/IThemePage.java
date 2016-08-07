package forpdateam.ru.forpda.api.theme.interfaces;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.theme.models.ThemePost;

/**
 * Created by radiationx on 04.08.16.
 */
public interface IThemePage {
    String getTitle();

    String getDesc();

    boolean isInFavorite();

    int getPostsOnPageCount();

    int getAllPagesCount();

    int getCurrentPage();

    boolean isFirstPage();

    boolean isLastPage();

    ArrayList<ThemePost> getPosts();
}
