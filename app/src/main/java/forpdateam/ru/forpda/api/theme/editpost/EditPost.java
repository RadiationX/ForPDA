package forpdateam.ru.forpda.api.theme.editpost;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPoll;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;

/**
 * Created by radiationx on 10.01.17.
 */

public class EditPost {
    private final static Pattern formInfoPattern = Pattern.compile("is_mod\\s*?=\\s*?(\\d+)[\\s\\S]*?poll_questions\\s*?=\\s*?(\\{[\\s\\S]*?\\})\\n,[\\s\\S]*?poll_choices\\s*?=\\s*?(\\{[\\s\\S]*?\\})\\n[\\s\\S]*?poll_votes\\s*?=\\s*?(\\{[\\s\\S]*?\\})\\n[\\s\\S]*?poll_multi\\s*?=\\s*?(\\{[\\s\\S]*?\\})\\n[\\s\\S]*?max_poll_questions\\s*?=\\s*?(\\d+)[\\s\\S]*?max_poll_choices\\s*?=\\s*?(\\d+)[\\s\\S]*?<input[^>]*?name=\"poll_question\"[^>]*?value=\"([^\"]*?)\"");

    private final static Pattern fckngInvalidJsonPattern = Pattern.compile("(?:\\{|\\,)[\\\"\\']?(\\d+)(?:_(\\d+))?[\\\"\\']?\\s*?\\:\\s*?[\\\"\\']([^\\'\\\"]*?)[\\\"\\'](?:\\})?");
    //private final static Pattern pollIndicesPattern = Pattern.compile("(\\d+)_(\\d+)");


    private final static Pattern postPattern = Pattern.compile("[^<]*?<textarea[^>]*>([\\s\\S]*?)<\\/textarea>[\\s\\S]*?<input[^>]*?name=\"post_edit_reason\" value=\"([^\"]*?)\"");
    private final static Pattern loadedAttachments = Pattern.compile("add_current_item\\([^'\"]*?['\"](\\d+)['\"],[^'\"]*?['\"]([^'\"]*\\.(\\w*))['\"],[^'\"]*?['\"]([^'\"]*?)['\"],[^'\"]*?['\"][^'\"]*\\/(\\w*)\\.[^\"']*?['\"]");
    private final static Pattern statusInfo = Pattern.compile("can_upload = parseInt\\([\"'](\\d+)'\\)[\\s\\S]*?status_msg_files = .([\\s\\S]*?).;[\\s\\S]*?status_msg = .([\\s\\S]*?).;[\\s\\S]*?status_is_error = ([\\s\\S]*?);");

    private final static Pattern attachmentsPattern = Pattern.compile("(\\d+)\u0002([^\u0002]*?)\u0002([^\u0002]*?)\u0002(\\/\\/[^\u0002]*?)\u0002(\\d+)\u0002([0-9a-fA-F]+)(?:(?:\u0002(\\/\\/[^\u0002]*?)\u0002(\\d+)\u0002(\\d+))?(?:\u0003\u0004(\\d+)\u0003\u0004([^\u0002]*?)\u0003\u0004([^\u0002]*?)\u0003)?)?");

    public static void printPoll(EditPoll poll) {
        if (poll != null) {
            Log.d("POLL", "poll_question: " + poll.getTitle() + " : {" + poll.getMaxQuestions() + ":" + poll.getMaxChoices() + "} : " + poll.getBaseIndexOffset() + " +" + poll.getIndexOffset());
            for (int i = 0; i < poll.getQuestions().size(); i++) {
                EditPoll.Question question = poll.getQuestion(i);
                int q_index = question.getIndex();
                Log.d("POLL", "question[" + q_index + "]: " + question.getTitle() + " : " + question.getBaseIndexOffset() + " +" + question.getIndexOffset());
                Log.d("POLL", "multi[" + q_index + "]: " + (question.isMulti() ? "1" : "0"));
                for (int j = 0; j < question.getChoices().size(); j++) {
                    EditPoll.Choice choice = question.getChoice(j);
                    int c_index = choice.getIndex();
                    Log.d("POLL", "choice[" + q_index + '_' + c_index + "]: " + choice.getTitle());
                }
            }
        }
    }

