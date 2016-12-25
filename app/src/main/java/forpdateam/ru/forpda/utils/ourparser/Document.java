package forpdateam.ru.forpda.utils.ourparser;

import android.graphics.Color;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 13.09.16.
 */
public class Document {
    private Element root;
    //private final static Pattern nonClosedTags = Pattern.compile("(<(area|base|br|col|colgroup|command|embed|hr|img|input|keygen|link|meta|param|source|track|wbr)([^>]*?)(/|)>)");
    //private final static Pattern unclosedTags = Pattern.compile("(?:area|area|br|col|colgroup|command|embed|hr|img|input|keygen|link|meta|param|source|track|wbr)");
    private final static Pattern commentTag = Pattern.compile("<!--[\\s\\S]*?-->");
    private final static Pattern scriptBlock = Pattern.compile("");

    private final static Pattern mainPattern = Pattern.compile("<([^\\/!][\\w]*)(?: |)([^>]*)>([^<]*)|<\\/([\\w]*)>([^<]*)");
    private final static Pattern mainPattern2 = Pattern.compile("(?:<([^\\/!][\\w]*)(?: ([^>]*))?>|<\\/([\\w]*)>)(?:([^<]+))?");
    private final static Pattern mainPattern3 = Pattern.compile("(?:<([\\/])?([\\w]*)(?: ([^\\/][^>]*[^\\/]))?( \\/)?>)(?:([^<]+))?");
    private final static Pattern mainPattern4 = Pattern.compile("(?:<([\\/])?([\\w]*)(?: ([^>]*))?\\/?>)(?:([^<]+))?");
    //private final static Pattern attrPattern = Pattern.compile("([^ \"]*?)=[\"']([^\"']*)[\"']");
    private final static String[] uTags = {"area", "area", "br", "col", "colgroup", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

    public static Document parse(String html) {
        long time;
        time = System.currentTimeMillis();

        //html = commentTag.matcher(html).replaceAll("");
        //html = scriptBlock.matcher(html).replaceAll("");
        StringBuilder sb = new StringBuilder();
        /*for(String s :html.split("<!--[\\s\\S]*?-->")){
            sb.append(s);
        }
        html = sb.toString();
        sb = new StringBuilder();*/
        for(String s :html.split("<script[^>]*>[\\s\\S]*?</script>")){
            sb.append(s);
        }
        html = sb.toString();

        Log.e("myparser", "check1 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        int level = 0;
        Document document = new Document();
        Matcher matcher = mainPattern.matcher(html);
        Log.e("myparser", "check2 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        Element last = null;
        ArrayList<Element> lasts = new ArrayList<>();
        Element element;
        String tempTag;
        boolean lastNotNull = false;
        int i = 0;
        Log.e("myparser", "check3 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        while (matcher.find()) {
            i++;
            if (lasts.size() > 0) {
                last = lasts.get(lasts.size() - 1);
                lastNotNull = true;
            }
            tempTag = matcher.group(1);
            if (tempTag != null) {
                element = new Element(tempTag, matcher.group(2));
                element.setText(matcher.group(3));
                element.setLevel(level);
                if (lastNotNull)
                    element.setParent(last.getLevel() == element.getLevel() ? last.getParent() : last);

                document.add(element);
                if (!containsInUTag(element.tagName())) {
                    lasts.add(element);
                    level++;
                }
            } else {
                if (lasts.size() > 0 && lastNotNull) {
                    if (last.tagName().equals(matcher.group(4)))
                        last.setAfterText(matcher.group(5));
                    lasts.remove(lasts.size() - 1);
                }
                level--;
            }
        }
        Log.e("myparser", "check4 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        Log.d("myparser", "asdf : " + i + " : " + lasts.size() + " : " + level);
        //Log.d("myparser", "parsed : "+document.html());
        return document;
    }

    private static boolean containsInUTag(String tag) {
        for (String uTag : uTags)
            if (uTag.compareTo(tag) == 0) return true;

        return false;
    }

    public void add(Element children) {
        if (children.getLevel() == 0) {
            root = children;
            return;
        }
        findToAdd(root, children);
    }

    private void findToAdd(Element root, Element children) {
        if (children.getLevel() - 1 == root.getLevel()) {
            root.add(children);
        } else {
            findToAdd(root.getLast(), children);
        }
    }

    public Element getRoot() {
        return root;
    }

    public String html() {
        return root.html();
    }

    public String htmlNoParent() {
        return root.htmlNoParent();
    }

    public String getAllText() {
        return root.getAllText();
    }
}

