package forpdateam.ru.forpda.utils.ourparser;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 13.09.16.
 */
public class Element {
    private List<Element> elements = new ArrayList<>();
    private Element parent;
    private List<Pair<String, String>> attributes = null;
    private String text = "";
    private String afterText = "";
    private String tagName = "";
    private int level = 0;
    private String attrsSource = "";

    public Element(String tagName, int level) {
        this.tagName = tagName;
        this.level = level;
    }

    public Element(String tagName) {
        this.tagName = tagName;
    }

    public Element(String tagName, String attrs) {
        this.tagName = tagName;
        this.attrsSource = attrs;
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

    public List<Element> getElements() {
        return elements;
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

    public String tagName() {
        return tagName;
    }

    /*public void addAttr(String key, String value) {
        attributes.add(new Pair<>(key, value));
    }*/

    public String attr(String key) {
        for (Pair<String, String> p : getAttributes()) {
            if (p.first.compareTo(key) == 0) {
                return p.second;
            }
        }
        return null;
    }

    public List<Pair<String, String>> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<>();
            ElementHelper.parseAttrs(attrsSource, attributes);
        }
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

    public String html() {
        return ElementHelper.html(this, true);
    }

    public String htmlNoParent() {
        return ElementHelper.html(this, false);
    }

    public String getAllText() {
        return ElementHelper.getAllText(this);
    }

    public void fixSpace() {
        ElementHelper.fixSpace(this);
    }

    public String ownText() {
        String text = getText();
        for (Element element : elements) {
            text += " " + element.getAfterText();
        }
        return text.trim();
    }
}
