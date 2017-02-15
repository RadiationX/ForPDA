package forpdateam.ru.forpda.api.forum;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.forum.models.ForumItem;
import forpdateam.ru.forpda.api.forum.models.ForumItemSecond;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;

/**
 * Created by radiationx on 15.02.17.
 */

public class Forum {
    Pattern rootPattern = Pattern.compile("<div[^>]*?id=[\"']fo_(\\d+)[\"'][^>]*?>[^<]*?<div[^>]*?cat_name[^>]*?>[^<]*?<div[\\s\\S]*?\\/div>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/div>([\\s\\S]*?)<\\/div>[^<]*?(?=<div id=['\"]fc|<div class=[\"']stat)");
    Pattern boardsPattern = Pattern.compile("<div[^>]*?board_forum_row[^>]*><div[^>]*?forum_name[^>]*?>[\\s\\S]*?<a[^>]*?showforum=(\\d+)[^>]*?>([^<]*?)<\\/a>[^<]*?<\\/div>");

    Pattern forumsFromSearch = Pattern.compile("<select[^>]*?name=[\"']forums(?:\\[\\])?[\"'][^>]*?>([\\s\\S]*?)<\\/select>");
    Pattern forumItemFromSearch = Pattern.compile("<option[^>]*?value=[\"'](\\d+)['\"][^>]*?>[^-\\s]*?(-*?) ([\\s\\S]*?)<\\/option>");


    public Observable<ForumItem> getForums() {
        return Observable.fromCallable(this::parse);
    }

    public Observable<List<ForumItemSecond>> getForumsSearch() {
        return Observable.fromCallable(this::parseFromSearch);
    }

    private ForumItem parse() throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=idx");
        Matcher rootMatcher = rootPattern.matcher(response);
        ForumItem root = new ForumItem();
        while (rootMatcher.find()) {
            suka();
            ForumItem item = new ForumItem();
            item.setId(Integer.parseInt(rootMatcher.group(1)));
            item.setTitle(rootMatcher.group(2));
            root.addForum(recourse(item, rootMatcher.group(3)));
        }
        return root;
    }

    private ForumItem recourse(ForumItem root, String body) throws Exception {
        suka();
        Matcher boards = boardsPattern.matcher(body);
        while (boards.find()) {
            ForumItem item = new ForumItem();
            item.setId(Integer.parseInt(boards.group(1)));
            item.setTitle(boards.group(2));
            if (item.getTitle().contains("редирект"))
                continue;
            String response = Client.getInstance().get("http://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(item.getId())));
            root.addForum(recourse(item, response));
        }
        return root;
    }

    private int i = 0;

    private void suka() {
        i++;
        Log.d("SUKA", "FORUM ITERATOR" + i);
    }

    private List<ForumItemSecond> parseFromSearch() throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=search");
        Matcher matcher = forumsFromSearch.matcher(response);
        List<ForumItemSecond> forumList = new ArrayList<>();
        if (matcher.find()) {
            matcher = forumItemFromSearch.matcher(matcher.group(1));

            final ForumItemSecond finalParent = new ForumItemSecond();
            List<ForumItemSecond> parentsList = new ArrayList<>();
            ForumItemSecond lastParent = finalParent;
            while (matcher.find()) {
                ForumItemSecond item = new ForumItemSecond();
                item.setId(Integer.parseInt(matcher.group(1)));
                item.setLevel(matcher.group(2).length() / 2);
                item.setTitle(matcher.group(3));
                if (item.getLevel() <= lastParent.getLevel()) {
                    if (lastParent.getLevel() == item.getLevel()) {
                        parentsList.remove(lastParent);
                    } else {
                        for (int i = 0; i < (lastParent.getLevel() - item.getLevel() + 1); i++) {
                            parentsList.remove(parentsList.size() - 1);
                        }
                    }
                    if (parentsList.size() == 0) {
                        lastParent = finalParent;
                    } else if (parentsList.size() > 0) {
                        lastParent = parentsList.get(parentsList.size() - 1);
                    }
                }
                item.setParentId(lastParent.getId());
                if (item.getLevel() > lastParent.getLevel()) {
                    lastParent = item;
                    parentsList.add(lastParent);
                }
                forumList.add(item);
            }
            parentsList.clear();
        }
        return forumList;
    }
}
