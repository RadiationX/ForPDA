package forpdateam.ru.forpda.api.news.html;

import forpdateam.ru.forpda.App;

public class NewsHtmlBuilder extends HtmlBuilder {

    public static String transformBody(String origPage) {
        NewsHtmlBuilder builder = new NewsHtmlBuilder();
        builder.beginHtml("News");
        builder.beginBody("news", true);
        builder.append("<div id=\"news\">");
        origPage = origPage.replace("\"//", "\"http://");
        builder.append(origPage);
        builder.append("</div>");
        builder.endBody();
        builder.endHtml();
        return builder.html.toString();
    }

    @Override
    public void addStyleSheetLink(StringBuilder sb) {
        super.addStyleSheetLink(sb);
        if (App.getInstance().isDarkTheme()) {
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/news/news_dark.css\"/>\n");
        } else {
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/news/news_light.css\"/>\n");
        }
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/news/youtube_video.css\"/>\n");

    }
}
