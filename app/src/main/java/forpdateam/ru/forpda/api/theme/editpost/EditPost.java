package forpdateam.ru.forpda.api.theme.editpost;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.RequestFile;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPost {
    private final static Pattern postPattern = Pattern.compile("<div[^>]*?>[^<]*?<textarea[^>]*>([\\s\\S]*?)<\\/textarea>[^<]*?<\\/div>[\\s\\S]*?<input[^>]*?name=\"post_edit_reason\" value=\"([\\s\\S]*?)\" \\/>");
    private final static Pattern loadedAttachments = Pattern.compile("add_current_item\\([^'\"]*?['\"](\\d+)['\"],[^'\"]*?['\"]([^'\"]*\\.(\\w*))['\"],[^'\"]*?['\"]([^'\"]*?)['\"],[^'\"]*?['\"][^'\"]*\\/(\\w*)\\.[^\"']*?['\"]");
    private final static Pattern statusInfo = Pattern.compile("can_upload = parseInt\\([\"'](\\d+)'\\)[\\s\\S]*?status_msg_files = .([\\s\\S]*?).;[\\s\\S]*?status_msg = .([\\s\\S]*?).;[\\s\\S]*?status_is_error = ([\\s\\S]*?);");



    public EditPostForm loadForm(int postId) throws Exception {
        Log.d("FORPDA_LOG", "START LOAD FORM");
        EditPostForm form = new EditPostForm();
        String url = "http://4pda.ru/forum/index.php?s=&act=post&do=post-edit-show&p=".concat(Integer.toString(postId));
        //url = url.concat("&t=").concat(Integer.toString(topicId)).concat("&f=").concat(Integer.toString(forumId));

        String response = Client.getInstance().get(url);
        Matcher matcher = postPattern.matcher(response);
        if (matcher.find()) {
            Log.d("FORPDA_LOG", "MESSAGE " + matcher.group(1));
            form.setMessage(matcher.group(1));
            Log.d("FORPDA_LOG", "REASON " + matcher.group(2));
            form.setEditReason(matcher.group(2));
        }

        response = Client.getInstance().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=".concat(Integer.toString(postId)));
        matcher = loadedAttachments.matcher(response);
        while (matcher.find())
            form.addAttachment(fillAttachment(new AttachmentItem(), matcher));
        Log.d("FORPDA_LOG", "ATTACHES " + form.getAttachments().size());
        return form;
    }

    public List<AttachmentItem> uploadFiles(int postId, List<RequestFile> files) throws Exception {
        String url = "http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=".concat(postId == 0 ? "" : Integer.toString(postId));
        List<AttachmentItem> items = new ArrayList<>();
        AttachmentItem item;
        String response;
        Matcher matcher;
        for (RequestFile file : files) {
            file.setRequestName("FILE_UPLOAD[]");
            item = new AttachmentItem();

            response = Client.getInstance().post(url, null, file);
            matcher = loadedAttachments.matcher(response);
            if (matcher.find())
                item = fillAttachment(item, matcher);

            matcher = statusInfo.matcher(response);
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
            items.add(item);
        }
        return items;
    }

    public List<AttachmentItem> deleteFiles(int postId, List<AttachmentItem> items) throws Exception {
        String response;
        Matcher matcher;
        for (AttachmentItem item : items) {
            response = Client.getInstance().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_remove&attach_rel_id=".concat(postId == 0 ? "" : Integer.toString(postId)).concat("&attach_id=").concat(Integer.toString(item.getId())));
            matcher = statusInfo.matcher(response);
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
        }
        return items;
    }

    private AttachmentItem fillAttachment(AttachmentItem item, Matcher matcher) {
        item.setId(Integer.parseInt(matcher.group(1)));
        item.setName(matcher.group(2));
        item.setFormat(matcher.group(3));
        item.setWeight(matcher.group(4));
        if (item.getTypeFile() == AttachmentItem.TYPE_IMAGE) {
            item.setImageUrl("http://cs5-2.4pda.to/".concat(Integer.toString(item.getId())).concat(".").concat(item.getFormat()));
        }
        item.setLoadState(AttachmentItem.STATE_LOADED);
        return item;
    }

    private AttachmentItem fillAttachmentStatus(AttachmentItem item, Matcher matcher) {
        item.setStatus(getStatus(matcher.group(3)));
        item.setError(!matcher.group(4).equals("0"));
        return item;
    }

    private int getStatus(String status) {
        switch (status) {
            case "attach_removed":
                return AttachmentItem.STATUS_REMOVED;
            case "upload_no_file":
                return AttachmentItem.STATUS_NO_FILE;
            case "upload_ok":
                return AttachmentItem.STATUS_UPLOADED;
            case "ready":
                return AttachmentItem.STATUS_READY;
        }
        return AttachmentItem.STATUS_UNKNOWN;
    }



    public ThemePage sendPost(EditPostForm form) throws Exception {
        String url = "http://4pda.ru/forum/index.php";
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "Post");
        Log.d("FORPDA_LOG", "FORM TYPE " + form.getType());
        headers.put("CODE", form.getType() == EditPostForm.TYPE_NEW_POST ? "03" : "9");
        if (form.getPostId() != 0)
            headers.put("p", "" + form.getPostId());
        headers.put("f", "" + form.getForumId());
        headers.put("t", "" + form.getTopicId());


        headers.put("auth_key", Client.getAuthKey());
        headers.put("Post", form.getMessage());
        headers.put("enablesig", "yes");
        headers.put("enableemo", "yes");


        headers.put("st", ""+form.getSt());
        headers.put("removeattachid", "0");
        headers.put("MAX_FILE_SIZE", "0");
        headers.put("parent_id", "0");
        headers.put("ed-0_wysiwyg_used", "0");
        headers.put("editor_ids[]", "ed-0");
        headers.put("iconid", "0");
        headers.put("_upload_single_file", "1");
        //headers.put("file-list", addedFileList);
        if (form.getType() == EditPostForm.TYPE_EDIT_POST)
            headers.put("post_edit_reason", form.getEditReason());


        StringBuilder ids = new StringBuilder();
        if (form.getAttachments() != null && form.getAttachments().size() > 0) {
            for (int i = 0; i < form.getAttachments().size(); i++) {
                int id = form.getAttachments().get(i).getId();
                ids.append(id);
                if (i < form.getAttachments().size() - 1) {
                    ids.append(",");
                }
            }
        }
        headers.put("file-list", ids.toString());
        return Api.Theme().parsePage(url, Client.getInstance().post(url, headers), true);
    }
}
