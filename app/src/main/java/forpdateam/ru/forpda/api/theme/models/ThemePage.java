package forpdateam.ru.forpda.api.theme.models;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.theme.interfaces.IThemePage;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePage implements IThemePage {
    private String title, desc, html, url, elementToScroll;
    private int id = 0, forumId = 0, favId = 0, scrollY = 0;
    private boolean inFavorite = false, curator = false, quote = false, hatOpen = false, pollOpen = false;
    private ArrayList<ThemePost> posts = new ArrayList<>();
    private Pagination pagination = new Pagination();
    private Poll poll;

    public boolean isHatOpen() {
        return hatOpen;
    }

    public boolean isPollOpen() {
        return pollOpen;
    }

    public void setHatOpen(boolean hatOpen) {
        this.hatOpen = hatOpen;
    }

    public void setPollOpen(boolean pollOpen) {
        this.pollOpen = pollOpen;
    }

    public boolean canQuote() {
        return quote;
    }

    public void setCanQuote(boolean quote) {
        this.quote = quote;
    }

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

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

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

    public void addPost(ThemePost post) {
        posts.add(post);
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public int getSt() {
        return pagination.getCurrent() * pagination.getPerPage();
    }

    public int getFavId() {
        return favId;
    }

    public void setFavId(int favId) {
        this.favId = favId;
    }
}
