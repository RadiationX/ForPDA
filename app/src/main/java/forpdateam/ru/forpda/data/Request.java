package forpdateam.ru.forpda.data;

import android.support.annotation.Nullable;

public class Request {
    @Nullable
    private String url = null;
    @Nullable
    private String category = null;
    private int pageNumber = 0;
    @Nullable
    private String title = null;
    private long id;

    public Request(@Nullable String category, @Nullable String title) {
        this.category = category;
        this.title = title;
    }

    public Request(@Nullable String category, int pageNumber) {
        this.category = category;
        this.pageNumber = pageNumber;
    }

    public Request(@Nullable String url) {
        this.url = url;
    }

    public Request(String url, long id) {
        this.url = url;
        this.id = id;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    @Nullable
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(@Nullable int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