    public EditPostForm loadForm(int postId) throws Exception {
        Log.d("FORPDA_LOG", "START LOAD FORM");
        EditPostForm form = new EditPostForm();
        String url = "http://4pda.ru/forum/index.php?act=post&do=edit_post&p=".concat(Integer.toString(postId));
        //url = url.concat("&t=").concat(Integer.toString(topicId)).concat("&f=").concat(Integer.toString(forumId));

        NetworkResponse response = Api.getWebClient().get(url);
        if (response.getBody().equals("nopermission")) {
            form.setErrorCode(EditPostForm.ERROR_NO_PERMISSION);
            return form;
        }
        Matcher matcher = postPattern.matcher(response.getBody());
        if (matcher.find()) {
            Log.d("FORPDA_LOG", "MESSAGE " + matcher.group(1));
            form.setMessage(Utils.fromHtml(Utils.escapeNewLine(matcher.group(1))));
            Log.d("FORPDA_LOG", "REASON " + matcher.group(2));
            form.setEditReason(matcher.group(2));
        }
        matcher = formInfoPattern.matcher(response.getBody());
        if (matcher.find()) {
            EditPoll poll = createPoll(matcher);
            form.setPoll(poll);
            EditPost.printPoll(poll);
        }


        /*response = Api.getWebClient().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=".concat(Integer.toString(postId)));
        matcher = loadedAttachments.matcher(response);
        while (matcher.find())
            form.addAttachment(fillAttachment(new AttachmentItem(), matcher));
        Log.d("FORPDA_LOG", "ATTACHES " + form.getAttachments().size());
*/
        response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=attach&index=1&relId=" + postId + "&maxSize=134217728&allowExt=&code=init&unlinked=");
        matcher = attachmentsPattern.matcher(response.getBody());
        Log.d("SUKA", "NEW ATTACHES " + response.getBody());
        while (matcher.find()) {
            Log.d("SUKA", "NEW ATTACH " + matcher.group(2));
            form.addAttachment(fillAttachmentV2(new AttachmentItem(), matcher));
        }

        return form;
    }

    private EditPoll createPoll(Matcher matcher) {
        EditPoll poll = new EditPoll();

        Matcher jsonMatcher = fckngInvalidJsonPattern.matcher(matcher.group(2));
        while (jsonMatcher.find()) {
            EditPoll.Question question = new EditPoll.Question();
            int questionIndex = Integer.parseInt(jsonMatcher.group(1));
            if (questionIndex > poll.getBaseIndexOffset()) {
                poll.setBaseIndexOffset(questionIndex);
            }
            question.setIndex(questionIndex);
            question.setTitle(Utils.fromHtml(jsonMatcher.group(3)));
            poll.addQuestion(question);
        }

        jsonMatcher = jsonMatcher.reset(matcher.group(3));
        while (jsonMatcher.find()) {
            int questionIndex = Integer.parseInt(jsonMatcher.group(1));
            EditPoll.Question question = EditPoll.findQuestionByIndex(poll, questionIndex);
            if (question != null) {
                EditPoll.Choice choice = new EditPoll.Choice();

                int choiceIndex = Integer.parseInt(jsonMatcher.group(2));
                if (choiceIndex > question.getBaseIndexOffset()) {
                    question.setBaseIndexOffset(choiceIndex);
                }
                choice.setIndex(choiceIndex);
                choice.setTitle(Utils.fromHtml(jsonMatcher.group(3)));
                question.addChoice(choice);
            }
        }

        jsonMatcher = jsonMatcher.reset(matcher.group(4));
        while (jsonMatcher.find()) {
            int questionIndex = Integer.parseInt(jsonMatcher.group(1));
            EditPoll.Question question = EditPoll.findQuestionByIndex(poll, questionIndex);
            if (question != null) {
                int choiceIndex = Integer.parseInt(jsonMatcher.group(2));
                EditPoll.Choice choice = EditPoll.findChoiceByIndex(question, choiceIndex);
                if (choice != null) {
                    choice.setVotes(Integer.parseInt(jsonMatcher.group(3)));
                }
            }
        }

        jsonMatcher = jsonMatcher.reset(matcher.group(5));
        while (jsonMatcher.find()) {
            int questionIndex = Integer.parseInt(jsonMatcher.group(1));
            EditPoll.Question question = EditPoll.findQuestionByIndex(poll, questionIndex);
            if (question != null) {
                question.setMulti(jsonMatcher.group(3).equals("1"));
            }
        }

        poll.setMaxQuestions(Integer.parseInt(matcher.group(6)));
        poll.setMaxChoices(Integer.parseInt(matcher.group(7)));
        poll.setTitle(Utils.fromHtml(matcher.group(8)));
        return poll;
    }

