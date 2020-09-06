package forpdateam.ru.forpda.model.data.remote.api.regex.parser;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 13.08.17.
 */

public class Parser {
    private final static int S_TAG = 1;
    private final static int S_ATTRS = 2;
    private final static int S_TEXT = 3;
    private final static int CLOSING = 4;
    private final static int TAG = 5;
    private final static int ATTRS = 6;
    private final static int TEXT = 7;
    /*
    * GROUPS
    *
    * script/style/textarea/etc:
    * 1. Tag name
    * 2. Attributes
    * 3. Inner text
    *
    * basic:
    * 4. Close tag symbol "/"
    * 5. Tag name
    * 6. Attributes
    * 7. Text
    *
    * if no groups - comment
    * */

    //private final static Pattern NON_CLOSING_TAGS = Pattern.compile("!DOCTYPE|colgroup|command|keygen|source|embed|input|param|track|area|link|meta|col|img|wbr|br|hr", Pattern.CASE_INSENSITIVE);

    private static Pattern mainPattern = null;
    private static Pattern attributePattern;
    private static String[] uTags;

    public static Pattern getMainPattern() {
        if(mainPattern==null)
            mainPattern = Pattern.compile("\\<(?:(?:(script|style|textarea)(?:([^\\>]+))?\\>)([\\s\\S]*?)(?:\\<\\/\\1)|([\\/])?(!?[\\w]*)(?:([^\\>]+))?\\/?)\\>(?:([^<]+))?", Pattern.CASE_INSENSITIVE);
        return mainPattern;
    }

    public static Pattern getAttributePattern() {
        if(attributePattern==null)
            attributePattern = Pattern.compile("([^ \"']*?)\\s*?=\\s*?([\"'])([\\s\\S]*?)\\2", Pattern.CASE_INSENSITIVE);
        return attributePattern;
    }

    public static String[] getuTags() {
        if(uTags==null)
            uTags = new String[]{"!doctype", "area", "br", "col", "colgroup", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};
        return uTags;
    }

    private static boolean containsInUTag(String tag) {
        for (String uTag : getuTags())
            if (uTag.equalsIgnoreCase(tag)) return true;
        return false;
    }

    public static Matcher getMatcher(Matcher m, Pattern p, String s) {
        return m == null ? p.matcher(s) : m.reset(s);
    }

    public static Document parse(String html) {
        final ArrayList<Node> openedNodes = new ArrayList<>();
        final Document root = new Document();

        openedNodes.add(root);
        Node lastOpened = null;

        final Matcher matcher = getMainPattern().matcher(html);
        Matcher ncMatcher = null;
        Matcher attrMatcher = null;
        int nodesAdd = 0, nodesClose = 0;
        while (matcher.find()) {
            lastOpened = openedNodes.get(openedNodes.size() - 1);
            final Node node = new Node();


            boolean special = false;
            String tagName = matcher.group(TAG);
            if (tagName == null) {
                special = true;
            }


            boolean openAction = matcher.group(CLOSING) == null;


            if (openAction) {
                if (special) {
                    tagName = matcher.group(S_TAG);
                    special = tagName != null;
                }
                String attrs = matcher.group(special ? S_ATTRS : ATTRS);
                String text = matcher.group(special ? S_TEXT : TEXT);
                // Log.d("PARSER", "Open last= " + lastOpened + "; new= " + tagName + "; text= '" + text + "'");
                boolean addToOpened = true;
                if (tagName == null) {
                    if (text == null) {
                        node.setName(Node.NODE_COMMENT);
                        node.setText(matcher.group());
                    }
                    addToOpened = false;
                } else {
                    node.setName(tagName);

                    if (attrs != null) {
                        attrMatcher = getMatcher(attrMatcher, getAttributePattern(), attrs);
                        while (attrMatcher.find()) {
                            node.putAttribute(attrMatcher.group(1), attrMatcher.group(3));
                        }
                    }

                    //ncMatcher = getMatcher(ncMatcher, NON_CLOSING_TAGS, tagName);
                    if (containsInUTag(tagName)) {
                        if (tagName.equalsIgnoreCase(Document.DOCTYPE_TAG)) {
                            root.setDocType(attrs);
                        }
                        addToOpened = false;
                    }
                    if (text != null) {
                        if (special) {
                            addToOpened = false;
                        }
                        Node textNode = new Node(Node.NODE_TEXT);
                        textNode.setText(text);
                        node.addNode(textNode);
                        nodesAdd++;
                    }

                }

                lastOpened.addNode(node);
                //Log.d("PARSER", "ADD? = " + addToOpened);
                nodesAdd++;
                if (addToOpened) {
                    openedNodes.add(node);
                }
            } else {
                //Log.e("PARSER", "Close last = " + lastOpened);
                openedNodes.remove(lastOpened);
                nodesClose++;
            }

        }
        openedNodes.remove(root);

        Log.d("SUKA", "FINAL OPENED " + openedNodes.size() + " : " + nodesAdd + " : " + nodesClose);

        return root;
    }

