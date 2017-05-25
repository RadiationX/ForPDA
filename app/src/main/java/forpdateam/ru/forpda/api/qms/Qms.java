package forpdateam.ru.forpda.api.qms;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.client.ForPdaRequest;


/**
 * Created by radiationx on 29.07.16.
 */
public class Qms {
    private final static Pattern contactsPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-member-id=\"([^\"]*?)\" (?=data-unread-count=\"([^\"]*?)\"|)[^>]*?>[^<]*?<div[^>]*?>[^<]*?<i[^>]*?></i>[^<]*?</div>[^<]*?<span[^>]*?>[^<]*?<div[^>]*?><img[^>]*?src=\"([^\"]*?)\" title=\"([^\"]*?)\"");
    private final static Pattern threadPattern = Pattern.compile("<a class=\"list-group-item[^>]*?data-thread-id=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div class=\"bage[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?(?:<strong>)?([^<]*?)\\((\\d+)(?: \\/ (\\d+))?\\)");
    private final static Pattern threadNickPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?<\\/b>");
    private final static Pattern chatInfoPattern = Pattern.compile("<div class=\"nav\">[\\s\\S]*?<b>(?:<a[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?:<\\/b>([\\s\\S]*?)<\\/span>[\\s\\S]*?<input[^>]*?name=\"mid\" value=\"(\\d+)\"[^>]*>[\\s\\S]*?<input[^>]*?name=\"t\" value=\"(\\d+)\"[^>]*>[\\s\\S]*?(?:[\\s\\S]*?list-group-item(?! our-message)[^\"]*?\"[\\s\\S]*?class=\"avatar\"[^>]*?src=\"([^\"]*?)\")?");
    private final static Pattern chatPatternOld = Pattern.compile("group-item([^\"]*?)\" data-message-id=\"([^\"]*?)\"[^>]*?data-unread-status=\"([^\"]*?)\">[\\s\\S]*?<\\/b> ([^ <]*?) [\\s\\S]*?src=\"([^\"]*?)\"[\\s\\S]*?<div[^>]*?msg-content[^>]*?>([\\s\\S]*?)<\\/div>([^<]*?<\\/div>[^<]*?<div (class=\"list|id=\"threa|class=\"date))?|<div class=\"text\">([^<]*?)<\\/div>");
    private final static Pattern chatPattern = Pattern.compile("group-item([^\"]*?)\" data-message-id=\"([^\"]*?)\"[^>]*?data-unread-status=\"([^\"]*?)\">[\\s\\S]*?<\\/b> ([^ <]*?) [\\s\\S]*?src=\"([^\"]*?)\"[\\s\\S]*?<div[^>]*?msg-content[^>]*?>([\\s\\S]*?)<\\/div>\\n[^<]*?<\\/div>[^<]*?(?:\\*\\/--><\\/div>|<div (?:class=\"(?=date|list-group-item)|id=\"thread-inside-bottom))|<div class=\"text\">([^<]*?)<\\/div>");

    private final static Pattern blackListPattern = Pattern.compile("<a class=\"list-group-item[^>]*?showuser=(\\d+)[^>]*?>[\\s\\S]*?<img class=\"avatar\" src=\"([^\"]*?)\" title=\"([\\s\\S]*?)\" alt[^>]*?>");
    private final static Pattern blackListMsgPattern = Pattern.compile("<div class=\"list-group-item msgbox ([^\"]*?)\"[^>]*?>[^<]*?<a[^>]*?>[^<]*?<\\/a>([\\s\\S]*?)<\\/div>");

    private final static Pattern findUserPattern = Pattern.compile("\\[(\\d+),\"([\\s\\S]*?)\",\\d+,\"<span[^>]*?background:url\\(([^\\)]*?)\\)");

    public ArrayList<QmsContact> getBlackList() throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist")
                .formHeader("xhr", "body");
        final String response = Api.getWebClient().request(builder.build());
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
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "delete-users");
        String strId;
        for (int id : ids) {
            strId = Integer.toString(id);
            builder.formHeader("user-id[".concat(strId).concat("]"), strId);
        }
        final String response = Api.getWebClient().request(builder.build());
        checkOperation(response);
        return parseBlackList(response);
    }

    public ArrayList<QmsContact> blockUser(String nick) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "add-user")
                .formHeader("username", nick);
        final String response = Api.getWebClient().request(builder.build());
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
        final String response = Api.getWebClient().request(new ForPdaRequest.Builder().url("http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist").build());
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
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&mid=" + id)
                .formHeader("xhr", "body");
        final String response = Api.getWebClient().request(builder.build());
        Matcher matcher = threadPattern.matcher(response);
        QmsTheme thread;
        while (matcher.find()) {
            thread = new QmsTheme();
            thread.setId(Integer.parseInt(matcher.group(1)));
            thread.setDate(matcher.group(2));
            thread.setName(forpdateam.ru.forpda.utils.ourparser.Html.fromHtml(matcher.group(3).trim()).toString());
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
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&mid=" + userId + "&t=" + themeId)
                .formHeader("xhr", "body");
        final String response = Api.getWebClient().request(builder.build());
        return parseChat(response);
    }

    private QmsChatModel parseChat(String response) {
        QmsChatModel chat = new QmsChatModel();
        Matcher matcher = chatPattern.matcher(response);
        QmsMessage item;
        while (matcher.find()) {
            item = new QmsMessage();
            if (matcher.group(1) == null && matcher.group(7) != null) {
                item.setIsDate(true);
                item.setDate(matcher.group(7).trim());
            } else {
                item.setMyMessage(!matcher.group(1).isEmpty());
                item.setId(Integer.parseInt(matcher.group(2)));
                if (item.isMyMessage()) {
                    item.setReadStatus(!matcher.group(3).equals("1"));
                } else {
                    item.setReadStatus(true);
                }
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

    public List<String> findUser(final String nick) throws Exception {
        String response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=" + nick + "&limit=150&timestamp=" + System.currentTimeMillis());
        List<String> list = new ArrayList<>();
        Matcher m = findUserPattern.matcher(response);
        while (m.find()) {
            list.add(Utils.htmlEncode(m.group(2)));
        }
        return list;
    }

    public QmsChatModel sendNewTheme(String nick, String title, String mess) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1")
                .formHeader("username", nick)
                .formHeader("title", title)
                .formHeader("message", mess);
        String response = Api.getWebClient().request(builder.build());
        return parseChat(response);
    }

    public QmsMessage sendMessage(int userId, int themeId, String text) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "send-message")
                .formHeader("message", text)
                .formHeader("mid", Integer.toString(userId))
                .formHeader("t", Integer.toString(themeId));
        String response = Api.getWebClient().request(builder.build());
        Log.e("FORPDA_LOG", "SEND MESSAGE RESPONSE " + response);
        Matcher matcher = chatPattern.matcher(response);
        QmsMessage item = new QmsMessage();
        if (matcher.find()) {
            if (matcher.group(1) == null && matcher.group(7) != null) {
                item.setIsDate(true);
                item.setDate(matcher.group(7).trim());
            } else {
                item.setMyMessage(!matcher.group(1).isEmpty());
                item.setId(Integer.parseInt(matcher.group(2)));
                if (item.isMyMessage()) {
                    item.setReadStatus(!matcher.group(3).equals("1"));
                } else {
                    item.setReadStatus(true);
                }
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
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "del-member")
                .formHeader("del-mid", Integer.toString(mid));
        return Api.getWebClient().request(builder.build());
    }

    public List<AttachmentItem> uploadFiles(List<RequestFile> files) throws Exception {
        String url = "http://savepic.ru/index.php";
        List<AttachmentItem> items = new ArrayList<>();
        AttachmentItem item;
        String response;
        Matcher matcher = null;

        HashMap<String, String> headers = new HashMap<>();
        //headers.put("file","");
        headers.put("note", "");
        //decor, techno, strong, italic, neutral
        headers.put("font1", "decor");
        //14, 16, 18, 20, 22, 24, 26, 28
        headers.put("font2", "20");
        //h, v
        headers.put("orient", "h");
        //1, 2, 3, 4, x - коэфф уменьшения
        headers.put("size1", "1");
        //при size1=x, разрешение
        headers.put("size2", "1024x768");
        //90, 270, 180
        headers.put("rotate", "00");
        //vr, hr, 0
        headers.put("flip", "0");
        //200x150, 300x225, 400x300
        headers.put("mini", "300x225");
        //annot - отображать подпись в изображении, gallery - разместить в галлерее
        headers.put("opt1[]", "");
        //gray, negat
        headers.put("opt2[]", "");
        //zoom - надпись увеличить
        headers.put("opt3[]", "zoom");
        headers.put("email", "");
        for (RequestFile file : files) {
            item = new AttachmentItem();
            file.setRequestName("file");
            ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                    .url(url)
                    .formHeaders(headers)
                    .file(file);
            response = Api.getWebClient().request(builder.build());


            if (matcher == null)
                matcher = loadedAttachment.matcher(response);
            else
                matcher = matcher.reset(response);
            if (matcher.find()) {
                item.setName(file.getFileName());
                item.setImageUrl("http://savepic.ru/".concat(matcher.group(1)));
                item.setId(Integer.parseInt(matcher.group(2)));
                item.setFormat(matcher.group(3));
                item.setWeight(matcher.group(4));
                item.setTypeFile(AttachmentItem.TYPE_IMAGE);
                item.setLoadState(AttachmentItem.STATE_LOADED);
                Log.e("FORPDA_LOG", item.getName() + " : " + item.getId() + " : " + item.getFormat() + " : " + item.getWeight() + " : " + item.getImageUrl());
            }
            items.add(item);
        }

        return items;
    }

    private final static Pattern loadedAttachment = Pattern.compile("<p class=\"[^\"]*?img[^\"]*?\"[^>]*?><a[^>]*?><img[^>]*?src=\"([^\"]*?(\\d+)m?\\.([^\"]*?))\"[^>]*?>[\\s\\S]*?<p class=\"[^\"]*?b-sign[^\"]*?\"[^>]*?>[\\s\\S]*?<strong>([^,<]*?),[^<]*?<\\/strong>\\.<\\/p>");
}
