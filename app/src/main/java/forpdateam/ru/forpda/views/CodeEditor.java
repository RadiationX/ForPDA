package forpdateam.ru.forpda.views;

/**
 * Created by radiationx on 27.07.17.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.utils.Html;

/*
* ORIGINAL: https://github.com/markusfisch/ShaderEditor/blob/master/app/src/main/java/de/markusfisch/android/shadereditor/widget/ShaderEditor.java
* */
public class CodeEditor extends AppCompatEditText {
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    private static class ForumCodes {
        private static final Pattern ELEMENT = Pattern.compile("(\\[(?:\\/)?((?:attachment|background|nomergetime|mergetime|snapback|numlist|spoiler|offtop|center|color|right|quote|code|hide|left|list|size|sub|sup|cur|url|b|i|u|s|\\*)))([^\\]\\[]+?)?(\\])", Pattern.CASE_INSENSITIVE);
        private static final Pattern ATTRIBUTE = Pattern.compile("(name|date|post)?=(\"[^\\\"]*?\"|[\\w\\d]+)", Pattern.CASE_INSENSITIVE);

    }

    private static final Pattern PATTERN_LINE = Pattern.compile(
            ".*\\n");
    private static final Pattern PATTERN_TRAILING_WHITE_SPACE = Pattern.compile(
            "[\\t ]+$",
            Pattern.MULTILINE);
    private static final Pattern PATTERN_INSERT_UNIFORM = Pattern.compile(
            "^([ \t]*uniform.+)$",
            Pattern.MULTILINE);
    private static final Pattern PATTERN_ENDIF = Pattern.compile(
            "(#endif)\\b");

    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();

            if (onTextChangedListener != null) {
                onTextChangedListener.onTextChanged(
                        e.toString());
            }