    public static boolean isNotElement(Node node) {
        return node.getName() == null || node.getName().equals(Node.NODE_TEXT) || node.getName().equals(Node.NODE_COMMENT);
    }

    public static boolean isTextNode(Node node) {
        return node.getName().equals(Node.NODE_TEXT);
    }

    public static String getHtml(Document document, Node node, Matcher matcher) {
        StringBuilder resultHtml = new StringBuilder();
        boolean onlyText = isNotElement(node);

        if (onlyText) {
            resultHtml.append(node.getText());
        } else {
            resultHtml.append("<").append(node.getName());
            if (node.getAttributes() != null) {
                for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
                    resultHtml.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
                }
            } else {
                if (node.getName().equalsIgnoreCase(Document.DOCTYPE_TAG)) {
                    resultHtml.append(" ").append(document.getDocType());
                }
            }
            resultHtml.append(">");
        }

        if (!onlyText) {
            if (node.getNodes() != null) {
                for (Node child : node.getNodes()) {
                    String s = getHtml(document, child, matcher);
                    resultHtml.append(s);
                }
            }
        }


        if (!onlyText) {
            if (!containsInUTag(node.getName())) {
                resultHtml.append("</").append(node.getName()).append(">");
            }
        }


        return resultHtml.toString();
    }

    public static String getHtml(Node node, boolean onlyInner) {
        if (isNotElement(node)) {
            return node.getText();
        }
        StringBuilder resultHtml = new StringBuilder();

        if (!onlyInner) {
            resultHtml.append("<").append(node.getName());
            if (node.getAttributes() != null) {
                for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
                    resultHtml.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
                }
            }
            resultHtml.append(">");
        }


        if (node.getNodes() != null) {
            for (Node child : node.getNodes()) {
                String s = getHtml(child, false);
                resultHtml.append(s);
            }
        }


        if (!onlyInner) {
            if (!containsInUTag(node.getName())) {
                resultHtml.append("</").append(node.getName()).append(">");
            }
        }


        return resultHtml.toString();
    }


    public static Node findNode(Node node, String tag, String attr, String value) {
        if (isNotElement(node)) {
            return null;
        }
        if (node.getName().equalsIgnoreCase(tag)) {
            if (attr == null) {
                return node;
            }
            String attrValue = node.getAttributes().get(attr);
            if (attrValue != null && attrValue.contains(value)) {
                return node;
            }
        }
        Node result = null;
        for (Node child : node.getNodes()) {
            result = findNode(child, tag, attr, value);
            if (result != null)
                break;
        }
        return result;
    }

    public static ArrayList<Node> findChildNodes(Node node, String tag, String attr, String value) {
        ArrayList<Node> result = new ArrayList<>();
        if (isNotElement(node)) {
            return result;
        }

        for (Node child : node.getNodes()) {
            if (isNotElement(child))
                continue;
            if (child.getName().equalsIgnoreCase(tag)) {
                if (attr == null) {
                    result.add(child);
                    continue;
                }
                String attrValue = child.getAttributes().get(attr);
                if (attrValue != null && attrValue.contains(value)) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    public static String ownText(Node node) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Node child : node.getNodes()) {
            if (isTextNode(child)) {
                stringBuilder.append(child.getText());
            }

        }
        return stringBuilder.toString();
    }

}

