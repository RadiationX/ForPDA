package forpdateam.ru.forpda.api.regex.parser;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by radiationx on 13.08.17.
 */

public class Node {
    public final static String NODE_DOCUMENT = "#document";
    public final static String NODE_TEXT = "#text";
    public final static String NODE_COMMENT = "#comment";

    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> elements = new ArrayList<>();
    private LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    private String name = null;
    private String text = null;

    public Node() {
    }

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public ArrayList<Node> getElements() {
        return elements;
    }

    public void addElement(Node node) {
        this.elements.add(node);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "" + name;
    }


    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public void putAttribute(String name, String value) {
        this.attributes.put(name, value);
    }
    @Nullable
    public String getAttribute(String attr){
        return attributes.get(attr);
    }
}
