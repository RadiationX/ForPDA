package forpdateam.ru.forpda.api.favorites;

import android.net.Uri;

import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.ApiUtils;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.api.others.pagination.Pagination;

/**
 * Created by radiationx on 22.09.16.
 */

public class Favorites {
    private final static Pattern mainPattern = Pattern.compile("<div data-item-fid=\"([^\"]*)\" data-item-track=\"([^\"]*)\" data-item-pin=\"([^\"]*)\">[\\s\\S]*?(?:class=\"(?:modifier|forum_img_with_link)\"[^>]*?>(?:<font color=\"([^\"]*)\">)?([^< ]*)(?:<\\/font>)?<\\/(?:span|a)>)?[^<]*?<a href=\"[^\"]*=(\\d*)[^\"]*?\"[^>]*?>(<strong>)?([^<]*)(?:<\\/strong>)?<\\/a>(?:[^<]*?<a[^>]*?tpg\\(\\d+,(\\d+)\\)[^>]*?>[^<]*?<\\/a>[\\s\\S]*?)?(?:<\\/div><div class=\"topic_body\"><span class=\"topic_desc\">([^<]*|)(<br[^>]*>|)[\\s\\S]*?showforum=([^\"]*?)\">([^<]*)<\\/a><br[^>]*>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a> ([^<]*?)|[^<]*?<\\/div>[^<]*?<div class=\"board-forum-lastpost[\\s\\S]*?<div class=\"topic_body\">([^<]*?) <a href=\"[^\"]*?(\\d+)\"[^>]*?>([^<]*?))<(?:span class=\"forumdesc\"[^\"]*?>[^>]*?<br[^>]*?>[^<]*?<a href=\"[^\"]*?=(\\d+)\"[^>]*?>([\\s\\S]*?)<\\/a><\\/span><)?\\/div>[^<]*?<script[^>]*?>wr_fav_subscribe\\([^\"]*?\"([^\"]*?)\"\\)");
    private final static Pattern checkPattern = Pattern.compile("<div style=\"[^\"]*background:#dff0d8[^\"]*\">[\\s\\S]*<div id=\"navstrip");
    private final static Pattern pagesPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>");
    public final static int ACTION_EDIT_SUB_TYPE = 0;
    public final static int ACTION_EDIT_PIN_STATE = 1;
    public final static int ACTION_DELETE = 2;
    public final static int ACTION_ADD = 3;
    public final static int ACTION_ADD_FORUM = 4;
    public final static String[] SUB_TYPES = {"none", "delayed", "immediate", "daily", "weekly", "pinned"};

    private final static Comparator<FavItem> DESC_ORDER = (item1, item2) -> item1.getTopicTitle().compareToIgnoreCase(item2.getTopicTitle());
    private final static Comparator<FavItem> ASC_ORDER = (item1, item2) -> item2.getTopicTitle().compareToIgnoreCase(item1.getTopicTitle());

