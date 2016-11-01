package forpdateam.ru.forpda.api.theme.models;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.theme.interfaces.IThemePage;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePage implements IThemePage {
    private String title, desc, html, url, elementToScroll;
    private boolean inFavorite = false, curator = false, quote = false;

    public boolean canQuote() {
        return quote;
    }

    public void setCanQuote(boolean quote) {
        this.quote = quote;
    }

    private int id = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCurator() {
        return curator;
    }

    public void setCurator(boolean curator) {
        this.curator = curator;
    }

    private int postsOnPageCount = 20, allPagesCount = 0, currentPage = 0, scrollY = 0;

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    private ArrayList<ThemePost> posts = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getElementToScroll() {
        return elementToScroll;
    }

    public void setElementToScroll(String elementToScroll) {
        this.elementToScroll = elementToScroll;
    }

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
