package forpdateam.ru.forpda.utils.ourparser;

import android.util.Log;

import com.nostra13.universalimageloader.utils.L;

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
    private HashMap<String, String> attributes = new HashMap<>();
    private String text = "";
    private String afterText = "";
    private String tagName = "";
    private int level = 0;

    public Element(String tagName, int level) {
        this.tagName = tagName;
        this.level = level;
    }

    public Element(String tagName) {
        this.tagName = tagName;
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

    public List<Element> getElements(){
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

    public void addAttr(String key, String value) {
        attributes.put(key, value);
    }

    public String attr(String key) {
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

    public String html() {
        return html(this, true);
    }
    public String htmlNoParent() {
        return html(this, false);
    }

    Pattern pattern = Pattern.compile("br|img|meta");
    Matcher matcher;
    public String html(Element element, boolean withParent) {
        String html = "";
        /*if (!element.tagName().matches("a|meta|p|span|img")) {
            html = html.concat("\n");
            for (int k = 0; k < element.getLevel(); k++)
                html = html.concat("\t");
        }*/

        if(withParent){
            html = html.concat("<").concat(element.tagName());
            for(Map.Entry<String, String> entry : element.getAttributes().entrySet()) {
                html = html.concat(" ").concat(entry.getKey()).concat("=\"").concat(entry.getValue()).concat("\"");
            }
            html = html.concat(">");
        }

        if (!element.getText().isEmpty()) {
            //html = html.concat(probel);
            html = html.concat(element.getText());
        }

        for (int i = 0; i < element.getSize(); i++) {
            html = html.concat(html(element.get(i), true));
        }

        /*if (!element.tagName().matches("br|meta|a|p|span|img")) {
            html = html.concat("\n");
            for (int k = 0; k < element.getLevel(); k++)
                html = html.concat("\t");
        }*/
        if(withParent){
            matcher = pattern.matcher(element.tagName());
            if (!matcher.matches()) {
                html = html.concat("</").concat(element.tagName()).concat(">");
            }
            if (!element.getAfterText().isEmpty()) {
                html = html.concat(probel);
                html = html.concat(element.getAfterText());
            }
        }

        html = html.concat(probel);
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

    public static void fixSpace(Element element){
        element.setText(element.getText().replaceAll(" ", "&nbsp;"));
        for (int i = 0; i < element.getSize(); i++)
            fixSpace(element.get(i));
        element.setAfterText(element.getAfterText().replaceAll(" ", "&nbsp;"));
    }


    public Element selectLink(){
        for(Element element:elements){
            if(element.tagName().equals("a"))
                return element;
        }
        return null;
    }


    public String ownText(){
        String text = getText();
        for(Element element:elements){
            text+=" "+element.getAfterText();
        }
        return text.trim();
    }
}
