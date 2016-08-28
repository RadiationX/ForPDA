package forpdateam.ru.forpda.test.regexparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by radiationx on 26.08.16.
 */
public class Element {
    private List<Element> elements = new ArrayList<>();
    private Element parent;
    private HashMap<String, String> attributes = new HashMap<>();
    private String text = "";
    private String afterText = "";
    private String tag = "";
    private int level = 0;

    public Element(String tag, int level) {
        this.tag = tag;
        this.level = level;
    }

    public Element(String tag) {
        this.tag = tag;
    }

    public int getSize() {
        return elements.size();
    }

    public Element get(int i) {
        return elements.get(i);
    }

    public Element getLast() {
        if (elements.size() == 0)
            return null;
        return elements.get(elements.size() - 1);
    }

    public void add(Element element) {
        elements.add(element);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTag() {
        return tag;
    }

    public void addAttr(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttr(String key) {
        return attributes.get(key);
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setAfterText(String afterText) {
        this.afterText = afterText;
    }

    public String getAfterText() {
        return afterText;
    }

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public Element getParent() {
        return parent;
    }

    public String getHtml() {
        return getHtml(this);
    }

    public String getHtml(Element element) {
        String html = "";
        if (!element.getTag().matches("a|meta|p|span|img")) {
            html = html.concat("\n");
            for (int k = 0; k < element.getLevel(); k++)
                html = html.concat("\t");
        }

        html = html.concat("<").concat(element.getTag());
        if (!element.getTag().matches("br|img"))
            html = html.concat(" class=\"").concat(element.getAttr("class")==null?"null":element.getAttr("class")).concat("\"");
        html = html.concat(">");
        if (!element.getText().isEmpty()) {
            html = html.concat(element.getText());
        }

        for (int i = 0; i < element.getSize(); i++) {
            html = html.concat(getHtml(element.get(i)));
        }

        if (!element.getTag().matches("br|meta|a|p|span|img")) {
            html = html.concat("\n");
            for (int k = 0; k < element.getLevel(); k++)
                html = html.concat("\t");
        }
        if (!element.getTag().matches("br|img|meta")) {
            html = html.concat("</").concat(element.getTag()).concat(">");
        }
        if (!element.getAfterText().isEmpty()) {
            html = html.concat(element.getAfterText());
        }
        return html;
    }

    public String getAllText() {
        return getAllText(this);
    }

    private final static String probel = " ";

    public String getAllText(Element element) {
        String text = "";
        //text+=" "+element.getText();
        text = text.concat(probel).concat(element.getText());

        for (int i = 0; i < element.getSize(); i++) {
            text = text.concat(getAllText(element.get(i)));
        }
        text = text.concat(probel).concat(element.getAfterText());

        return text;
    }

}
