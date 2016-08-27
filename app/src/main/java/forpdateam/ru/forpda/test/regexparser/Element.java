package forpdateam.ru.forpda.test.regexparser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by radiationx on 26.08.16.
 */
public class Element {
    private ArrayList<Element> elements = new ArrayList<>();
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

    public Element getParent(){
        return parent;
    }
}
