package forpdateam.ru.forpda.api.news.html;

public class HtmlBuilder {
    protected StringBuilder html;

    public HtmlBuilder() {
        this.html = new StringBuilder();
    }

    public void beginHtml(String title) {
        html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        html.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        html.append("<head>\n");
        html.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n");
        if (html != null) {
            addStyleSheetLink(html);
        }
        html.append("<title>").append(title).append("</title>\n");
        html.append("</head>\n");
    }

    public void addStyleSheetLink(StringBuilder sb) {
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
    }

    public HtmlBuilder append(String str) {
        html.append(str);
        return this;
    }

    public void beginBody(String id, boolean isImage) {

    }

    public HtmlBuilder endBody() {
        html.append("</body>\n");
        return this;
    }

    public void endHtml() {
        html.append("</html>");
    }
}
