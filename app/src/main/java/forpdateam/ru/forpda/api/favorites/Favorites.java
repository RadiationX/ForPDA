package forpdateam.ru.forpda.api.favorites;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.client.ForPdaRequest;

/**
 * Created by radiationx on 22.09.16.
 */

public class Favorites {
    private final static Pattern mainPattern = Pattern.compile("<div data-item-fid=\"([^\"]*)\" data-item-track=\"([^\"]*)\" data-item-pin=\"([^\"]*)\">[\\s\\S]*?class=\"modifier\">(<font color=\"([^\"]*)\">|)([^< ]*)(<\\/font>|)<\\/span><a href=\"[^\"]*=(\\d*)[^\"]*?\"[^>]*?>(<strong>|)([^<]*)(<\\/strong>|)<\\/a>[\\s\\S]*?(<a href=\"[^\"]*=(\\d*)\">\\((\\d*?)\\)[^<]*?<\\/a>|)<\\/div>[\\s\\S]*?topic_desc\">([^<]*|)(<br[^>]*>|)[\\s\\S]*?showforum=([^\"]*?)\">([^<]*)<\\/a><br[^>]*>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a> ([^<]*?)<");
    private final static Pattern checkPattern = Pattern.compile("<div style=\"[^\"]*background:#dff0d8[^\"]*\">[\\s\\S]*<div id=\"navstrip");
    private final static Pattern pagesPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>");
    public final static int ACTION_EDIT_SUB_TYPE = 0;
    public final static int ACTION_EDIT_PIN_STATE = 1;
    public final static int ACTION_DELETE = 2;
    public final static int ACTION_ADD = 3;
    public final static String[] SUB_TYPES = {"none", "delayed", "immediate", "daily", "weekly", "pinned"};
    public final static CharSequence[] SUB_NAMES = {"Не уведомлять", "Первый раз", "Каждый раз", "Каждый день", "Каждую неделю", "При изменении первого поста"};

    public FavData getFavorites(int st) throws Exception {
        FavData data = new FavData();
        final String response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=fav&st=".concat(Integer.toString(st)));
        long time = System.currentTimeMillis();
        Matcher matcher = mainPattern.matcher(response);
        FavItem item;
        while (matcher.find()) {
            item = new FavItem();
            item.setFavId(Integer.parseInt(matcher.group(1)));
            item.setTrackType(matcher.group(2));
            item.setPin(matcher.group(3).equals("1"));
            if (!matcher.group(4).isEmpty())
                item.setInfoColor(matcher.group(5));
            item.setInfo(matcher.group(6));
            item.setTopicId(Integer.parseInt(matcher.group(8)));
            item.setNewMessages(!matcher.group(9).isEmpty());
            item.setTopicTitle(Utils.fromHtml(matcher.group(10)));
            if (!matcher.group(12).isEmpty()) {
                item.setStParam(Integer.parseInt(matcher.group(13)));
                item.setPages(Integer.parseInt(matcher.group(14)));
            }
            if (!matcher.group(15).isEmpty())
                item.setDesc(Utils.fromHtml(matcher.group(15)));
            item.setForumId(Integer.parseInt(matcher.group(17)));
            item.setForumTitle(Utils.fromHtml(matcher.group(18)));
            item.setAuthorId(Integer.parseInt(matcher.group(19)));
            item.setAuthorUserNick(Utils.fromHtml(matcher.group(20)));
            item.setLastUserId(Integer.parseInt(matcher.group(21)));
            item.setLastUserNick(Utils.fromHtml(matcher.group(22)));
            item.setDate(matcher.group(23));
            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(response));
        Log.d("FORPDA_LOG", "parsing time " + ((System.currentTimeMillis() - time)));

        return data;
    }

    public boolean editSubscribeType(String type, int favId) throws Exception {
        String result = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=" + type + "&selectedtids=" + favId);
        return checkIsComplete(result);
    }

    public boolean editPinState(String type, int favId) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=fav")
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", type);
        String result = Api.getWebClient().request(builder.build());
        return checkIsComplete(result);
    }

    public boolean delete(int favId) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=fav")
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", "delete");
        String result = Api.getWebClient().request(builder.build());
        return checkIsComplete(result);
    }

    public boolean add(int id, String type) throws Exception {
        String result = Api.getWebClient().request(new ForPdaRequest.Builder().url("http://4pda.ru/forum/index.php?act=fav&type=add&t=" + id + "&track_type=" + type).build());
        return checkIsComplete(result);
    }

    private boolean checkIsComplete(String result) {
        //forpdateam.ru.forpda.utils.Utils.longLog("FAVORITE RESULT "+result);
        return checkPattern.matcher(result).find();
    }
}
