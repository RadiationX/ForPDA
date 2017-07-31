package forpdateam.ru.forpda.api.qms;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;


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
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist")
                .formHeader("xhr", "body");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return parseBlackList(response.getBody());
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
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "delete-users");
        String strId;
        for (int id : ids) {
            strId = Integer.toString(id);
            builder.formHeader("user-id[".concat(strId).concat("]"), strId);
        }
        NetworkResponse response = Api.getWebClient().request(builder.build());
        checkOperation(response.getBody());
        return parseBlackList(response.getBody());
    }

    public ArrayList<QmsContact> blockUser(String nick) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
                .formHeader("action", "add-user")
                .formHeader("username", nick);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        checkOperation(response.getBody());
        return parseBlackList(response.getBody());
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
        NetworkResponse response = Api.getWebClient().request(new NetworkRequest.Builder().url("http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist").build());
        final Matcher matcher = contactsPattern.matcher(response.getBody());
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

    public QmsThemes getThemesList(int id) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&mid=" + id)
                .formHeader("xhr", "body");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return parseThemes(response.getBody(), id);
    }

    public QmsThemes deleteTheme(int id, int themeId) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&mid=" + id + "&xhr=body&do=1")
                .formHeader("xhr", "body")
                .formHeader("action", "delete-threads")
                .formHeader("thread-id[" + themeId + "]", "" + themeId);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return parseThemes(response.getBody(), id);
    }

    private QmsThemes parseThemes(String response, int id) {
        QmsThemes qmsThemes = new QmsThemes();
        Matcher matcher = threadPattern.matcher(response);
        while (matcher.find()) {
            QmsTheme thread = new QmsTheme();
            thread.setId(Integer.parseInt(matcher.group(1)));
            thread.setDate(matcher.group(2));
            thread.setName(Utils.fromHtml(matcher.group(3).trim()));
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
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&mid=" + userId + "&t=" + themeId)
                .formHeader("xhr", "body");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return parseChat(response.getBody());
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
            chat.addMessage(item);
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

    public List<ForumUser> findUser(final String nick) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=qms-xhr&action=autocomplete-username&q=" + nick /*+ "&limit=150&timestamp=" + System.currentTimeMillis()*/);
        List<ForumUser> list = new ArrayList<>();
        Matcher m = findUserPattern.matcher(response.getBody());
        while (m.find()) {
            ForumUser user = new ForumUser();
            user.setId(Integer.parseInt(m.group(1)));
            user.setNick(Utils.htmlEncode(m.group(2)));
            String avatar = m.group(3);
            if (avatar.substring(0, 2).equals("//")) {
                avatar = "http:".concat(avatar);
            } else if (avatar.substring(0, 1).equals("/")) {
                avatar = "http://4pda.ru".concat(avatar);
            }
            user.setAvatar(avatar);
            list.add(user);
        }
        return list;
    }

    public QmsChatModel sendNewTheme(String nick, String title, String mess) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms&action=create-thread&xhr=body&do=1")
                .formHeader("username", nick)
                .formHeader("title", title)
                .formHeader("message", mess);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return parseChat(response.getBody());
    }

    public ArrayList<QmsMessage> sendMessage(int userId, int themeId, String text) throws Exception {
        ArrayList<QmsMessage> messages = new ArrayList<>();
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "send-message")
                .formHeader("message", text)
                .formHeader("mid", Integer.toString(userId))
                .formHeader("t", Integer.toString(themeId));
        NetworkResponse response = Api.getWebClient().request(builder.build());
        Log.e("FORPDA_LOG", "SEND MESSAGE RESPONSE " + response.getBody());
        Matcher matcher = chatPattern.matcher(response.getBody());
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
            messages.add(item);
        } else {
            matcher = Pattern.compile("class=\"list-group-item[^\"]*?error\"[\\s\\S]*?<\\/a>([\\s\\S]*?)<\\/div>").matcher(response.getBody());
            if (matcher.find()) {
                throw new Exception(matcher.group(1).trim());
            }
        }
        return messages;
    }

    Pattern messageInfoPattern = Pattern.compile("\"id_to\":(\\d+)");

    public ArrayList<QmsMessage> getMessagesFromWs(int themeId, int messageId, int afterMessageId) throws Exception {
        ArrayList<QmsMessage> messages = new ArrayList<>();
        NetworkRequest.Builder messInfoBuilder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms-xhr&")
                .formHeader("action", "message-info")
                .formHeader("t", Integer.toString(themeId))
                .formHeader("msg-id", Integer.toString(messageId));
        NetworkResponse messInfoResponse = Api.getWebClient().request(messInfoBuilder.build());

        Matcher matcher = messageInfoPattern.matcher(messInfoResponse.getBody());
        int idTo = 0;
        if (matcher.find()) {
            idTo = Integer.parseInt(matcher.group(1));
        }

        NetworkRequest.Builder threadMessagesBuilder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=qms-xhr&")
                .xhrHeader()
                .formHeader("action", "get-thread-messages")
                .formHeader("mid", Integer.toString(idTo))
                .formHeader("t", Integer.toString(themeId))
                .formHeader("after-message", Integer.toString(afterMessageId));
        NetworkResponse threadMessagesResponse = Api.getWebClient().request(threadMessagesBuilder.build());

        matcher = chatPattern.matcher(threadMessagesResponse.getBody());
        while (matcher.find()) {
            QmsMessage item = new QmsMessage();
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
            messages.add(item);
        }
        return messages;
    }

    public String deleteDialog(int mid) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php")
                .formHeader("act", "qms-xhr")
                .formHeader("action", "del-member")
                .formHeader("del-mid", Integer.toString(mid));
        return Api.getWebClient().request(builder.build()).getBody();
    }

    public List<AttachmentItem> uploadFiles(List<RequestFile> files, List<AttachmentItem> pending) throws Exception {
        String url = "http://savepic.ru/index.php";

        NetworkResponse response;
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
        for (int i = 0; i < files.size(); i++) {
            RequestFile file = files.get(i);
            AttachmentItem item = pending.get(i);

            file.setRequestName("file");
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .url(url)
                    .formHeaders(headers)
                    .file(file);
            response = Api.getWebClient().request(builder.build(), item.getItemProgressListener());


            if (matcher == null)
                matcher = loadedAttachment.matcher(response.getBody());
            else
                matcher = matcher.reset(response.getBody());
            if (matcher.find()) {
                item.setName(file.getFileName());
                item.setImageUrl("http://savepic.ru/".concat(matcher.group(1)));
                item.setId(Integer.parseInt(matcher.group(2)));
                item.setExtension(matcher.group(3));
                item.setWeight(matcher.group(4));
                item.setTypeFile(AttachmentItem.TYPE_IMAGE);
                item.setLoadState(AttachmentItem.STATE_LOADED);
                Log.e("FORPDA_LOG", item.getName() + " : " + item.getId() + " : " + item.getExtension() + " : " + item.getWeight() + " : " + item.getImageUrl());
            }
        }

        return pending;
    }

    private final static Pattern loadedAttachment = Pattern.compile("<p class=\"[^\"]*?img[^\"]*?\"[^>]*?><a[^>]*?><img[^>]*?src=\"([^\"]*?(\\d+)m?\\.([^\"]*?))\"[^>]*?>[\\s\\S]*?<p class=\"[^\"]*?b-sign[^\"]*?\"[^>]*?>[\\s\\S]*?<strong>([^,<]*?),[^<]*?<\\/strong>\\.<\\/p>");
}
