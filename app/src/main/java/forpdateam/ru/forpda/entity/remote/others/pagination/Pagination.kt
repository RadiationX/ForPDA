package forpdateam.ru.forpda.entity.remote.others.pagination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 03.03.17.
 */

public class Pagination {
    private final static Pattern forumPaginationPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>");
    private final static Pattern newsPaginationPattern = Pattern.compile("class=\"s-count[\\s\\S]*?<strong>(\\d+)<\\/strong>[\\s\\S]*?<ul class=\"page-nav[^>]*?>[\\s\\S]*?<li class=\"active\"><a[^>]*?>(\\d+)");
    private int perPage = 20, all = 1, current = 1, st = 0;
    private boolean isForum = true;

    public boolean isForum() {
        return isForum;
    }

    public void setForum(boolean forum) {
        isForum = forum;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getPage(int page) {
        if (!isForum) return page;
        return page * perPage;
    }

    public static Pagination parseNews(String page) {
        return parseNews(new Pagination(), page);
    }

    public static Pagination parseNews(Pagination pagination, String page) {
        pagination.setForum(false);
        Matcher matcher = newsPaginationPattern.matcher(page);
        if (matcher.find()) {
            pagination.setPerPage(30);
            pagination.setAll((int) Math.ceil(Integer.parseInt(matcher.group(1)) / 30d));
            pagination.setCurrent(Integer.parseInt(matcher.group(2)));
        }
        return pagination;
    }

    public static Pagination parseForum(String page) {
        return parseForum(new Pagination(), page);
    }

    public static Pagination parseForum(Pagination pagination, String page) {
        Matcher matcher = forumPaginationPattern.matcher(page);
        if (matcher.find()) {
            pagination.setAll(Integer.parseInt(matcher.group(1)) + 1);
            pagination.setPerPage(Integer.parseInt(matcher.group(2)));
            pagination.setCurrent(Integer.parseInt(matcher.group(3)));
        }
        return pagination;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }
}
