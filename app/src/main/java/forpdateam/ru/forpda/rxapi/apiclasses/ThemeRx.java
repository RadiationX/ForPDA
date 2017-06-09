package forpdateam.ru.forpda.rxapi.apiclasses;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.theme.models.Poll;
import forpdateam.ru.forpda.api.theme.models.PollQuestion;
import forpdateam.ru.forpda.api.theme.models.PollQuestionItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ThemeRx {
    private final static Pattern firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])");


    public Observable<ThemePage> getTheme(final String url, boolean withHtml, boolean hatOpen, boolean pollOpen) {
        return Observable.fromCallable(() -> transform(Api.Theme().getTheme(url, hatOpen, pollOpen), withHtml));
    }

    public Observable<String> reportPost(int themeId, int postId, String message) {
        return Observable.fromCallable(() -> Api.Theme().reportPost(themeId, postId, message));
    }

    public Observable<Boolean> deletePost(int postId) {
        return Observable.fromCallable(() -> Api.Theme().deletePost(postId));
    }

    public Observable<String> votePost(int postId, boolean type) {
        return Observable.fromCallable(() -> Api.Theme().votePost(postId, type));
    }

    public static ThemePage transform(ThemePage page, boolean withHtml) throws Exception {
        if (withHtml) {
            int memberId = ClientHelper.getUserId();
            long time = System.currentTimeMillis();
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_THEME);
            boolean authorized = ClientHelper.getAuthState();
            boolean prevDisabled = page.getPagination().getCurrent() <= 1;
            boolean nextDisabled = page.getPagination().getCurrent() == page.getPagination().getAll();

            t.setVariableOpt("topic_title", Utils.htmlEncode(page.getTitle()));
            t.setVariableOpt("topic_description", Utils.htmlEncode(page.getDesc()));
            t.setVariableOpt("topic_url", page.getUrl());
            t.setVariableOpt("in_favorite", Boolean.toString(page.isInFavorite()));
            t.setVariableOpt("all_pages", page.getPagination().getAll());
            t.setVariableOpt("posts_on_page", page.getPagination().getPerPage());
            t.setVariableOpt("current_page", page.getPagination().getCurrent());
            t.setVariableOpt("authorized", Boolean.toString(authorized));
            t.setVariableOpt("is_curator", Boolean.toString(page.isCurator()));
            t.setVariableOpt("member_id", ClientHelper.getUserId());
            t.setVariableOpt("elem_to_scroll", page.getAnchor());
            t.setVariableOpt("body_type", "topic");
            t.setVariableOpt("navigation_disable", prevDisabled && nextDisabled ? "navigation_disable" : "");
            t.setVariableOpt("first_disable", getDisableStr(prevDisabled));
            t.setVariableOpt("prev_disable", getDisableStr(prevDisabled));
            t.setVariableOpt("next_disable", getDisableStr(nextDisabled));
            t.setVariableOpt("last_disable", getDisableStr(nextDisabled));
            boolean isDisableAvatar = App.getInstance().getPreferences().getBoolean(Preferences.Theme.SHOW_AVATARS, true);
            t.setVariableOpt("disable_avatar_js", Boolean.toString(isDisableAvatar));
            t.setVariableOpt("disable_avatar", isDisableAvatar ? "show_avatar" : "hide_avatar");
            t.setVariableOpt("avatar_type", App.getInstance().getPreferences().getBoolean(Preferences.Theme.CIRCLE_AVATARS, true) ? "circle_avatar" : "square_avatar");

            Log.d("FORPDA_LOG", "template check 1 " + (System.currentTimeMillis() - time));

            int hatPostId = page.getPosts().get(0).getId();
            Log.d("FORPDA_LOG", "template check 2 " + (System.currentTimeMillis() - time));
            Matcher letterMatcher = null;
            for (ThemePost post : page.getPosts()) {
                t.setVariableOpt("user_online", post.isOnline() ? "online" : "");
                t.setVariableOpt("post_id", post.getId());
                t.setVariableOpt("user_id", post.getUserId());

                //Post header
                //t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "file:///android_asset/av.png" : "http://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
                t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "" : "http://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
                t.setVariableOpt("none_avatar", post.getAvatar().isEmpty() ? "none_avatar" : "");

                if (letterMatcher != null) {
                    letterMatcher = letterMatcher.reset(post.getNick());
                } else {
                    letterMatcher = firstLetter.matcher(post.getNick());
                }
                String letter = null;
                if (letterMatcher.find()) {
                    letter = letterMatcher.group(1);
                } else {
                    letter = post.getNick().substring(0, 1);
                }
                t.setVariableOpt("nick_letter", letter);
                t.setVariableOpt("nick", Utils.htmlEncode(post.getNick()));
                t.setVariableOpt("curator", post.isCurator() ? "curator" : "");
                t.setVariableOpt("group_color", post.getGroupColor());
                t.setVariableOpt("group", post.getGroup());
                t.setVariableOpt("reputation", post.getReputation());
                t.setVariableOpt("date", post.getDate());
                t.setVariableOpt("number", post.getNumber());

                //Post body
                if (page.getPosts().size() > 1 && hatPostId == post.getId()) {
                    t.setVariableOpt("hat_state_class", page.isHatOpen() ? "open" : "close");
                    t.setVariableOpt("hat_body_state", page.isPollOpen() ? "" : "hidden");
                    t.addBlockOpt("hat_button");
                    t.addBlockOpt("hat_content_start");
                    t.addBlockOpt("hat_content_end");
                } else {
                    t.setVariableOpt("hat_state_class", "");
                }
                t.setVariableOpt("body", post.getBody());

                //Post footer

                if (post.canReport() && authorized)
                    t.addBlockOpt("report_block");
                if (page.canQuote() && authorized && post.getUserId() != memberId)
                    t.addBlockOpt("reply_block");
                if (authorized && post.getUserId() != memberId)
                    t.addBlockOpt("vote_block");
                if (post.canDelete() && authorized)
                    t.addBlockOpt("delete_block");
                if (post.canEdit() && authorized)
                    t.addBlockOpt("edit_block");

                t.addBlockOpt("post");
            }

            //Poll block
            if (page.getPoll() != null) {
                t.setVariableOpt("poll_state_class", page.isPollOpen() ? "open" : "close");
                t.setVariableOpt("poll_body_state", page.isPollOpen() ? "" : "hidden");
                Poll poll = page.getPoll();
                boolean isResult = poll.isResult();
                t.setVariableOpt("poll_type", isResult ? "result" : "default");
                t.setVariableOpt("poll_title", poll.getTitle().isEmpty() || poll.getTitle().equals("-") ? "Опрос" : poll.getTitle());

                for (PollQuestion question : poll.getQuestions()) {
                    t.setVariableOpt("question_title", question.getTitle());

                    for (PollQuestionItem questionItem : question.getQuestionItems()) {
                        t.setVariableOpt("question_item_title", questionItem.getTitle());

                        if (isResult) {
                            t.setVariableOpt("question_item_votes", questionItem.getVotes());
                            t.setVariableOpt("question_item_percent", Float.toString(questionItem.getPercent()));
                            t.addBlockOpt("poll_result_item");
                        } else {
                            t.setVariableOpt("question_item_type", questionItem.getType());
                            t.setVariableOpt("question_item_name", questionItem.getName());
                            t.setVariableOpt("question_item_value", questionItem.getValue());
                            t.addBlockOpt("poll_default_item");
                        }
                    }
                    t.addBlockOpt("poll_question_block");
                }
                t.setVariableOpt("poll_votes_count", poll.getVotesCount());
                if (poll.haveButtons()) {
                    if (poll.haveVoteButton())
                        t.addBlockOpt("poll_vote_button");
                    if (poll.haveShowResultsButton())
                        t.addBlockOpt("poll_show_results_button");
                    if (poll.haveShowPollButton())
                        t.addBlockOpt("poll_show_poll_button");
                    t.addBlockOpt("poll_buttons");
                }
                t.addBlockOpt("poll_block");
            }


            Log.d("FORPDA_LOG", "template check 3 " + (System.currentTimeMillis() - time));
            page.setHtml(t.generateOutput());
            Log.d("FORPDA_LOG", "template check 4 " + (System.currentTimeMillis() - time));
            t.reset();
            Log.d("FORPDA_LOG", "template check 5 " + (System.currentTimeMillis() - time));
        }

        /*final String veryLongString = page.getHtml();

        int maxLogSize = 1000;
        for (int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > veryLongString.length() ? veryLongString.length() : end;
            Log.v("FORPDA_LOG", veryLongString.substring(start, end));
        }*/

        return page;
    }

    private static String getDisableStr(boolean b) {
        return b ? "disabled" : "";
    }

}