    public FavData getFavorites(int st, boolean all, Sorting sorting) throws Exception {
        FavData data = new FavData();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("4pda.ru");
        builder.appendPath("forum");
        builder.appendQueryParameter("act", "fav");
        builder.appendQueryParameter("type", "all");
        builder.appendQueryParameter("st", Integer.toString(st));
        builder.appendQueryParameter(Sorting.Key.HEADER, sorting.getKey());
        builder.appendQueryParameter(Sorting.Order.HEADER, sorting.getOrder());

        NetworkResponse response = Api.getWebClient().get(builder.build().toString());
        Matcher matcher = mainPattern.matcher(response.getBody());
        FavItem item;
        while (matcher.find()) {
            item = new FavItem();
            boolean isForum = matcher.group(19) != null;

            item.setFavId(Integer.parseInt(matcher.group(1)));
            item.setTrackType(matcher.group(2));
            item.setPin(matcher.group(3).equals("1"));
            if (matcher.group(4) != null) {
                item.setInfoColor(matcher.group(4));
            }
            String tmp;
            if (matcher.group(5) != null) {
                tmp = matcher.group(5);
                item.setNew(tmp.contains("+"));
                item.setPoll(tmp.contains("^"));
                item.setClosed(tmp.contains("Х"));
            }
            int iId = Integer.parseInt(matcher.group(6));
            if (isForum) {
                item.setForumId(iId);
            } else {
                item.setTopicId(iId);
            }
            item.setNew(matcher.group(7) != null);
            item.setTopicTitle(ApiUtils.fromHtml(matcher.group(8)));

            if (isForum) {
                item.setDate(matcher.group(19));
                item.setLastUserId(Integer.parseInt(matcher.group(20)));
                item.setLastUserNick(ApiUtils.fromHtml(matcher.group(21)));
                item.setForum(true);
            } else {
                if (matcher.group(9) != null) {
                    item.setStParam(Integer.parseInt(matcher.group(9)));
                    item.setPages((item.getStParam() / 20) + 1);
                }
                if (matcher.group(10) != null)
                    item.setDesc(ApiUtils.fromHtml(matcher.group(10)));

                item.setForumId(Integer.parseInt(matcher.group(12)));
                item.setForumTitle(ApiUtils.fromHtml(matcher.group(13)));
                item.setAuthorId(Integer.parseInt(matcher.group(14)));
                item.setAuthorUserNick(ApiUtils.fromHtml(matcher.group(15)));
                item.setLastUserId(Integer.parseInt(matcher.group(16)));
                item.setLastUserNick(ApiUtils.fromHtml(matcher.group(17)));
                item.setDate(matcher.group(18));

                tmp = matcher.group(22);
                if (tmp != null) {
                    item.setCuratorId(Integer.parseInt(matcher.group(22)));
                    item.setCuratorNick(ApiUtils.fromHtml(matcher.group(23)));
                }

                item.setSubType(matcher.group(24).trim().toLowerCase());
            }

            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(response.getBody()));
        data.setSorting(Sorting.parse(response.getBody()));
        if (all) {
            while (true) {
                if (data.getPagination().getCurrent() >= data.getPagination().getAll()) {
                    break;
                }
                FavData favData = getFavorites(data.getPagination().getPage(data.getPagination().getCurrent()), false, sorting);
                data.setPagination(favData.getPagination());
                if (favData.getItems().isEmpty()) {
                    break;
                }
                for (FavItem favItem : favData.getItems()) {
                    data.addItem(favItem);
                }
            }
            data.getPagination().setAll(1);

            if (data.getSorting().getKey().equals(Sorting.Key.TITLE)) {
                if (data.getSorting().getOrder().equals(Sorting.Order.DESC)) {
                    Collections.sort(data.getItems(), DESC_ORDER);
                } else if (data.getSorting().getOrder().equals(Sorting.Order.ASC)) {
                    Collections.sort(data.getItems(), ASC_ORDER);
                }
            }
        }

        return data;
    }

    public boolean editSubscribeType(String type, int favId) throws Exception {
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=" + type + "&selectedtids=" + favId);
        return checkIsComplete(response.getBody());
    }

    public boolean editPinState(String type, int favId) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=fav")
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", type);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return checkIsComplete(response.getBody());
    }

    public boolean delete(int favId) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=fav")
                .xhrHeader()
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", "delete");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return checkIsComplete(response.getBody());
    }

    public boolean add(int id, int action, String type) throws Exception {
        String url = "https://4pda.ru/forum/index.php?act=fav&type=add&track_type=" + type;
        if (action == ACTION_ADD_FORUM) {
            url += "&f=";
        } else if (action == ACTION_ADD) {
            url += "&t=";
        }
        url += id;
        NetworkResponse response = Api.getWebClient().request(new NetworkRequest.Builder().url(url).build());
        return checkIsComplete(response.getBody());
    }

    private boolean checkIsComplete(String result) {
        return checkPattern.matcher(result).find();
    }
}
