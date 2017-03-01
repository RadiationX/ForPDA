package forpdateam.ru.forpda.api.search.models;

import android.net.Uri;
import android.util.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 01.02.17.
 */

public class SearchSettings {
    private final static Pattern argsPattern = Pattern.compile("(?:\\?|\\&)([^=]*?)=([\\s\\S]*?)(?=&| |$)");
    public final static Pair<String, String> RESOURCE_NEWS = new Pair<>("news", "Новости");
    public final static Pair<String, String> RESOURCE_FORUM = new Pair<>("forum", "Форум");

    public final static String ARG_RESULT = "result";
    public final static String ARG_SORT = "sort";
    public final static String ARG_SOURCE = "source";
    public final static String ARG_QUERY_FORUM = "query";
    public final static String ARG_QUERY_NEWS = "s";
    public final static String ARG_NICK = "username";
    public final static String ARG_FORUMS_SIMPLE = "forums";
    public final static String ARG_TOPICS_SIMPLE = "topics";
    public final static String ARG_FORUMS = "forums%5B%5D";
    public final static String ARG_TOPICS = "topics%5B%5D";
    public final static String ARG_SUB_FORUMS = "subforums";
    public final static String ARG_NO_FORM = "noform";
    public final static String ARG_ST = "st";

    public final static Pair<String, String> RESULT_TOPICS = new Pair<>("topics", "Темы");
    public final static Pair<String, String> RESULT_POSTS = new Pair<>("posts", "Сообщения");

    public final static Pair<String, String> SORT_DA = new Pair<>("da", "Убываение даты");
    public final static Pair<String, String> SORT_DD = new Pair<>("dd", "Возрастание даты");
    public final static Pair<String, String> SORT_REL = new Pair<>("rel", "Соответствие");

    public final static Pair<String, String> SOURCE_ALL = new Pair<>("all", "Везде");
    public final static Pair<String, String> SOURCE_TITLES = new Pair<>("top", "Заголовки");
    public final static Pair<String, String> SOURCE_CONTENT = new Pair<>("pst", "Содержание");

    public final static int SUB_FORUMS_TRUE = 1;
    public final static int SUB_FORUMS_FALSE = 0;

    private String resourceType, result, sort, source, query, nick;
    private int subforums;
    private int st = 0;
    private List<Integer> forums, topics;

    public SearchSettings() {
        resourceType = RESOURCE_FORUM.first;
        result = RESULT_POSTS.first;
        sort = SORT_DD.first;
        source = SOURCE_ALL.first;
        query = "";
        nick = "";
        subforums = SUB_FORUMS_FALSE;
        forums = new ArrayList<>();
        topics = new ArrayList<>();
    }


    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public List<Integer> getForums() {
        return forums;
    }

    public void addForum(int forum) {
        forums.add(forum);
    }

    public List<Integer> getTopics() {
        return topics;
    }

    public void addTopic(int topic) {
        topics.add(topic);
    }

    public int getSubforums() {
        return subforums;
    }

    public void setSubforums(int subforums) {
        this.subforums = subforums;
    }

    public static SearchSettings parseSettings(String url) {
        return parseSettings(new SearchSettings(), url);
    }

    public static SearchSettings parseSettings(SearchSettings settings, String url) {
        Matcher matcher = argsPattern.matcher(url);
        String name, value;
        while (matcher.find()) {
            name = matcher.group(1);
            value = matcher.group(2);
            switch (name) {
                case SearchSettings.ARG_ST:
                    settings.setSt(Integer.parseInt(value));
                    break;
                case SearchSettings.ARG_RESULT:
                    settings.setResult(value);
                    break;
                case SearchSettings.ARG_SORT:
                    settings.setSort(value);
                    break;
                case SearchSettings.ARG_SOURCE:
                    settings.setSource(value);
                    break;
                case SearchSettings.ARG_QUERY_FORUM:
                    settings.setResourceType(SearchSettings.RESOURCE_FORUM.first);
                    try {
                        settings.setQuery(URLDecoder.decode(value, "windows-1251"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case SearchSettings.ARG_QUERY_NEWS:
                    settings.setResourceType(SearchSettings.RESOURCE_NEWS.first);
                    try {
                        settings.setQuery(URLDecoder.decode(value, "windows-1251"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case SearchSettings.ARG_NICK:
                    try {
                        settings.setNick(URLDecoder.decode(value, "windows-1251"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case SearchSettings.ARG_SUB_FORUMS:
                    settings.setSubforums(Integer.parseInt(value));
                    break;
            }
            if (name.contains(SearchSettings.ARG_FORUMS_SIMPLE)) {
                try {
                    settings.addForum(Integer.parseInt(value));
                } catch (NumberFormatException ignore) {
                }
                continue;
            }
            if (name.contains(SearchSettings.ARG_TOPICS_SIMPLE)) {
                try {
                    settings.addTopic(Integer.parseInt(value));
                } catch (NumberFormatException ignore) {
                }
                continue;
            }
        }
        return settings;
    }

    public String toUrl() {
        return toUrl(this);
    }

    public static String toUrl(SearchSettings settings) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("4pda.ru");
        if (settings.getResourceType().equals(RESOURCE_NEWS.first)) {
            builder.appendPath("page");
            builder.appendPath(Integer.toString(settings.getSt()));
            try {
                builder.appendQueryParameter(ARG_QUERY_NEWS, URLEncoder.encode(settings.getQuery(), "windows-1251"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            builder.appendPath("forum");
            builder.appendQueryParameter("act", "search");
            builder.appendQueryParameter(ARG_RESULT, settings.getResult());
            builder.appendQueryParameter(ARG_SORT, settings.getSort());
            builder.appendQueryParameter(ARG_SOURCE, settings.getSource());
            if (settings.getQuery() != null && !settings.getQuery().isEmpty()) {
                try {
                    builder.appendQueryParameter(ARG_QUERY_FORUM, URLEncoder.encode(settings.getQuery(), "windows-1251"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (settings.getNick() != null && !settings.getNick().isEmpty()) {
                try {
                    builder.appendQueryParameter(ARG_NICK, URLEncoder.encode(settings.getNick(), "windows-1251"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            for (int forum : settings.getForums())
                builder.appendQueryParameter(ARG_FORUMS, Integer.toString(forum));

            for (int topic : settings.getTopics())
                builder.appendQueryParameter(ARG_TOPICS, Integer.toString(topic));

            builder.appendQueryParameter(ARG_SUB_FORUMS, Integer.toString(settings.getSubforums()));
            builder.appendQueryParameter(ARG_NO_FORM, "1");
            builder.appendQueryParameter(ARG_ST, Integer.toString(settings.getSt()));
        }

        String url = builder.build().toString();
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }
}
