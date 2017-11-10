package forpdateam.ru.forpda.api.forum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.ApiUtils;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.forum.interfaces.IForumItemFlat;
import forpdateam.ru.forpda.api.forum.models.Announce;
import forpdateam.ru.forpda.api.forum.models.ForumItemFlat;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.api.forum.models.ForumRules;

/**
 * Created by radiationx on 15.02.17.
 */

public class Forum {
    private final static Pattern rulesHeaders = Pattern.compile("<b>([\\d\\.]+)\\s?([\\s\\S]*?)<\\/b>[^<]*?<[^>]*?br[^>]*?>([\\s\\S]*?<[^>]*?br[^>]*?>)(?=<[^>]*?br[^>]*?>(?:<b>|<[^>]*?br[^>]*?>))");
    private final static Pattern rulesItems = Pattern.compile("([\\d\\.]+)\\s?([\\s\\S]*?)<[^>]*?br[^>]*?>(?=[\\d\\.]+|$)");
    private final static Pattern announcePattern = Pattern.compile("<title>([\\s\\S]*?)(?: - 4PDA)?<\\/title>[\\s\\S]*?<div[^>]*?class=\"[^\"]*?postcolor[^\"]*?\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/td>");
    private final static Pattern forumsFromSearch = Pattern.compile("<select[^>]*?name=[\"']forums(?:\\[\\])?[\"'][^>]*?>([\\s\\S]*?)<\\/select>");
    private final static Pattern forumItemFromSearch = Pattern.compile("<option[^>]*?value=[\"'](\\d+)['\"][^>]*?>[^-\\s]*?(-*?) ([\\s\\S]*?)<\\/option>");

    //Для обхода по страницам, но чет там через попу всё работает - не все элементы находит
    //private final static Pattern rootPattern = Pattern.compile("<div[^>]*?id=[\"']fo_(\\d+)[\"'][^>]*?>[^<]*?<div[^>]*?cat_name[^>]*?>[^<]*?<div[\\s\\S]*?\\/div>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/div>([\\s\\S]*?)<\\/div>[^<]*?(?=<div id=['\"]fc|<div class=[\"']stat)");
    //private final static Pattern boardsPattern = Pattern.compile("<div[^>]*?board_forum_row[^>]*><div[^>]*?forum_name[^>]*?>[\\s\\S]*?<a[^>]*?showforum=(\\d+)[^>]*?>([^<]*?)<\\/a>[^<]*?<\\/div>");

    public ForumItemTree getForums() throws Exception {
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/forum/index.php?act=search");
        Matcher matcher = forumsFromSearch.matcher(response.getBody());
        final ForumItemTree root = new ForumItemTree();
        if (matcher.find()) {
            matcher = forumItemFromSearch.matcher(matcher.group(1));
            List<ForumItemTree> parentsList = new ArrayList<>();
            ForumItemTree lastParent = root;
            parentsList.add(lastParent);
            while (matcher.find()) {
                ForumItemTree item = new ForumItemTree();
                item.setId(Integer.parseInt(matcher.group(1)));
                item.setLevel(matcher.group(2).length() / 2);
                item.setTitle(ApiUtils.fromHtml(matcher.group(3)));
                if (item.getLevel() <= lastParent.getLevel()) {
                    //Удаление элементов, учитывая случай с резким скачком уровня вложенности
                    for (int i = 0; i < (lastParent.getLevel() - item.getLevel() + 1); i++)
                        parentsList.remove(parentsList.size() - 1);
                    lastParent = parentsList.get(parentsList.size() - 1);
                }
                item.setParentId(lastParent.getId());
                lastParent.addForum(item);
                if (item.getLevel() > lastParent.getLevel()) {
                    lastParent = item;
                    parentsList.add(lastParent);
                }
            }
            parentsList.clear();
        }
        return root;
    }

    public void transformToList(List<ForumItemFlat> list, ForumItemTree rootForum) {
        if (rootForum.getForums() == null) return;
        for (ForumItemTree item : rootForum.getForums()) {
            list.add(new ForumItemFlat(item));
            transformToList(list, item);
        }
    }

    public void transformToTree(Collection<? extends IForumItemFlat> list, ForumItemTree rootForum) {
        List<ForumItemTree> parentsList = new ArrayList<>();
        ForumItemTree lastParent = rootForum;
        parentsList.add(lastParent);
        for (IForumItemFlat item : list) {
            ForumItemTree newItem = new ForumItemTree(item);
            if (item.getLevel() <= lastParent.getLevel()) {
                //Удаление элементов, учитывая случай с резким скачком уровня вложенности
                for (int i = 0; i < (lastParent.getLevel() - item.getLevel() + 1); i++)
                    parentsList.remove(parentsList.size() - 1);
                lastParent = parentsList.get(parentsList.size() - 1);
            }
            lastParent.addForum(newItem);
            if (item.getLevel() > lastParent.getLevel()) {
                lastParent = newItem;
                parentsList.add(lastParent);
            }
        }
        parentsList.clear();
    }

    public Object markAllRead() throws Exception {
        Api.getWebClient().request(new NetworkRequest.Builder().url("https://4pda.ru/forum/index.php?act=login&CODE=05").withoutBody().build());
        return new Object();
    }

    public Object markRead(int id) throws Exception {
        Api.getWebClient().request(new NetworkRequest.Builder().url("https://4pda.ru/forum/index.php?act=auth&action=markforum&f=" + id + "&fromforum=" + id).withoutBody().build());
        return new Object();
    }

    public ForumRules getRules() throws Exception {
        ForumRules rules = new ForumRules();

        String response = Api.getWebClient().get("https://4pda.ru/forum/index.php?act=boardrules").getBody();
        Matcher headerMatcher = rulesHeaders.matcher(response);
        Matcher itemMatcher = null;
        while (headerMatcher.find()) {
            ForumRules.Item header = new ForumRules.Item();
            header.setHeader(true);
            header.setNumber(headerMatcher.group(1));
            header.setText(headerMatcher.group(2));
            String itemContent = headerMatcher.group(3);
            itemMatcher = itemMatcher == null ? rulesItems.matcher(itemContent) : itemMatcher.reset(itemContent);
            rules.addItem(header);
            while (itemMatcher.find()) {
                ForumRules.Item item = new ForumRules.Item();
                item.setNumber(itemMatcher.group(1));
                item.setText(itemMatcher.group(2));
                rules.addItem(item);
            }
        }

        return rules;
    }

    public Announce getAnnounce(int id, int forumId) throws Exception {
        Announce announce = new Announce();
        announce.setId(id);
        announce.setForumId(forumId);
        String response = Api.getWebClient().get("https://4pda.ru/forum/index.php?act=announce&f=" + forumId + "&st=" + id).getBody();
        Matcher matcher = announcePattern.matcher(response);
        if (matcher.find()) {
            announce.setTitle(matcher.group(1));
            announce.setHtml(matcher.group(2));
        }
        return announce;
    }
}
