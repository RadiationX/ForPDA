package forpdateam.ru.forpda.api.search;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.utils.Utils;

/**
 * Created by radiationx on 01.02.17.
 */

public class Search {
    private final static Pattern forumTopicsPattern = Pattern.compile("<div[^>]*?data-topic=\"(\\d+)\"[^>]*?>[\\s\\S]*?(?:<font color=\"([^\"]*?)\"[^>]*?>([^<]*?)<\\/font>[\\s\\S]*?)?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?форум:[^<]*?<a[^>]*?showforum=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?автор:[^<]*?<a[^>]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?Послед[\\s\\S]*?<a[^>]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>(?:\\s*)?([^<]*?)<\\/div>");
    private final static Pattern forumPostsPattern = Pattern.compile("<div[^>]*?class=\"cat_name[^>]*?>[^<]*?<a[^>]*?showtopic=(\\d+)[^>]*?p=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?post_date[^>]*>([^&]*?)&[\\s\\S]*?<a[^>]*?showuser=(\\d+)[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/?[ia][\\s\\S]*?post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div[^>]*?class=\"cat_name[^>]*?>|<div><div[^>]*?class=\"pagination|<div><\\/div><br[^>]*?><\\/form>)");
    private final static Pattern newsListPattern = Pattern.compile("<li>[^<]*?<div[^>]*?class=\"photo\"[^>]*?>[\\s\\S]*?<a[^\"]*?href=\"[^\"]*?(\\d+)\\/\"[^>]*?>[\\s\\S]*?<img[^>]*?src=\"([\\s\\S]*?)\"[^>]*?>[\\s\\S]*?class=\"date[^>]*>([\\s\\S]*?)<\\/em>[\\s\\S]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<h\\d[^>]*>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<p>[^<]*?<a[^>]*>([\\s\\S]*?)<\\/a>[^<]*?<\\/p>");

    public SearchResult getSearch(SearchSettings settings) throws Exception {
        SearchResult result = new SearchResult();
        String response = Client.getInstance().get(settings.toUrl());
        Matcher matcher = null;
        SearchItem item = null;
        boolean isNews = settings.getResourceType().equals(SearchSettings.RESOURCE_NEWS.first);
        boolean resultTopics = settings.getResult().equals(SearchSettings.RESULT_TOPICS.first);
        Log.d("FORPDA_LOG", "params " + isNews + " : " + resultTopics + " : " + settings.getResourceType() + " : " + settings.getResult());
        if (isNews) {
            matcher = newsListPattern.matcher(response);
            while (matcher.find()) {
                item = new SearchItem();
                item.setId(Integer.parseInt(matcher.group(1)));
                item.setImage(matcher.group(2));
                item.setDate(matcher.group(3));
                item.setLastUserId(Integer.parseInt(matcher.group(4)));
                item.setLastUserNick(Utils.fromHtml(matcher.group(5)));
                item.setTitle(Utils.fromHtml(matcher.group(6)));
                item.setContent(matcher.group(7));
                result.addItem(item);
            }
        } else {
            if (resultTopics) {
                matcher = forumTopicsPattern.matcher(response);
                while (matcher.find()) {
                    item = new SearchItem();
                    item.setId(Integer.parseInt(matcher.group(1)));
                    item.setTitle(Utils.fromHtml(matcher.group(4)));
                    item.setLastUserId(Integer.parseInt(matcher.group(9)));
                    item.setLastUserNick(Utils.fromHtml(matcher.group(10)));
                    item.setDate(matcher.group(11));
                    result.addItem(item);
                }
            } else {
                matcher = forumPostsPattern.matcher(response);
                while (matcher.find()) {
                    item = new SearchItem();
                    item.setId(Integer.parseInt(matcher.group(1)));
                    item.setPostId(Integer.parseInt(matcher.group(2)));
                    item.setTitle(Utils.fromHtml(matcher.group(3)));
                    item.setDate(matcher.group(4));
                    item.setLastUserId(Integer.parseInt(matcher.group(5)));
                    item.setAvatar(matcher.group(6));
                    item.setLastUserNick(Utils.fromHtml(matcher.group(7)));
                    item.setContent(matcher.group(8));
                    result.addItem(item);
                }
            }
        }

        if (isNews) {
            result.setPagination(Pagination.parseNews(response));
        } else {
            result.setPagination(Pagination.parseForum(response));
        }
        result.setSettings(settings);
        return result;
    }
}