            highlightWithoutChange(e);
        }
    };

    private OnTextChangedListener onTextChangedListener;
    private int updateDelay = 1000;
    private int errorLine = 0;
    private boolean dirty = false;
    private boolean modified = true;
    private int colorError;
    private int colorTag;
    private int colorAttrName;
    private int colorAttrValue;
    private int tabWidthInCharacters = 0;
    private int tabWidth = 0;

    public CodeEditor(Context context) {
        super(context);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        onTextChangedListener = listener;
    }

    public void setUpdateDelay(int ms) {
        updateDelay = ms;
    }

    public void setTabWidth(int characters) {
        if (tabWidthInCharacters == characters) {
            return;
        }

        tabWidthInCharacters = characters;
        tabWidth = Math.round(getPaint().measureText("m") * characters);
    }

    public boolean hasErrorLine() {
        return errorLine > 0;
    }

    public void setErrorLine(int line) {
        errorLine = line;
    }

    public void updateHighlighting() {
        highlightWithoutChange(getText());
    }

    public boolean isModified() {
        return dirty;
    }

    public void setTextHighlighted(CharSequence text) {
        if (text == null) {
            text = "";
        }

        cancelUpdate();

        errorLine = 0;
        dirty = false;

        modified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        modified = true;

        if (onTextChangedListener != null) {
            onTextChangedListener.onTextChanged(text.toString());
        }
    }

    public String getCleanText() {
        return PATTERN_TRAILING_WHITE_SPACE
                .matcher(getText())
                .replaceAll("");
    }

    public void insertTab() {
        int start = getSelectionStart();
        int end = getSelectionEnd();

        getText().replace(
                Math.min(start, end),
                Math.max(start, end),
                "\t",
                0,
                1);
    }

    public void addUniform(String statement) {
        if (statement == null) {
            return;
        }

        Editable e = getText();
        Matcher m = PATTERN_INSERT_UNIFORM.matcher(e);
        int start = -1;

        while (m.find()) {
            start = m.end();
        }

        if (start > -1) {
            // add line break before statement because it's
            // inserted before the last line-break
            statement = "\n" + statement;
        } else {
            // add a line break after statement if there's no
            // uniform already
            statement += "\n";

            // add an empty line between the last #endif
            // and the now following uniform
            if ((start = endIndexOfLastEndIf(e)) > -1) {
                statement = "\n" + statement;
            }

            // move index past line break or to the start
            // of the text when no #endif was found
            ++start;
        }

        e.insert(start, statement);
    }

    private int endIndexOfLastEndIf(Editable e) {
        Matcher m = PATTERN_ENDIF.matcher(e);
        int idx = -1;

        while (m.find()) {
            idx = m.end();
        }

        return idx;
    }

    private void init(Context context) {
        //setHorizontallyScrolling(true);

        /*setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(
                    CharSequence source,
                    int start,
                    int end,
                    Spanned dest,
                    int dstart,
                    int dend) {
                if (modified &&
                        end - start == 1 &&
                        start < source.length() &&
                        dstart < dest.length()) {
                    char c = source.charAt(start);

                    if (c == '\n') {
                        return autoIndent(source, dest, dstart, dend);
                    }
                }

                return source;
            }
        }});*/

        addTextChangedListener(new TextWatcher() {
            private int start = 0;
            private int count = 0;

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
                this.start = start;
                this.count = count;
            }

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();
                //convertTabs(e, start, count);

                if (!modified) {
                    return;
                }

                dirty = true;
                updateHandler.postDelayed(updateRunnable, updateDelay);
            }
        });

        setSyntaxColors(context);
        /*setUpdateDelay(ShaderEditorApp
                .preferences
                .getUpdateDelay());
        setTabWidth(ShaderEditorApp
                .preferences
                .getTabWidth())*/
        setUpdateDelay(1000);
        setTabWidth(4);
        updateHighlighting();
    }

    private void setSyntaxColors(Context context) {
        colorError = Color.RED;
        colorTag = Color.parseColor("#446FBD");
        colorAttrName = Color.parseColor("#6D8600");
        colorAttrValue = Color.parseColor("#e88501");
    }

    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void highlightWithoutChange(Editable e) {
        modified = false;
        highlight(e);
        modified = true;
    }

    private Editable highlight(Editable e) {
        long time = System.currentTimeMillis();
        try {
            // don't use e.clearSpans() because it will
            // remove too much
            clearSpans(e);

            if (e.length() == 0) {
                return e;
            }

            if (errorLine > 0) {
                Matcher m = PATTERN_LINE.matcher(e);

                for (int i = errorLine; i-- > 0 && m.find(); ) {
                    // {} because analyzers don't like for (); statements
                }

                e.setSpan(
                        new BackgroundColorSpan(colorError),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (Matcher m = ForumCodes.ELEMENT.matcher(e), attributes = null; m.find(); ) {
                String tagName = m.group(2);
                String attrsSrc = m.group(3);
                if (attrsSrc != null) {
                    int eg3s = m.start(3);
                    int eg3e = m.end(3);
                    if (attributes == null) {
                        attributes = ForumCodes.ATTRIBUTE.matcher(attrsSrc);
                    } else {
                        attributes = attributes.reset(attrsSrc);
                    }


                    while (attributes.find()) {
                        String attrName = attributes.group(1);
                        if (attrName != null) {
                            int ag1s = attributes.start(1);
                            int ag1e = attributes.end(1);
                            e.setSpan(
                                    new ForegroundColorSpan(colorAttrName),
                                    eg3s + ag1s,
                                    eg3s + ag1e,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        String attrValue = attributes.group(2);
                        if (attrValue != null) {
                            int ag2s = attributes.start(2);
                            int ag2e = attributes.end(2);
                            int color = colorAttrValue;
                            if (attrName == null) {
                                if (tagName.equalsIgnoreCase("spoiler") || tagName.equalsIgnoreCase("code")) {
                                    e.setSpan(
                                            new ForegroundColorSpan(color),
                                            eg3s + 1,
                                            eg3e,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    break;
                                } else if (tagName.equalsIgnoreCase("color") || tagName.equalsIgnoreCase("background")) {

                                    try {
                                        if (attrValue.charAt(0) != '#') {
                                            Integer htmlColor = Html.getColorMap().get(attrValue);
                                            if (htmlColor != null) {
                                                color = htmlColor;
                                            }
                                        } else {
                                            color = Color.parseColor(attrValue);
                                        }
                                    } catch (Exception ignore) {
                                    }
                                }
                            }

                            e.setSpan(
                                    new ForegroundColorSpan(color),
                                    eg3s + ag2s,
                                    eg3s + ag2e,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }


                    }
                }

                int eg1s = m.start(1);
                int eg1e = m.end(1);
                e.setSpan(
                        new ForegroundColorSpan(colorTag),
                        eg1s,
                        eg1e,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                int eg4s = m.start(4);
                int eg4e = m.end(4);
                e.setSpan(
                        new ForegroundColorSpan(colorTag),
                        eg4s,
                        eg4e,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        } catch (IllegalStateException ex) {
            // raised by Matcher.start()/.end() when
            // no successful match has been made what
            // shouldn't ever happen because of find()
        }

        Log.d("SUKA", "TIME: " + (System.currentTimeMillis() - time));
        return e;
    }

    private static void clearSpans(Editable e) {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    ForegroundColorSpan.class);

            for (int i = spans.length; i-- > 0; ) {
                e.removeSpan(spans[i]);
            }
        }

        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    BackgroundColorSpan.class);

            for (int i = spans.length; i-- > 0; ) {
                e.removeSpan(spans[i]);
            }
        }
    }

    private CharSequence autoIndent(
            CharSequence source,
            Spanned dest,
            int dstart,
            int dend) {
        String indent = "";
        int istart = dstart - 1;

        // find start of this line
        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n') {
                break;
            }

            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    // indent always after those characters
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=') {
                        --pt;
                    }

                    dataBefore = true;
                }

                // parenthesis counter
                if (c == '(') {
                    --pt;
                } else if (c == ')') {
                    ++pt;
                }
            }
        }

        // copy indent of this line into the next
        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);
            int iend;

            for (iend = ++istart; iend < dend; ++iend) {
                char c = dest.charAt(iend);

                // auto expand comments
                if (charAtCursor != '\n' &&
                        c == '/' &&
                        iend + 1 < dend &&
                        dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' ' && c != '\t') {
                    break;
                }
            }

            indent += dest.subSequence(istart, iend);
        }

        // add new indent
        if (pt < 0) {
            indent += "\t";
        }

        // append white space of previous line and new indent
        return source + indent;
    }

    private void convertTabs(Editable e, int start, int count) {
        if (tabWidth < 1) {
            return;
        }

        String s = e.toString();

        for (int stop = start + count;
             (start = s.indexOf("\t", start)) > -1 && start < stop;
             ++start) {
            e.setSpan(
                    new TabWidthSpan(),
                    start,
                    start + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private class TabWidthSpan extends ReplacementSpan {
        @Override
        public int getSize(
                @NonNull Paint paint,
                CharSequence text,
                int start,
                int end,
                Paint.FontMetricsInt fm) {
            return tabWidth;
        }

        @Override
        public void draw(
                @NonNull Canvas canvas,
                CharSequence text,
                int start,
                int end,
                float x,
                int top,
                int y,
                int bottom,
                @NonNull Paint paint) {
        }
    }
}