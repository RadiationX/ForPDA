package forpdateam.ru.forpda.api.reputation;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.OnlyShowException;
import forpdateam.ru.forpda.utils.Utils;

/**
 * Created by radiationx on 20.03.17.
 */

public class Reputation {
    public final static String MODE_TO = "to";
    public final static String MODE_FROM = "from";
    public final static String SORT_ASC = "asc";
    public final static String SORT_DESC = "desc";
    private final static Pattern listPattern = Pattern.compile("<tr>[^<]*?<td[^>]*?><strong><a [^>]*?showuser=(\\d+)[^\"]*\">([\\s\\S]*?)<\\/a><\\/strong><\\/td>[^<]*?<td[^>]*?>(?:<strong><a href=\"([^\"]*?)\">([\\s\\S]*?)<\\/a><\\/strong>)?[^<]*?<\\/td>[^<]*?<td[^>]*?>([\\s\\S]*?)<\\/td>[^<]*?<td[^>]*?><img[^>]*?src=\"([^\"]*?)\"[^>]*?><\\/td>[^<]*?<td[^>]*?>([\\s\\S]*?)<\\/td>[^<]*?<\\/tr>");
    private final static Pattern infoPattern = Pattern.compile("<div class=\"maintitle\">[\\s\\S]*?<a href=\"[^\"]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)<\\/a>[^\\[]*?\\[\\+(\\d+)\\/\\-(\\d+)\\]");

    public RepData getReputation(RepData data) throws Exception {
        if (data == null) return null;
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=rep&view=history&mid=" + data.getId() + "&mode=" + data.getMode() + "&order=" + data.getSort() + "&st=" + data.getPagination().getSt());
        Matcher matcher = infoPattern.matcher(response);
        if (matcher.find()) {
            data.setId(Integer.parseInt(matcher.group(1)));
            data.setNick(Utils.fromHtml(matcher.group(2)));
            data.setPositive(Integer.parseInt(matcher.group(3)));
            data.setNegative(Integer.parseInt(matcher.group(4)));
        }


        matcher = listPattern.matcher(response);
        String temp;
        data.getItems().clear();
        while (matcher.find()) {
            RepItem item = new RepItem();
            item.setUserId(Integer.parseInt(matcher.group(1)));
            item.setUserNick(Utils.fromHtml(matcher.group(2)));
            temp = matcher.group(3);
            if (temp != null) {
                item.setSourceUrl(temp);
                item.setSourceTitle(Utils.fromHtml(matcher.group(4)));
            }
            item.setTitle(Utils.fromHtml(matcher.group(5)));
            item.setImage(matcher.group(6));
            item.setDate(matcher.group(7));
            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(data.getPagination(), response));
        return data;
    }

    public String editReputation(int postId, int userId, boolean type, String message) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "rep");
        if (postId > 0)
            headers.put("p", Integer.toString(postId));
        headers.put("mid", Integer.toString(userId));
        headers.put("type", type ? "add" : "minus");
        headers.put("message", message);

        try {
            Client.getInstance().post("http://4pda.ru/forum/index.php", headers);
        } catch (OnlyShowException exception) {
            return exception.getMessage();
        }
        return "";
    }

    public static RepData fromUrl(String url) {
        return fromUrl(new RepData(), url);
    }

    public static RepData fromUrl(RepData data, String url) {
        Uri uri = Uri.parse(url);

        Matcher matcher = Pattern.compile("st=(\\d+)").matcher(url);
        if (matcher.find()) {
            data.getPagination().setSt(Integer.parseInt(matcher.group(1)));
        }
        matcher = Pattern.compile("mid=(\\d+)").matcher(url);
        if (matcher.find())
            data.setId(Integer.parseInt(matcher.group(1)));
        matcher = Pattern.compile("mode=([^&]+)").matcher(url);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case MODE_FROM:
                    data.setMode(MODE_FROM);
                    break;
                case MODE_TO:
                    data.setMode(MODE_TO);
                    break;
            }
        }

        matcher = Pattern.compile("order=([^&]+)").matcher(url);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case SORT_ASC:
                    data.setMode(SORT_ASC);
                    break;
                case SORT_DESC:
                    data.setMode(SORT_DESC);
                    break;
            }
        }
        return data;
    }
}
