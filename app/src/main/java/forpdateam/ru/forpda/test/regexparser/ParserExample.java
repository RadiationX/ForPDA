package forpdateam.ru.forpda.test.regexparser;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 26.08.16.
 */
public class ParserExample {

    String html =
            "<div class=\"message\">\n" +
                    "\t<h1 class=\"title\">Attention!</h1>\n" +
                    "\t<div class=\"content\">This site is under construction.\n" +
                    "\t\t<p>Real content coming soon.</p>\n" +
                    "\t\tcontent after\n" +
                    "\t\t<p>Real content coming soon.</p>\n" +
                    "\t</div>\n" +
                    "</div>";
    ArrayList<Element> opened = new ArrayList<>();
    ArrayList<String> closed = new ArrayList<>();

    boolean replaced = false;
    private void func2() {
        if (!replaced) {
            html = html.replaceAll("(<(area|base|br|col|colgroup|command|embed|hr|img|input|keygen|link|meta|param|source|track|wbr)[^>]*?>)", "$1</$2>");
            replaced = true;
        }
        final Pattern mainPattern = Pattern.compile("<([^/!][\\w]*)([^>]*)>([^<]*)|</([\\w]*)>([^<]*)");
        final Pattern attrPattern = Pattern.compile("([^ \"]*?)=\"([^\"]*?)\"");

        Document document = new Document();
        int level = 0;
        Matcher matcher = mainPattern.matcher(html);
        Matcher attrMatcher;
        Element last = null;
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                Element element = new Element(matcher.group(1));

                attrMatcher = attrPattern.matcher(matcher.group(2));
                while (attrMatcher.find())
                    element.addAttr(attrMatcher.group(1), attrMatcher.group(2));

                element.setText(matcher.group(3).trim());
                element.setLevel(level);
                if (last != null)
                    element.setParent(last.getLevel() == element.getLevel() ? last.getParent() : last);

                document.add(element);

                level++;
                last = element;
            } else {
                if (last != null && last.getTag().equals(matcher.group(4)))
                    last.setAfterText(matcher.group(5).trim());

                level--;
            }
        }
        Log.d("kek", "recurse\n" + document.getAllText());

        //Log.d("kek", "recurse\n" + document.getRoot().get(1).get(1).getAllText());
    }


    private Document recurse(int level) {
        Document document = new Document();
        ArrayList<String> unclosed = new ArrayList<>();
        for (int i = 0, j = 0; i < opened.size(); i++) {
            opened.get(i).setLevel(level);
            document.add(opened.get(i));
            if (opened.get(i).getTag().equals(closed.get(j))) {
                j++;
                for (int k = unclosed.size() - 1; k >= 0; k--) {
                    if (unclosed.get(k).equals(closed.get(j))) {
                        level--;
                        j++;
                        unclosed.remove(k);
                    }
                }
            } else {
                level++;
                unclosed.add(opened.get(i).getTag());
            }
        }
        Log.d("kek", "unclosed final " + level + " : " + unclosed.size());
        Log.d("kek", "document size " + document.getRoot().getSize());
        Log.d("kek", "document size " + document.getRoot().get(0).getSize());
        Log.d("kek", "document size " + document.getRoot().get(0).get(0).getTag());
        Log.d("kek", "document size " + document.getRoot().get(0).get(1).getTag());
        Log.d("kek", "document size " + document.getRoot().get(0).get(1).get(0).getTag());
        Log.d("kek", "document size " + document.getRoot().get(0).get(1).get(1).getTag());
        Log.d("kek", "document size " + document.getRoot().get(0).get(1).get(1).get(0).getTag());
        Log.d("kek", "document size " + document.getRoot().get(1).getSize());
        Log.d("kek", "document \n" + recurseNode(document.getRoot()));
        return document;
    }

    private String recurseNode(Element element) {
        String text = "";
        for (int i = 0; i < element.getSize(); i++) {
            text += element.getLevel() + " ";
            for (int k = 0; k < element.getLevel(); k++)
                text += "_";
            text += element.get(i).getTag() + " " + element.get(i).getAttributes().values().toArray()[0];
            text += "\n";
            text += recurseNode(element.get(i));
        }
        return text;
    }

    private void func() {
        long time = System.currentTimeMillis();
        html = html.replaceAll("(<(area|base|br|col|colgroup|command|embed|hr|img|input|keygen|link|meta|param|source|track|wbr)[^>]*?>)", "$1</$2>");
        System.out.print(html);
        Matcher matcher = Pattern.compile("<([^/>]*?) ([^>]*?)>([^<]*)").matcher(html);
        while (matcher.find()) {
            Element element = new Element(matcher.group(1));
            Matcher attrMatcher = Pattern.compile("([^ \"]*?)=\"([^\"]*?)\"").matcher(matcher.group(2));
            while (attrMatcher.find()) {
                element.addAttr(attrMatcher.group(1), attrMatcher.group(2));
            }
            element.setText(matcher.group(3).trim());
            opened.add(element);
        }
        matcher.usePattern(Pattern.compile("</([^>]*?)>"));
        while (matcher.find()) {
            closed.add(matcher.group(1));
        }

        /*Log.d("kek", "sizes " + opened.size() + " : " + closed.size());
        for (int i = 0; i < Math.min(opened.size(), closed.size()); i++) {
            Log.d("kek", "compare " + opened.get(i) + " : " + closed.get(i));
        }*/

        Log.d("kek", "recurse\n" + recurseNode(recurse(0).getRoot()));
        Log.d("kek", "time " + (System.currentTimeMillis() - time));
    }
}
