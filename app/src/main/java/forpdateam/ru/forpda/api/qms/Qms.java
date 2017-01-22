package forpdateam.ru.forpda.api.qms;

import android.text.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import io.reactivex.Observable;

import static forpdateam.ru.forpda.client.Client.getInstance;

/**
 * Created by radiationx on 29.07.16.
 */
public class Qms {
    private final static Pattern contactsPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-member-id=\"([^\"]*?)\" (?=data-unread-count=\"([^\"]*?)\"|)[^>]*?>[^<]*?<div[^>]*?>[^<]*?<i[^>]*?></i>[^<]*?</div>[^<]*?<span[^>]*?>[^<]*?<div[^>]*?><img[^>]*?src=\"([^\"]*?)\" title=\"([^\"]*?)\"");
    private final static Pattern threadPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-thread-id=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div class=\"bage[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?(?:<strong>)?([^<]*?)\\((\\d+)(?: \\/ (\\d+))?\\)");
    private final static Pattern threadNickPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?<\\/b>");
    private final static Pattern chatInfoPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?:<\\/b>([\\s\\S]*?)<\\/span>(?:[\\s\\S]*?class=\"avatar\"[^>]*?src=\"([^\"]*?)\")?");
    private final static Pattern chatPattern = Pattern.compile("group-item([^\"]*?)\" data-message-id=\"([^\"]*?)\"[^>]*?data-unread-status=\"([^\"]*?)\">[\\s\\S]*?</b> ([^ <]*?) [\\s\\S]*?src=\"([^\"]*?)\"[\\s\\S]*?(<div[^>]*?msg-content[^>]*?>[\\s\\S]*?</div>)([^<]*?</div>[^<]*?<div (class=\"list|id=\"threa|class=\"date))|<div class=\"text\">([^<]*?)</div>");

    public Qms() {
    }

    private ArrayList<QmsContact> contactsList() throws Exception {

        ArrayList<QmsContact> list = new ArrayList<>();
        final String response = getInstance().get("http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist");
        final Matcher matcher = contactsPattern.matcher(response);
        QmsContact contact;
        while (matcher.find()) {
            contact = new QmsContact();
            contact.setId(Integer.parseInt(matcher.group(1)));
            String temp = matcher.group(2);
            contact.setCount(temp == null || temp.isEmpty() ? 0 : Integer.parseInt(temp));
            temp = matcher.group(3);
            if (temp.substring(0, 2).equals("//"))
                temp = "http:".concat(temp);
            contact.setAvatar(temp);
            contact.setNick(Html.fromHtml(matcher.group(4).trim()).toString());
            list.add(contact);
        }

        return list;
    }

    private QmsThemes themesList(final int id) throws Exception {
        QmsThemes qmsThemes = new QmsThemes();
        Map<String, String> headers = new HashMap<>();
        headers.put("xhr", "body");
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&mid=" + id, headers);
        Matcher matcher = threadPattern.matcher(response);
        QmsTheme thread;
        while (matcher.find()) {
            thread = new QmsTheme();
            thread.setId(Integer.parseInt(matcher.group(1)));
            thread.setDate(matcher.group(2));
            thread.setName(Html.fromHtml(matcher.group(3).trim()).toString());
            thread.setCountMessages(Integer.parseInt(matcher.group(4)));
            String countNew = matcher.group(5);
            thread.setCountNew(countNew == null || countNew.isEmpty() ? 0 : Integer.parseInt(countNew));
            qmsThemes.addTheme(thread);
        }
        matcher = threadNickPattern.matcher(response);
        if (matcher.find()) {
            qmsThemes.setNick(matcher.group(1));
        }
        qmsThemes.setUserId(id);
        return qmsThemes;
    }

    private QmsChatModel chatItemsList(final int userId, final int themeId) throws Exception {
        QmsChatModel chat = new QmsChatModel();
        Map<String, String> headers = new HashMap<>();
        headers.put("xhr", "body");
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&mid=" + userId + "&t=" + themeId, headers);
        Matcher matcher = chatPattern.matcher(response);
        QmsMessage item;
        while (matcher.find()) {
            item = new QmsMessage();
            if (matcher.group(1) == null && matcher.group(9) != null) {
                item.setIsDate(true);
                item.setDate(matcher.group(9).trim());
            } else {
                item.setWhoseMessage(!matcher.group(1).isEmpty());
                item.setId(matcher.group(2));
                item.setReadStatus(matcher.group(3));
                item.setTime(matcher.group(4));
                item.setAvatar(matcher.group(5));
                item.setContent(matcher.group(6).trim());
            }
            chat.addChatItem(item);
        }
        matcher = chatInfoPattern.matcher(response);
        if (matcher.find()) {
            chat.setNick(Html.fromHtml(matcher.group(1).trim()).toString());
            chat.setTitle(Html.fromHtml(matcher.group(2).trim()).toString());
            chat.setAvatarUrl(matcher.group(3));
        }
        return chat;
    }

    private String[] findUser(final String nick) throws Exception {
        String response = getInstance().get("http://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=" + nick + "&limit=150&timestamp=" + System.currentTimeMillis());
        return response.split(" |\n");
    }

    private String newTheme(String nick, String title, String mess) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("username", nick);
        headers.put("title", title);
        headers.put("message", mess);
        String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1", headers);
        /*Pattern errorPattern = Pattern.compile("<div class=\"list-group-item msgbox error\">([^<]*<a[^>]*?>[^<]*?<[^>]*a>|)([\\s\\S]*?)</div>");
        Matcher matcher = errorPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(2);
        } else {
            return null;
        }*/
        return response;
    }

    private String delDialog(int mid) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "qms-xhr");
        headers.put("action", "del-member");
        headers.put("del-mid", Integer.toString(mid));
        return getInstance().post("http://4pda.ru/forum/index.php", headers);
    }

    public Observable<ArrayList<QmsContact>> getContactList() {
        return Observable.fromCallable(this::contactsList);
    }

    public Observable<QmsThemes> getThemesList(final int id) {
        return Observable.fromCallable(() -> themesList(id));
    }

    public Observable<QmsChatModel> getChat(final int userId, final int themeId) {
        return Observable.fromCallable(() -> chatItemsList(userId, themeId));
    }

    public Observable<String[]> search(final String nick) {
        return Observable.fromCallable(() -> findUser(nick));
    }

    public Observable<String> sendNewTheme(String nick, String title, String mess) {
        return Observable.fromCallable(() -> newTheme(nick, title, mess));
    }

    public Observable<String> deleteDialog(int mid) {
        return Observable.fromCallable(() -> delDialog(mid));
    }
}
