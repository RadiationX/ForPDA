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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.utils.Html;

/*
* ORIGINAL: https://github.com/markusfisch/CodeEditor/blob/master/app/src/main/java/de/markusfisch/android/CodeEditor/widget/CodeEditor.java
* */
public class CodeEditor extends AppCompatEditText {
    private static class ForumCodes {
        private static final Pattern ELEMENT = Pattern.compile("(\\[(?:\\/)?((?:attachment|background|nomergetime|mergetime|snapback|numlist|spoiler|offtop|center|color|right|quote|code|font|hide|left|list|size|img|sub|sup|cur|url|b|i|u|s|\\*)))=?([^\\]\\[]+?)?(\\])", Pattern.CASE_INSENSITIVE);
        private static final Pattern ATTRIBUTE = Pattern.compile("(name|date|post)?=?(\"[^\\\"]*?\"|[\\w\\d]+)", Pattern.CASE_INSENSITIVE);
    }

    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = () -> {
        Editable e = getText();
        highlightWithoutChange(e);
    };

    private int updateDelay = 500;
    private boolean modified = true;
    private int colorTag;
    private int colorAttrName;
    private int colorAttrValue;
    private ScrollView scrollView;

    private Runnable scrollerTask;
    private int initialPosition;
    private int newCheck = 100;

    public void attachToScrollView(ScrollView sv) {
        scrollView = sv;

        scrollView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int heightWas = oldBottom - oldTop;
            if (v.getHeight() != heightWas) {
                smartUpdateHighlighting();
            }
        });

        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                initialPosition = scrollView.getScrollY();
                scrollView.postDelayed(scrollerTask, newCheck);
            }
            return false;
        });

        scrollerTask = () -> {
            int newPosition = scrollView.getScrollY();
            if (initialPosition - newPosition == 0) {
                smartUpdateHighlighting();
            } else {
                initialPosition = scrollView.getScrollY();
                scrollView.postDelayed(scrollerTask, newCheck);
            }
        };

    }

    public CodeEditor(Context context) {
        super(context);
        init();
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void setUpdateDelay(int ms) {
        updateDelay = ms;
    }


    public void updateHighlighting() {
        highlightWithoutChange(getText());
    }

    private void init() {
        //setHorizontallyScrolling(true);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
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
                smartUpdateHighlighting();
            }
        });

        setSyntaxColors();
        setUpdateDelay(500);
        updateHighlighting();
    }

    private void smartUpdateHighlighting() {
        cancelUpdate();
        if (!modified) {
            return;
        }
        updateHandler.postDelayed(updateRunnable, updateDelay);
    }

    private void setSyntaxColors() {
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

            int scrollY = scrollView.getScrollY();
            int scrollViewHeight = scrollView.getHeight();


            int visibleStart = getOffsetForPosition(0, scrollY);
            int visibleEnd = getOffsetForPosition(0, scrollY + scrollViewHeight);
            visibleStart = Math.max(0, visibleStart - 100);
            visibleEnd = Math.min(e.length(), visibleEnd + 100);

            CharSequence hlText = e.subSequence(visibleStart, visibleEnd);
            //visibleStart = 0;
            //visibleEnd = 0;
            for (Matcher m = ForumCodes.ELEMENT.matcher(hlText), attributes = null; m.find(); ) {
                String attrsSrc = m.group(3);
                if (attrsSrc != null) {
                    String tagName = m.group(2);
                    int eg3s = m.start(3);
                    int eg3e = m.end(3);
                    int color = colorAttrValue;

                    if (tagName.equalsIgnoreCase("quote")) {
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
                                        visibleStart + eg3s + ag1s,
                                        visibleStart + eg3s + ag1e,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            String attrValue = attributes.group(2);
                            if (attrValue != null) {
                                int ag2s = attributes.start(2);
                                int ag2e = attributes.end(2);

                                e.setSpan(
                                        new ForegroundColorSpan(color),
                                        visibleStart + eg3s + ag2s,
                                        visibleStart + eg3s + ag2e,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }


                        }
                    } else {
                        if (tagName.equalsIgnoreCase("color") || tagName.equalsIgnoreCase("background")) {
                            try {
                                if (attrsSrc.charAt(0) != '#') {
                                    Integer htmlColor = Html.getColorMap().get(attrsSrc);
                                    if (htmlColor != null) {
                                        color = htmlColor;
                                    }
                                } else {
                                    color = Color.parseColor(attrsSrc);
                                }
                            } catch (Exception ignore) {
                            }
                        }
                        e.setSpan(
                                new ForegroundColorSpan(color),
                                visibleStart + eg3s,
                                visibleStart + eg3e,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                int eg1s = m.start(1);
                int eg1e = m.end(1);
                e.setSpan(
                        new ForegroundColorSpan(colorTag),
                        visibleStart + eg1s,
                        visibleStart + eg1e,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                int eg4s = m.start(4);
                int eg4e = m.end(4);
                e.setSpan(
                        new ForegroundColorSpan(colorTag),
                        visibleStart + eg4s,
                        visibleStart + eg4e,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        } catch (IllegalStateException ex) {
            // raised by Matcher.start()/.end() when
            // no successful match has been made what
            // shouldn't ever happen because of find()
        }

        Log.d("CodeEditor", "Time: " + (System.currentTimeMillis() - time));
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
}