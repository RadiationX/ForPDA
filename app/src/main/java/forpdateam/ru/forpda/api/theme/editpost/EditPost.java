package forpdateam.ru.forpda.api.theme.editpost;

import android.util.Log;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.RequestFile;
import forpdateam.ru.forpda.messagepanel.attachments.AttachmentAdapter;
import io.reactivex.Observable;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPost {
    private final static Pattern loadedAttachments = Pattern.compile("add_current_item\\([^'\"]*?['\"](\\d+)['\"],[^'\"]*?['\"]([^'\"]*\\.(\\w*))['\"],[^'\"]*?['\"]([^'\"]*?)['\"],[^'\"]*?['\"][^'\"]*\\/(\\w*)\\.[^\"']*?['\"]");
    private final static Pattern statusInfo = Pattern.compile("can_upload = parseInt\\([\"'](\\d+)'\\)[\\s\\S]*?status_msg_files = .([\\s\\S]*?).;[\\s\\S]*?status_msg = .([\\s\\S]*?).;[\\s\\S]*?status_is_error = ([\\s\\S]*?);");

    public Observable<EditPostForm> loadForm(int id) {
        return Observable.fromCallable(() -> _loadForm(id));
    }

    public Observable<EditPostForm> uploadFile(final int id, String name, String scheme, InputStream file) {
        return Observable.fromCallable(() -> _uploadFile(id, name, scheme, file));
    }

    private EditPostForm _loadForm(int postId) throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=".concat(Integer.toString(postId)));

        return parseForm(response);
    }

    private EditPostForm _uploadFile(int postId, String name, String scheme, InputStream file) throws Exception {
        String url = "http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=".concat(Integer.toString(postId));
        String response = Client.getInstance().post(url, null, new RequestFile("FILE_UPLOAD[]", name, scheme, file));
        Log.d("SUKA", "RESPONSE " + response);
        return parseForm(response);
    }

    private EditPostForm parseForm(String response) {
        EditPostForm form = new EditPostForm();
        Matcher matcher = statusInfo.matcher(response);
        if (matcher.find()) {
            form.setCanUpload(matcher.group(1).equals("1"));
            form.setStatusFile(matcher.group(2));
            form.setStatus(matcher.group(3));
            form.setError(!matcher.group(4).equals("0"));
        }

        matcher = loadedAttachments.matcher(response);
        while (matcher.find()) {
            AttachmentItem item = new AttachmentItem();
            item.setId(Integer.parseInt(matcher.group(1)));
            item.setName(matcher.group(2));
            item.setFormat(matcher.group(3));
            item.setWeight(matcher.group(4));
            item.setType(matcher.group(5));
            item.createImageUrl();
            item.setLoadState(AttachmentItem.STATE_LOADED);
            form.addAttachment(item);
        }
        return form;
    }

    private EditPostForm _deleteFiles(int postId, List<AttachmentItem> items) {
        for (AttachmentItem item : items) {
            //do request
        }
        return null;
    }
}
