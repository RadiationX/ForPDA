package forpdateam.ru.forpda.api.favorites;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;

/**
 * Created by radiationx on 22.09.16.
 */

public class Favorites {
    private final static Pattern mainPattern = Pattern.compile("<div data-item-fid=\"([^\"]*)\" data-item-track=\"([^\"]*)\" data-item-pin=\"([^\"]*)\">[\\s\\S]*?<br[^>]*>[^<]*?(<font color=\"([^\"]*)\">|)([^< ]*)(</font>|)[^<]*<a href=\"[^\"]*=(\\d*)[^\"]*?\"[^>]*?>(<strong>|)([^<]*)(</strong>|)</a>[\\s\\S]*?(<a href=\"[^\"]*=(\\d*)\">\\((\\d*?)\\)[^<]*?</a>|)</div>[\\s\\S]*?topic_desc\">([^<]*|)(<br[^>]*>|)[\\s\\S]*?showforum=([^\"]*?)\">([^<]*)</a><br[^>]*>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)</a>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)</a> ([^<]*?)<");
    private final static String url = "http://4pda.ru/forum/index.php?act=fav";
    public final static String[] SUB_TYPES = {"none", "delayed", "immediate", "daily", "weekly", "pinned"};
    public final static CharSequence[] SUB_NAMES = {"Не уведомлять", "Первый раз", "Каждый раз", "Каждый день", "Каждую неделю", "При изменении первого поста"};

    public static class SubTypes {
        public final static String NONE = "none";
        public final static String DELAYED = "delayed";
        public final static String IMMEDIATE = "immediate";
        public final static String DAILY = "daily";
        public final static String WEEKLY = "weekly";
        public final static String PINNED = "pinned";
    }

    private FavData _getFav() throws Exception {
        FavData favData = new FavData();
        final String response = Client.getInstance().get(url);
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
            item.setTopicTitle(matcher.group(10));
            if (!matcher.group(12).isEmpty()) {
                item.setStParam(Integer.parseInt(matcher.group(13)));
                item.setPages(Integer.parseInt(matcher.group(14)));
            }
            if (!matcher.group(15).isEmpty())
                item.setDesc(matcher.group(15));
            item.setForumId(Integer.parseInt(matcher.group(17)));
            item.setForumTitle(matcher.group(18));
            item.setAuthorId(Integer.parseInt(matcher.group(19)));
            item.setAuthorUserNick(matcher.group(20));
            item.setLastUserId(Integer.parseInt(matcher.group(21)));
            item.setLastUserNick(matcher.group(22));
            item.setDate(matcher.group(23));
            favData.addItem(item);
        }
        Log.d("kek", "parsing time " + ((System.currentTimeMillis() - time)));

        return favData;
    }

    private Void _changeSubType(String type, int id) throws Exception {
        Client.getInstance().get("http://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=" + type + "&selectedtids=" + id);
        return null;
    }

    private Void _setPinState(String type, int id) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("selectedtids", "" + id);
        headers.put("tact", type);
        Client.getInstance().post("http://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0", headers);
        return null;
    }

    private Void _delete(int id) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("selectedtids", "" + id);
        headers.put("tact", "delete");
        Client.getInstance().post("http://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0", headers);
        return null;
    }

    public Observable<FavData> get() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(_getFav());
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<Void> changeFav(int act, String type, int id) {
        return Observable.create(subscriber -> {
            try {
                switch (act) {
                    case 0:
                        subscriber.onNext(_changeSubType(type, id));
                        break;
                    case 1:
                        subscriber.onNext(_setPinState(type, id));
                        break;
                    case 2:
                        subscriber.onNext(_delete(id));
                        break;
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
