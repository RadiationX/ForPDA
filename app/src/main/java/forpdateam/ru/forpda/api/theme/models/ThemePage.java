package forpdateam.ru.forpda.api.theme.models;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.theme.interfaces.IThemePage;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePage implements IThemePage {
    private String title, desc, html;
    private boolean inFavorite;
    private int postsOnPageCount = 20, allPagesCount = 0, currentPage = 0;
    private ArrayList<ThemePost> posts = new ArrayList<>();

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public boolean isInFavorite() {
        return inFavorite;
    }

    @Override
    public int getPostsOnPageCount() {
        return postsOnPageCount;
    }

    @Override
    public int getAllPagesCount() {
        return allPagesCount;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public ArrayList<ThemePost> getPosts() {
        return posts;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setInFavorite(boolean inFavorite) {
        this.inFavorite = inFavorite;
    }

    public void setPostsOnPageCount(int postsOnPageCount) {
        this.postsOnPageCount = postsOnPageCount;
    }

    public void setAllPagesCount(int allPagesCount) {
        this.allPagesCount = allPagesCount;
    }

    public void addPost(ThemePost post) {
        posts.add(post);
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
