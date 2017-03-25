package forpdateam.ru.forpda.api.qms;

import android.text.Html;
import android.util.Log;

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

    private final static Pattern blackListPattern = Pattern.compile("<a class=\"list-group-item[^>]*?showuser=(\\d+)[^>]*?>[\\s\\S]*?<img class=\"avatar\" src=\"([^\"]*?)\" title=\"([\\s\\S]*?)\" alt[^>]*?>");
    private final static Pattern blackListMsgPattern = Pattern.compile("<div class=\"list-group-item msgbox ([^\"]*?)\"[^>]*?>[^<]*?<a[^>]*?>[^<]*?<\\/a>([\\s\\S]*?)<\\/div>");

    public ArrayList<QmsContact> getBlackList() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("xhr", "body");
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&settings=blacklist", headers);
        return parseBlackList(response);
    }

    private ArrayList<QmsContact> parseBlackList(String response) {
        ArrayList<QmsContact> list = new ArrayList<>();
        Matcher matcher = blackListPattern.matcher(response);
        while (matcher.find()) {
            QmsContact contact = new QmsContact();
            contact.setId(Integer.parseInt(matcher.group(1)));
            contact.setAvatar(matcher.group(2));
            contact.setNick(Utils.fromHtml(matcher.group(3)));
            list.add(contact);
        }
        return list;
    }

    public ArrayList<QmsContact> unBlockUsers(int[] ids) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("action", "delete-users");
        String strId;
        for (int id : ids) {
            strId = Integer.toString(id);
            headers.put("user-id[".concat(strId).concat("]"), strId);
        }
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1", headers);
        checkOperation(response);
        return parseBlackList(response);
    }

    public ArrayList<QmsContact> blockUser(String nick) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("action", "add-user");
        headers.put("username", nick);
        final String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1", headers);
        checkOperation(response);
        return parseBlackList(response);
    }

    private void checkOperation(String response) throws Exception {
        Matcher matcher = blackListMsgPattern.matcher(response);
        while (matcher.find()) {
            if (!matcher.group(1).contains("success")) {
                throw new Exception(Utils.fromHtml(matcher.group(2).trim()));
            }
        }
    }

    public ArrayList<QmsContact> getContactList() throws Exception {

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

    public QmsThemes getThemesList(final int id) throws Exception {
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

    public QmsChatModel getChat(final int userId, final int themeId) throws Exception {
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

    public String[] findUser(final String nick) throws Exception {
        String response = getInstance().get("http://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=" + nick + "&limit=150&timestamp=" + System.currentTimeMillis());
        return response.split(" |\n");
    }

    public QmsChatModel sendNewTheme(String nick, String title, String mess) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("username", nick);
        headers.put("title", title);
        headers.put("message", mess);
        String response = getInstance().post("http://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1", headers);
        return parseChat(response);
    }

    public QmsMessage sendMessage(int userId, int themeId, String text) throws Exception {
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

    public String deleteDialog(int mid) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "qms-xhr");
        headers.put("action", "del-member");
        headers.put("del-mid", Integer.toString(mid));
        return getInstance().post("http://4pda.ru/forum/index.php", headers);
    }
}
