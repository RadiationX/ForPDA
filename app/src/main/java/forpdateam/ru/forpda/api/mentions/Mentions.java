package forpdateam.ru.forpda.api.mentions;

import android.util.Log;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.mentions.models.MentionItem;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.utils.ourparser.Html;
import io.reactivex.Observable;

/**
 * Created by radiationx on 21.01.17.
 */

public class Mentions {
    private final static Pattern mentionsPattern = Pattern.compile("<div class=\"topic_title_post ([^\"]*?)\"[^>]*?>([^:]*?):[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>(?:([^<]*?)(?:, ([^<]*?)|))<\\/a>[\\s\\S]*?post_date[^\"]*?\"[^>]*?>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?showuser[^>]*>([\\s\\S]*?)<");

    private final static Pattern pagesPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>");

    public Observable<MentionsData> getMentions(int st) {
        return Observable.fromCallable(() -> _getMentions(st));
    }

    private MentionsData _getMentions(int st) throws Exception {
        MentionsData data = new MentionsData();
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=mentions&st=".concat(Integer.toString(st)));
        Matcher matcher = mentionsPattern.matcher(response);
        while (matcher.find()) {
            MentionItem item = new MentionItem();
            item.setState(matcher.group(1).equals("read") ? MentionItem.STATE_READ : MentionItem.STATE_UNREAD);
            item.setType(matcher.group(2).equalsIgnoreCase("Форум") ? MentionItem.TYPE_FORUM : MentionItem.TYPE_NEWS);
            item.setLink(matcher.group(3));
            item.setTitle(matcher.group(4));
            item.setDesc(matcher.group(5));
            item.setDate(matcher.group(6));
            item.setNick(matcher.group(7));
            data.addItem(item);
        }
        matcher = pagesPattern.matcher(response);
        if (matcher.find()) {
            data.setAllPagesCount(Integer.parseInt(matcher.group(1)) + 1);
            data.setItemsPerPage(Integer.parseInt(matcher.group(2)));
            data.setCurrentPage(Integer.parseInt(matcher.group(3)));
        }
        return data;
    }
}
