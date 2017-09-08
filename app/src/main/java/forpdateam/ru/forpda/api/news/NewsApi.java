package forpdateam.ru.forpda.api.news;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.news.models.Comment;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.api.news.models.Material;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.api.news.models.Tag;
import forpdateam.ru.forpda.api.regex.RegexStorage;
import forpdateam.ru.forpda.api.regex.parser.Document;
import forpdateam.ru.forpda.api.regex.parser.Node;
import forpdateam.ru.forpda.api.regex.parser.Parser;

import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_SOFTWARE;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsApi {
    /*
    * 1. Дата курильщика (2017-08-24T07:00:00+00:00)
    * 2. Урл изображения
    * 3. Заголовок
    * 4. Дата нормального человека (24.08.17)
    * 5. Id автора
    * 6. Ник автора
    * 7. Кол-во комментов
    * 8. Сорсы тегов
    * 9. Вроде как контент
    * 10. Сорсы материалов по теме, их может не быть (null у group(9))
    * 11. Магический относительный id новости для навигации вперёд/назад
    * 12. Строка, по которой можно узнать доступность комментирования, если null или пустая, то низя
    * 13. Сорсы комментов
    * 14. Сорсы karma
    * */
    private final Pattern detailsPattern = Pattern.compile("<section[^>]*>[^<]*?<article[^>]*?>[\\s\\S]*?<meta[^>]*?content=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div[^>]*?class=\"photo\"[^>]*?>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<div[^>]*?class=\"description\"[^>]*?>[^<]*?<h1[^>]*?>(?:<span[^>]*?>)?([^<]*?)(?:<\\/span>)?<\\/h1>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[^<]*?<span[^>]*?class=\"name\"[^>]*?>[^<]*?<a[^>]*?href=\"[^\"]*?(\\d+)\"[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?class=\"more-box\"[^>]*?>[^<]*?<a[^>]*?>(\\d+)<\\/a>[\\s\\S]*?<div[^>]*?class=\"meta\"[^>]*?>([\\s\\S]*?)<\\/div>[\\s\\S]*?<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"materials-box\"[^>]*?>[\\s\\S]*?<ul class=\"materials-slider\"[^>]*?>([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>)?[^<]*?<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>[^<]*?<\\/h2>([\\s\\S]*?)<\\/div>([\\s\\S]*?)[^<]*?<br[^>]*?>[^<]*?<\\/div>[^<]*?<ul class=\"page-nav");
    private final Pattern excludeFormCommentPattern = Pattern.compile("<form[\\s\\S]*");

    /*
    * 1. tag для ссылки
    * 2. Заголовок
    * */
    private final Pattern tagsPattern = Pattern.compile("<a[^>]*?href=\"\\/tag\\/([^\"\\/]*?)\\/\"[^>]*?>([^<]*?)<\\/a>");


    /*
    * 1. Ссылка изображения
    * 2. id новости
    * 3. Заголовок
    * */
    private final Pattern materialsPattern = Pattern.compile("<li[^>]*?>[^<]*?<a[^>]*?>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[^<]*?<h3>[^<]*?<a[^>]*?href=\"[^\"]*?\\/(\\d+)\\/[^\"\\/]*?\"[^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/h3>");

    /*
    * 1. Id коммента
    * 2. Статус: 0 - не лайкнутый, 1 - лайкнутый, -1 - дизлайкнутый, (2 - нельзя лайкнуть) - хз, это с другого проекта значение, сам я такого не видел
    * */
    private final Pattern karmaPattern = Pattern.compile("\\\"(\\d+)\\\":\\[(.+?),(.+?),(.+?),(.+?)\\]");
    private final Pattern karmaSourcePattern = Pattern.compile("ModKarma\\(([\\s\\S]*?)\\)<\\/script>");


    public String getLink(@Nullable String category, int pageNumber) {
        String link = getUrlCategory(category);
        if (pageNumber >= 2) {
            link = link + "page/" + pageNumber + "/";
        }
        return link;
    }

    public List<NewsItem> getNews(String category, int pageNumber) throws Exception {
        String url = getLink(category, pageNumber);
        String response = Api.getWebClient().get(url).getBody();
        ArrayList<NewsItem> items = new ArrayList<>();
        final String regex = RegexStorage.News.List.getListPattern();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            NewsItem item = new NewsItem();
            boolean isReview = matcher.group(1) == null;
            if (!isReview) {
                item.setUrl(matcher.group(1));
                item.setId(Integer.parseInt(matcher.group(2)));
                item.setTitle(Utils.fromHtml(Utils.fromHtml(matcher.group(3))));
                item.setImgUrl(matcher.group(4));
                item.setCommentsCount(Integer.parseInt(matcher.group(5)));
                item.setDate(matcher.group(6));
                item.setAuthor(Utils.fromHtml(matcher.group(7)));
                item.setDescription(Utils.fromHtml(matcher.group(8)));
                parseTags(item.getTags(), matcher.group(9));
            } else {
                item.setUrl(matcher.group(10));
                item.setId(Integer.parseInt(matcher.group(11)));
                item.setImgUrl(matcher.group(12));
                item.setTitle(Utils.fromHtml(Utils.fromHtml(matcher.group(13))));
                item.setCommentsCount(Integer.parseInt(matcher.group(14)));
                item.setDate(matcher.group(16).replace('-', '.'));
                item.setAuthor(Utils.fromHtml(matcher.group(17)));
                item.setDescription(Utils.fromHtml(matcher.group(19).trim()));
            }
            items.add(item);
        }
        return items;
    }


    public DetailsPage getDetails(int id) throws Exception {
        String response = Api.getWebClient().get("https://4pda.ru/index.php?p=" + id).getBody();
        return parseArticle(id, response);
    }

    private DetailsPage parseArticle(int id, String response) {
        long time = System.currentTimeMillis();
        Matcher matcher = detailsPattern.matcher(response);
        DetailsPage page = new DetailsPage();
        if (matcher.find()) {
            Log.e("TIME", "Article found: " + (System.currentTimeMillis() - time));
            page.setId(id);
            page.setImgUrl(matcher.group(2));
            page.setTitle(Utils.fromHtml(matcher.group(3)));
            page.setDate(matcher.group(4));
            page.setAuthor(Utils.fromHtml(matcher.group(6)));
            page.setCommentsCount(Integer.parseInt(matcher.group(7)));
            parseTags(page.getTags(), matcher.group(8));
            page.setHtml(matcher.group(9));
            parseMaterials(page.getMaterials(), matcher.group(10));
            page.setNavId(matcher.group(11));

            parseKarma(page.getKarmaMap(), response);

            String comments = matcher.group(13);
            comments = excludeFormCommentPattern.matcher(comments).replaceFirst("");
            page.setCommentsSource(comments);

            /*Comment commentTree = parseComments(page.getKarmaMap(), page.getCommentsSource());
            page.setCommentTree(commentTree);*/
        }
        Log.e("TIME", "Article: " + (System.currentTimeMillis() - time));
        return page;
    }

    private void parseMaterials(ArrayList<Material> materials, String sourceMaterials) {
        if (sourceMaterials == null)
            return;
        Matcher matcher = materialsPattern.matcher(sourceMaterials);
        while (matcher.find()) {
            Material material = new Material();
            material.setImageUrl(matcher.group(1));
            material.setId(Integer.parseInt(matcher.group(2)));
            material.setTitle(Utils.fromHtml(matcher.group(3)));
            materials.add(material);
        }
    }

    private void parseTags(ArrayList<Tag> tags, String sourceTags) {
        if (sourceTags == null)
            return;
        Matcher matcher = tagsPattern.matcher(sourceTags);
        while (matcher.find()) {
            Tag tag = new Tag();
            tag.setTag(matcher.group(1));
            tag.setTitle(Utils.fromHtml(matcher.group(2)));
            tags.add(tag);
        }
    }

    private void parseKarma(final SparseArray<Comment.Karma> karmaMap, String sourceKarma) {
        if (sourceKarma == null)
            return;
        long time = System.currentTimeMillis();
        Matcher matcher = karmaSourcePattern.matcher(sourceKarma);
        if (matcher.find()) {
            matcher = karmaPattern.matcher(matcher.group(1));
            while (matcher.find()) {
                try {
                    Comment.Karma karma = new Comment.Karma();
                    int commentId = Integer.parseInt(matcher.group(1));
                    karma.setStatus(Integer.parseInt(matcher.group(2)));
                    karma.setCount(Integer.parseInt(matcher.group(5)));
                    //Log.d("SUKA", "parseKarma " + commentId + " : " + karma.getCount());
                    karmaMap.put(commentId, karma);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        Log.e("TIME", "Karma: " + (System.currentTimeMillis() - time));
    }


    private final Pattern idPattern = Pattern.compile("comment-(\\d+)");
    private final Pattern userIdPattern = Pattern.compile("showuser=(\\d+)");

    public Comment parseComments(final SparseArray<Comment.Karma> karmaMap, String source) {
        long time = System.currentTimeMillis();
        Document document = Parser.parse(source);
        Comment comments = new Comment();
        recurseComments(karmaMap, document, comments, 0);

        Log.e("TIME", "Comments: " + (System.currentTimeMillis() - time));
        Log.e("SUKA", "Comments: " + comments.getChildren().size() + " : " + comments.getChildren().get(0).getChildren().size());
        return comments;
    }


    private Comment recurseComments(final SparseArray<Comment.Karma> karmaMap, Node root, Comment parentComment, int level) {
        Node rootComments = Parser.findNode(root, "ul", "class", "comment-list");
        ArrayList<Node> commentNodes = Parser.findChildNodes(rootComments, "li", null, null);

        /*if (commentNodes.size() == 0) {
            return null;
        }*/
        for (Node commentNode : commentNodes) {
            Comment comment = new Comment();

            String id = null, userId = null, userNick = null, date = null, content = null;
            Matcher matcher;
            Node anchorNode = Parser.findNode(commentNode, "div", "id", "comment-");

            id = anchorNode.getAttribute("id");
            if (id != null) {
                matcher = idPattern.matcher(id);
                if (matcher.find()) {
                    id = matcher.group(1);
                    comment.setId(Integer.parseInt(id));
                }
            }

            String deletedString = anchorNode.getAttribute("class");
            boolean isDeleted = deletedString != null && deletedString.contains("deleted");
            comment.setDeleted(isDeleted);

            if (!isDeleted) {
                Node nickNode = Parser.findNode(commentNode, "a", "class", "nickname");
                Node metaNode = Parser.findNode(commentNode, "span", "class", "h-meta");

                userId = nickNode.getAttribute("href");
                if (userId != null) {
                    matcher = userIdPattern.matcher(userId);
                    if (matcher.find()) {
                        userId = matcher.group(1);
                        comment.setUserId(Integer.parseInt(userId));
                    }
                }

                userNick = Parser.getHtml(nickNode, true);
                comment.setUserNick(Utils.fromHtml(userNick));

                date = Parser.ownText(metaNode).trim();
                date = date.replace(" |", ",");
                comment.setDate(date);
            }

            Node contentNode = Parser.findNode(commentNode, "p", "class", "content");
            content = Parser.getHtml(contentNode, true);
            comment.setContent(Utils.fromHtml(content));
            comment.setLevel(level);
            comment.setKarma(karmaMap.get(comment.getId()));

            /*String levelPadding = "";
            for (int i = 0; i < level; i++) {
                levelPadding += "\t";
            }
*/

            //Log.d("SUKA", levelPadding + id + " : " + content);


            parentComment.addChild(comment);

            level++;
            recurseComments(karmaMap, commentNode, comment, level);
            level--;
        }

        return parentComment;
    }

    public ArrayList<Comment> commentsToList(Comment comment) {
        ArrayList<Comment> comments = new ArrayList<>();
        recurseCommentsToList(comments, comment);
        return comments;
    }


    public void recurseCommentsToList(ArrayList<Comment> comments, Comment comment) {
        for (Comment child : comment.getChildren()) {
            comments.add(new Comment(child));
            recurseCommentsToList(comments, child);
        }
    }

    public Boolean likeComment(int articleId, int commentId) throws Exception {
        String url = "http://4pda.ru/wp-content/plugins/karma/ajax.php?p=" + articleId + "&c=" + commentId + "&v=1";
        Api.getWebClient().request(new NetworkRequest.Builder().url(url).xhrHeader().build());
        return true;
    }

    public Comment replyComment(DetailsPage article, int commentId, String comment) throws Exception {
        try {
            comment = URLEncoder.encode(comment, "Windows-1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("https://4pda.ru/wp-comments-post.php")
                .formHeader("comment_post_ID", Integer.toString(article.getId()))
                .formHeader("comment_reply_ID", Integer.toString(commentId))
                .formHeader("comment_reply_dp", commentId == 0 ? "0" : "1")
                .formHeader("comment", comment, true);
        NetworkResponse response = Api.getWebClient().request(builder.build());

        DetailsPage newArticle = parseArticle(article.getId(), response.getBody());

        return updateComments(article, newArticle);
    }

    public Comment updateComments(DetailsPage article, DetailsPage newArticle) {
        article.getKarmaMap().clear();
        article.setKarmaMap(newArticle.getKarmaMap());

        article.setCommentsSource(newArticle.getCommentsSource());
        article.setCommentsCount(newArticle.getCommentsCount());

        Comment newComments = parseComments(article.getKarmaMap(), newArticle.getCommentsSource());
        article.setCommentTree(newComments);
        return newComments;
    }

    private static String getUrlCategory(@Nullable String category) {
        Log.d("SUKA", "getUrlCategory " + category);
        if (category == null) return NEWS_URL_ALL;
        switch (category) {
            case NEWS_CATEGORY_ALL:
                return NEWS_URL_ALL;
            case NEWS_CATEGORY_ARTICLES:
                return NEWS_URL_ARTICLES;
            case NEWS_CATEGORY_REVIEWS:
                return NEWS_URL_REVIEWS;
            case NEWS_CATEGORY_SOFTWARE:
                return NEWS_URL_SOFTWARE;
            case NEWS_CATEGORY_GAMES:
                return NEWS_URL_GAMES;
            case NEWS_SUBCATEGORY_DEVSTORY_GAMES:
                return NEWS_URL_DEVSTORY_GAMES;
            case NEWS_SUBCATEGORY_WP7_GAME:
                return NEWS_URL_WP7_GAME;
            case NEWS_SUBCATEGORY_IOS_GAME:
                return NEWS_URL_IOS_GAME;
            case NEWS_SUBCATEGORY_ANDROID_GAME:
                return NEWS_URL_ANDROID_GAME;
            case NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE:
                return NEWS_URL_DEVSTORY_SOFTWARE;
            case NEWS_SUBCATEGORY_WP7_SOFTWARE:
                return NEWS_URL_WP7_SOFTWARE;
            case NEWS_SUBCATEGORY_IOS_SOFTWARE:
                return NEWS_URL_IOS_SOFTWARE;
            case NEWS_SUBCATEGORY_ANDROID_SOFTWARE:
                return NEWS_URL_ANDROID_SOFTWARE;
            case NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS:
                return NEWS_URL_SMARTPHONES_REVIEWS;
            case NEWS_SUBCATEGORY_TABLETS_REVIEWS:
                return NEWS_URL_TABLETS_REVIEWS;
            case NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS:
                return NEWS_URL_SMART_WATCH_REVIEWS;
            case NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS:
                return NEWS_URL_ACCESSORIES_REVIEWS;
            case NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS:
                return NEWS_URL_NOTEBOOKS_REVIEWS;
            case NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS:
                return NEWS_URL_ACOUSTICS_REVIEWS;
            case NEWS_SUBCATEGORY_HOW_TO_ANDROID:
                return NEWS_URL_HOW_TO_ANDROID;
            case NEWS_SUBCATEGORY_HOW_TO_IOS:
                return NEWS_URL_HOW_TO_IOS;
            case NEWS_SUBCATEGORY_HOW_TO_WP:
                return NEWS_URL_HOW_TO_WP;
            case NEWS_SUBCATEGORY_HOW_TO_INTERVIEW:
                return NEWS_URL_HOW_TO_INTERVIEW;
        }
        return NEWS_URL_ALL;
    }
}
