package forpdateam.ru.forpda.test.regexparser;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 26.08.16.
 */
public class ParserExample {

    String html;
    ArrayList<Element> opened = new ArrayList<>();
    ArrayList<String> closed = new ArrayList<>();

    private void func(){
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
            text += element.get(i).getTag()+" "+element.get(i).getAttributes().values().toArray()[0];
            text += "\n";
            text += recurseNode(element.get(i));
        }
        return text;
    }
}
