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
import forpdateam.ru.forpda.utils.Utils;
import io.reactivex.Observable;

import static forpdateam.ru.forpda.client.Client.getInstance;

/**
 * Created by radiationx on 29.07.16.
 */
public class Qms {
    private final static Pattern contactsPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-member-id=\"([^\"]*?)\" (?=data-unread-count=\"([^\"]*?)\"|)[^>]*?>[^<]*?<div[^>]*?>[^<]*?<i[^>]*?></i>[^<]*?</div>[^<]*?<span[^>]*?>[^<]*?<div[^>]*?><img[^>]*?src=\"([^\"]*?)\" title=\"([^\"]*?)\"");
    private final static Pattern threadPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-thread-id=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div class=\"bage[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?(?:<strong>)?([^<]*?)\\((\\d+)(?: \\/ (\\d+))?\\)");
    private final static Pattern threadNickPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?<\\/b>");
    private final static Pattern chatInfoPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?:<\\/b>([\\s\\S]*?)<\\/span>[\\s\\S]*?<input[^>]*?name=\"mid\" value=\"(\\d+)\"[^>]*>[\\s\\S]*?<input[^>]*?name=\"t\" value=\"(\\d+)\"[^>]*>[\\s\\S]*?(?:[\\s\\S]*?class=\"avatar\"[^>]*?src=\"([^\"]*?)\")?");
    private final static Pattern chatPattern = Pattern.compile("group-item([^\"]*?)\" data-message-id=\"([^\"]*?)\"[^>]*?data-unread-status=\"([^\"]*?)\">[\\s\\S]*?<\\/b> ([^ <]*?) [\\s\\S]*?src=\"([^\"]*?)\"[\\s\\S]*?(<div[^>]*?msg-content[^>]*?>[\\s\\S]*?<\\/div>)([^<]*?<\\/div>[^<]*?<div (class=\"list|id=\"threa|class=\"date))?|<div class=\"text\">([^<]*?)<\\/div>");

    public Qms() {
    }

    private ArrayList<QmsContact> contactsList() throws Exception {

        ArrayList<QmsContact> list = new ArrayList<>();
        final String response = getInstance().get("http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist");
        final Matcher matcher = contactsPattern.matcher(response);
        QmsContact contact;
        String temp;
        while (matcher.find()) {
            contact = new QmsContact();
            contact.setId(Integer.parseInt(matcher.group(1)));
            temp = matcher.group(2);
            contact.setCount(temp == null || temp.isEmpty() ? 0 : Integer.parseInt(temp));
            contact.setAvatar(matcher.group(3));
            contact.setNick(Utils.fromHtml(matcher.group(4).trim()));
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
            qmsThemes.setNick(Utils.fromHtml(matcher.group(1)));
        }
        qmsThemes.setUserId(id);
        return qmsThemes;
    }

    private QmsChatModel chatItemsList(final int userId, final int themeId) throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("xhr", "body");
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&mid=" + userId + "&t=" + themeId, headers);
        return parseChat(response);
    }

    private QmsChatModel parseChat(String response) {
        QmsChatModel chat = new QmsChatModel();
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
            chat.setNick(Utils.fromHtml(matcher.group(1).trim()));
            chat.setTitle(Utils.fromHtml(matcher.group(2).trim()));
            chat.setUserId(Integer.parseInt(matcher.group(3)));
            chat.setThemeId(Integer.parseInt(matcher.group(4)));
            chat.setAvatarUrl(matcher.group(5));
        }
        return chat;
    }

    private String[] findUser(final String nick) throws Exception {
        String response = getInstance().get("http://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=" + nick + "&limit=150&timestamp=" + System.currentTimeMillis());
        return response.split(" |\n");
    }

    private QmsChatModel newTheme(String nick, String title, String mess) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("username", nick);
        headers.put("title", title);
        headers.put("message", mess);
        String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1", headers);
        return parseChat(response);
    }

    private QmsMessage _sendMessage(int userId, int themeId, String text) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "qms-xhr");
        headers.put("action", "send-message");
        headers.put("mid", Integer.toString(userId));
        headers.put("t", Integer.toString(themeId));
        headers.put("message", text);
        String response = getInstance().post("http://4pda.ru/forum/index.php", headers);
        Matcher matcher = chatPattern.matcher(response);
        QmsMessage item = new QmsMessage();
        if (matcher.find()) {
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
        } else {
            matcher = Pattern.compile("class=\"list-group-item[^\"]*?error\"[\\s\\S]*?<\\/a>([\\s\\S]*?)<\\/div>").matcher(response);
            if (matcher.find()) {
                throw new Exception(matcher.group(1).trim());
            }
        }
        return item;
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

    public Observable<QmsChatModel> sendNewTheme(String nick, String title, String mess) {
        return Observable.fromCallable(() -> newTheme(nick, title, mess));
    }

    public Observable<QmsMessage> sendMessage(int userId, int themeID, String text) {
        return Observable.fromCallable(() -> _sendMessage(userId, themeID, text));
    }

    public Observable<String> deleteDialog(int mid) {
        return Observable.fromCallable(() -> delDialog(mid));
    }
}