    public List<AttachmentItem> uploadFiles(int postId, List<RequestFile> files) throws Exception {
        String url = "http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=".concat(postId == 0 ? "" : Integer.toString(postId));
        List<AttachmentItem> items = new ArrayList<>();
        AttachmentItem item;
        NetworkResponse response;
        Matcher matcher;
        for (RequestFile file : files) {
            file.setRequestName("FILE_UPLOAD[]");
            item = new AttachmentItem();
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .url(url)
                    .file(file);

            response = Api.getWebClient().request(builder.build());
            matcher = loadedAttachments.matcher(response.getBody());
            if (matcher.find())
                item = fillAttachment(item, matcher);

            matcher = statusInfo.matcher(response.getBody());
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
            items.add(item);
        }
        return items;
    }

    public List<AttachmentItem> uploadFilesV2(int postId, List<RequestFile> files, List<AttachmentItem> pending) throws Exception {

        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=attach")
                .xhrHeader()
                .formHeader("index", "1")
                .formHeader("relId", Integer.toString(postId))
                .formHeader("maxSize", "134217728")
                .formHeader("allowExt", "")
                .formHeader("forum-attach-files", "")
                .formHeader("code", "check");
        NetworkResponse response;
        Matcher matcher = null;
        for (int i = 0; i < files.size(); i++) {
            RequestFile file = files.get(i);
            AttachmentItem item = pending.get(i);

            file.setRequestName("FILE_UPLOAD[]");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] targetArray = new byte[file.getFileStream().available()];
            file.getFileStream().read(targetArray);
            InputStream is1 = new ByteArrayInputStream(targetArray);
            file.getFileStream().close();
            file.setFileStream(is1);
            messageDigest.update(targetArray);
            byte[] hash = messageDigest.digest();
            String md5 = ByteArraytoHexString(hash);
            Log.d("SUKA", "REQUEST FILE " + file.getFileName() + " : " + file.getFileStream().available() + " : " + md5);
            builder.formHeader("md5", md5)
                    .formHeader("size", "" + file.getFileStream().available())
                    .formHeader("name", file.getFileName());

            response = Api.getWebClient().request(builder.build());
            Log.d("SUKA", "RESPONSE " + response);
            if (response.getBody().equals("0")) {
                NetworkRequest.Builder uploadRequest = new NetworkRequest.Builder()
                        .url("http://4pda.ru/forum/index.php?act=attach")
                        .xhrHeader()
                        .formHeader("index", "1")
                        .formHeader("relId", Integer.toString(postId))
                        .formHeader("maxSize", "134217728")
                        .formHeader("allowExt", "")
                        .formHeader("forum-attach-files", "")
                        .formHeader("code", "upload")
                        .file(file);
                response = Api.getWebClient().request(uploadRequest.build(), item.getProgressListener());
                Log.d("SUKA", "RESPONSE2 " + response.getBody());
            }
            if (matcher == null) {
                matcher = attachmentsPattern.matcher(response.getBody());
            } else {
                matcher = matcher.reset(response.getBody());
            }
            if (matcher.find()) {
                fillAttachmentV2(item, matcher);
            }
            item.setStatus(AttachmentItem.STATUS_UPLOADED);
        }
        return pending;
    }

    private static String ByteArraytoHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public List<AttachmentItem> deleteFiles(int postId, List<AttachmentItem> items) throws Exception {
        NetworkResponse response;
        Matcher matcher;
        for (AttachmentItem item : items) {
            response = Api.getWebClient().get("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_remove&attach_rel_id=".concat(postId == 0 ? "" : Integer.toString(postId)).concat("&attach_id=").concat(Integer.toString(item.getId())));
            matcher = statusInfo.matcher(response.getBody());
            if (matcher.find())
                fillAttachmentStatus(item, matcher);
        }
        return items;
    }

    public List<AttachmentItem> deleteFilesV2(int postId, List<AttachmentItem> items) throws Exception {
        NetworkResponse response;
        for (AttachmentItem item : items) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder()
                    .url("http://4pda.ru/forum/index.php?act=attach")
                    .xhrHeader()
                    .formHeader("index", "1")
                    .formHeader("relId", Integer.toString(postId))
                    .formHeader("maxSize", "134217728")
                    .formHeader("allowExt", "")
                    .formHeader("code", "remove")
                    .formHeader("id", Integer.toString(item.getId()));
            response = Api.getWebClient().request(builder.build());
            //todo проверка на ошибки, я хз че еще может быть кроме 0
            if (response.getBody().equals("0")) {
                item.setStatus(AttachmentItem.STATUS_REMOVED);
                item.setError(false);
            }
        }
        return items;
    }

    private AttachmentItem fillAttachment(AttachmentItem item, Matcher matcher) {
        item.setId(Integer.parseInt(matcher.group(1)));
        try {
            item.setName(URLDecoder.decode(matcher.group(2), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        item.setExtension(matcher.group(3));
        item.setWeight(matcher.group(4));
        if (item.getTypeFile() == AttachmentItem.TYPE_IMAGE) {
            item.setImageUrl("http://cs5-2.4pda.to/".concat(Integer.toString(item.getId())).concat(".").concat(item.getExtension()));
        }
        item.setLoadState(AttachmentItem.STATE_LOADED);
        return item;
    }

    private AttachmentItem fillAttachmentV2(AttachmentItem item, Matcher matcher) {
        item.setId(Integer.parseInt(matcher.group(1)));
        try {
            item.setName(URLDecoder.decode(matcher.group(2), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        item.setExtension(matcher.group(3));
        String temp = readableFileSize(Long.parseLong(matcher.group(5)));
        item.setWeight(temp);
        item.setMd5(matcher.group(6));
        temp = matcher.group(7);

        if (temp != null) {
            item.setTypeFile(AttachmentItem.TYPE_IMAGE);
            item.setImageUrl("http:".concat(temp));
            item.setWidth(Integer.parseInt(matcher.group(8)));
            item.setHeight(Integer.parseInt(matcher.group(9)));
        }
        item.setLoadState(AttachmentItem.STATE_LOADED);
        return item;
    }

    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"Б", "КБ", "МБ", "ГБ", "ТБ"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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

        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url(url)
                .formHeaders(headers)
                .multipart()
                .formHeader("act", "Post")
                .formHeader("CODE", form.getType() == EditPostForm.TYPE_NEW_POST ? "03" : "9")
                .formHeader("f", "" + form.getForumId())
                .formHeader("t", "" + form.getTopicId())
                .formHeader("auth_key", Api.getWebClient().getAuthKey())
                .formHeader("Post", form.getMessage())
                .formHeader("enablesig", "yes")
                .formHeader("enableemo", "yes")
                .formHeader("st", "" + form.getSt())
                .formHeader("removeattachid", "0")
                .formHeader("MAX_FILE_SIZE", "0")
                .formHeader("parent_id", "0")
                .formHeader("ed-0_wysiwyg_used", "0")
                .formHeader("editor_ids[]", "ed-0")
                .formHeader("iconid", "0")
                .formHeader("_upload_single_file", "1");
        EditPoll poll = form.getPoll();
        if (poll != null) {
            EditPost.printPoll(poll);
            builder.formHeader("poll_question", poll.getTitle().replaceAll("\n"," "));
            for (int i = 0; i < poll.getQuestions().size(); i++) {
                EditPoll.Question question = poll.getQuestion(i);
                int q_index = i + 1;
                builder.formHeader("question[" + q_index + "]", question.getTitle().replaceAll("\n"," "));
                builder.formHeader("multi[" + q_index + "]", question.isMulti() ? "1" : "0");
                for (int j = 0; j < question.getChoices().size(); j++) {
                    EditPoll.Choice choice = question.getChoice(j);
                    int c_index = j + 1;
                    builder.formHeader("choice[" + q_index + '_' + c_index + "]", choice.getTitle().replaceAll("\n"," "));
                }
            }
        }

        //.formHeader("file-list", addedFileList);
        if (form.getType() == EditPostForm.TYPE_EDIT_POST)
            builder.formHeader("post_edit_reason", form.getEditReason());
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
        builder.formHeader("file-list", ids.toString());
        if (form.getPostId() != 0)
            builder.formHeader("p", "" + form.getPostId());
        return Api.Theme().parsePage(url, Api.getWebClient().request(builder.build()), false, false);
    }
}
