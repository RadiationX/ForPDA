package forpdateam.ru.forpda.rxapi.apiclasses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.search.models.SearchSettings;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class SearchRx {
    private final static Pattern firstLetter = Pattern.compile("([a-zA-Zа-яА-Я])");

    public Observable<SearchResult> getSearch(SearchSettings settings, boolean withHtml) {
        return Observable.fromCallable(() -> transform(Api.Search().getSearch(settings), withHtml));
    }

    public static SearchResult transform(SearchResult page, boolean withHtml) throws Exception {
        if (withHtml) {
            int memberId = ClientHelper.getUserId();
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_SEARCH);
            App.setTemplateResStrings(t);
            boolean authorized = ClientHelper.getAuthState();
            boolean prevDisabled = page.getPagination().getCurrent() <= 1;
            boolean nextDisabled = page.getPagination().getCurrent() == page.getPagination().getAll();

            t.setVariableOpt("style_type", App.getInstance().getCssStyleType());

            /*t.setVariableOpt("topic_title", Utils.htmlEncode(page.getTitle()));
            t.setVariableOpt("topic_description", Utils.htmlEncode(page.getDesc()));
            t.setVariableOpt("topic_url", page.getUrl());
            t.setVariableOpt("in_favorite", Boolean.toString(page.isInFavorite()));
            t.setVariableOpt("all_pages", page.getPagination().getAll());
            t.setVariableOpt("posts_on_page", page.getPagination().getPerPage());
            t.setVariableOpt("current_page", page.getPagination().getCurrent());
            t.setVariableOpt("authorized", Boolean.toString(authorized));
            t.setVariableOpt("is_curator", Boolean.toString(page.isCurator()));
            t.setVariableOpt("member_id", ClientHelper.getUserId());
            t.setVariableOpt("elem_to_scroll", page.getElementToScroll());*/
            t.setVariableOpt("body_type", "search");
            t.setVariableOpt("navigation_disable", ThemeRx.getDisableStr(prevDisabled && nextDisabled));
            t.setVariableOpt("first_disable", ThemeRx.getDisableStr(prevDisabled));
            t.setVariableOpt("prev_disable", ThemeRx.getDisableStr(prevDisabled));
            t.setVariableOpt("next_disable", ThemeRx.getDisableStr(nextDisabled));
            t.setVariableOpt("last_disable", ThemeRx.getDisableStr(nextDisabled));
            boolean isDisableAvatar = Preferences.Theme.isShowAvatars();
            t.setVariableOpt("disable_avatar_js", Boolean.toString(isDisableAvatar));
            t.setVariableOpt("disable_avatar", isDisableAvatar ? "show_avatar" : "hide_avatar");
            t.setVariableOpt("avatar_type", Preferences.Theme.isCircleAvatars() ? "circle_avatar" : "square_avatar");


            Matcher letterMatcher = null;
            for (SearchItem post : page.getItems()) {
                t.setVariableOpt("topic_id", post.getTopicId());
                t.setVariableOpt("post_title", post.getTitle());

                t.setVariableOpt("user_online", post.isOnline() ? "online" : "");
                t.setVariableOpt("post_id", post.getId());
                t.setVariableOpt("user_id", post.getUserId());

                //Post header
                t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "" : "https://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
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
                //t.setVariableOpt("curator", false ? "curator" : "");
                t.setVariableOpt("group_color", post.getGroupColor());
                t.setVariableOpt("group", post.getGroup());
                t.setVariableOpt("reputation", post.getReputation());
                t.setVariableOpt("date", post.getDate());
                //t.setVariableOpt("number", post.getNumber());

                //Post body
                t.setVariableOpt("body", post.getBody());

                //Post footer

                /*if (post.canReport() && authorized)
                    t.addBlockOpt("report_block");
                if (page.canQuote() && authorized && post.getUserId() != memberId)
                    t.addBlockOpt("reply_block");
                if (authorized && post.getUserId() != memberId)
                    t.addBlockOpt("vote_block");
                if (post.canDelete() && authorized)
                    t.addBlockOpt("delete_block");
                if (post.canEdit() && authorized)
                    t.addBlockOpt("edit_block");*/

                t.addBlockOpt("post");
            }

            page.setHtml(t.generateOutput());
            t.reset();
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
}
