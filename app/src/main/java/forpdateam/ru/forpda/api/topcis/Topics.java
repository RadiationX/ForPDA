package forpdateam.ru.forpda.api.topcis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.topcis.models.TopicItem;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;

/**
 * Created by radiationx on 01.03.17.
 */

public class Topics {
    private final static Pattern titlePattern = Pattern.compile("<div[^>]*?navstrip[^>]*?>[\\s\\S]*?showforum=(\\d+)[^>]*?>([^<]*?)<\\/a>[^<]*?<\\/div>");
    private final static Pattern canNewTopicPattern = Pattern.compile("<a[^>]*?href=\"[^\"]*?do=new_post[^\"]*?\"[^>]*?>");
    private final static Pattern announcePattern = Pattern.compile("<div[^>]*?anonce_body[^>]*?>[\\s\\S]*?<a[^>]*?href=['\"]([^\"']*?)[\"'][^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/div>");
    private final static Pattern forumPattern = Pattern.compile("<div[^>]*?board_forum_row[^>]*?>[\\s\\S]*?<a[^>]*?showforum=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a>");
    private final static Pattern topicsPattern = Pattern.compile("<div[^>]*?data-topic=\"(\\d+)\"[^>]*?>[^<]*?<div[^>]*?class=\"topic_title\"[^>]*?>[^<]*?<span[^>]*?class=\"modifier\"[^>]*?>(?:[^<]*?<font[^>]*?>)?([^<]*?)(?:<\\/font>)?<\\/span>[^<]*?(\\(!\\))?[^<]*?<a[^>]*?href=\"[^\"]*?\"[^>]*?>([\\s\\S]*?)<\\/a>(?: ?&nbsp;[\\s\\S]*?<\\/div>|<\\/div>)[^<]*?<div[^>]*?class=\"topic_body\"[^>]*?>(?:[^<]*?<span[^>]*?class=\"topic_desc\"[^>]*?>(?!автор)([\\s\\S]*?)<br\\s?\\/>[^<]*?<\\/span>)?[^<]*?<span[^>]*?class=\"topic_desc\"[^>]*?>[^<]*?<a[^>]*?showuser=(\\d+)[^>]*?>([^<]*?)<\\/a>[^<]*?<\\/span>[\\s\\S]*?showuser=(\\d+)[^>]*?>([^<]*?)<\\/a>\\s?([^<]*?)(?:<span[\\s\\S]*?showuser=(\\d+)[^>]*?>([^<]*?)<\\/a>[^<]*?<\\/span>)?<\\/div>[^<]*?<\\/div>");

    public TopicsData getTopics(int id, int st) throws Exception {
        TopicsData data = new TopicsData();
        String response = Api.getWebClient().get("http://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(id)).concat("&st=").concat(Integer.toString(st)));

        Matcher matcher = titlePattern.matcher(response);
        if (matcher.find()) {
            data.setId(Integer.parseInt(matcher.group(1)));
            data.setTitle(Utils.fromHtml(matcher.group(2)));
        } else {
            data.setId(id);
        }

        matcher = canNewTopicPattern.matcher(response);
        data.setCanCreateTopic(matcher.find());

        matcher = announcePattern.matcher(response);
        while (matcher.find()) {
            TopicItem item = new TopicItem();
            item.setAnnounce(true);
            item.setAnnounceUrl(matcher.group(1));
            item.setTitle(Utils.fromHtml(matcher.group(2)));
            data.addAnnounceItem(item);
        }

        matcher = topicsPattern.matcher(response);
        while (matcher.find()) {
            TopicItem item = new TopicItem();
            item.setId(Integer.parseInt(matcher.group(1)));
            int p = 0;
            String tmp = matcher.group(2);
            if (tmp.contains("+")) p |= TopicItem.NEW_POST;
            if (tmp.contains("-")) p |= TopicItem.NO_NEW_POST;
            if (tmp.contains("^")) p |= TopicItem.POLL;
            if (tmp.contains("Х")) p |= TopicItem.CLOSED;
            item.setParams(p);
            item.setPinned(matcher.group(3) != null);
            item.setTitle(Utils.fromHtml(matcher.group(4)));
            tmp = matcher.group(5);
            if (tmp != null)
                item.setDesc(Utils.fromHtml(tmp));
            item.setAuthorId(Integer.parseInt(matcher.group(6)));
            item.setAuthorNick(Utils.fromHtml(matcher.group(7)));
            item.setLastUserId(Integer.parseInt(matcher.group(8)));
            item.setLastUserNick(Utils.fromHtml(matcher.group(9)));
            item.setDate(matcher.group(10));
            tmp = matcher.group(11);
            if (tmp != null) {
                item.setCuratorId(Integer.parseInt(tmp));
                item.setCuratorNick(Utils.fromHtml(matcher.group(12)));
            }
            if (item.isPinned()) {
                data.addPinnedItem(item);
            } else {
                data.addTopicItem(item);
            }
        }
        matcher = forumPattern.matcher(response);
        while (matcher.find()){
            TopicItem topicItem = new TopicItem();
            topicItem.setId(Integer.parseInt(matcher.group(1)));
            topicItem.setTitle(matcher.group(2));
            topicItem.setForum(true);
            data.addForumItem(topicItem);
        }
        data.setPagination(Pagination.parseForum(response));
        return data;
    }
}
