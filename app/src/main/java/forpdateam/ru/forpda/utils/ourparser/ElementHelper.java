package forpdateam.ru.forpda.utils.ourparser;

import android.util.Pair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 04.12.16.
 */

public class ElementHelper {
    private final static Pattern pattern = Pattern.compile("br|img|meta");
    private final static Pattern attrPattern = Pattern.compile("([^ \"]*?)=[\"']([^\"']*)[\"']");

    public static String html(Element element, boolean withParent) {
        String html = "";
        /*if (!element.tagName().matches("a|meta|p|span|img")) {
            html = html.concat("\n");
            for (int k = 0; k < element.getLevel(); k++)
                html = html.concat("\t");
        }*/

        if (withParent) {
            html = html.concat("<").concat(element.tagName());
            for (Pair<String, String> pair : element.getAttributes()) {
                html = html.concat(" ").concat(pair.first).concat("=\"").concat(pair.second).concat("\"");
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
        if (withParent) {
            Matcher matcher = pattern.matcher(element.tagName());
            if (!matcher.matches()) {
                html = html.concat("</").concat(element.tagName()).concat(">");
            }
            if (!element.getAfterText().isEmpty()) {
                html = html.concat(" ");
                html = html.concat(element.getAfterText());
            }
        }

        html = html.concat(" ");
        return html;
    }



    public static String getAllText(Element element) {
        String text = "";
        //text+=" "+element.getText();
        text = text.concat(" ").concat(element.getText());

        for (int i = 0; i < element.getSize(); i++) {
            text = text.concat(getAllText(element.get(i)));
        }
        text = text.concat(" ").concat(element.getAfterText());

        return text;
    }

    public static void fixSpace(Element element) {
        element.setText(element.getText().replaceAll(" ", "&nbsp;"));
        for (int i = 0; i < element.getSize(); i++)
            fixSpace(element.get(i));
        element.setAfterText(element.getAfterText().replaceAll(" ", "&nbsp;"));
    }


    public static List<Pair<String,String>> parseAttrs(String source, List<Pair<String,String>> attrs){
        if (!source.isEmpty()) {
            Matcher attrMatcher = attrPattern.matcher(source);
            while (attrMatcher.find())
                attrs.add(new Pair<>(attrMatcher.group(1), attrMatcher.group(2)));
        }
        return attrs;
    }
}
