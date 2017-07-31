package forpdateam.ru.forpda.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ViewParent;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.settings.Preferences;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView {
    public final static int DIRECTION_NONE = 0;
    public final static int DIRECTION_UP = 1;
    public final static int DIRECTION_DOWN = 2;
    private int direction = DIRECTION_NONE;
    private OnDirectionListener onDirectionListener;

    public interface OnDirectionListener {
        void onDirectionChanged(int direction);
    }

    public ExtendedWebView(Context context) {
        super(context);
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setOnDirectionListener(OnDirectionListener onDirectionListener) {
        this.onDirectionListener = onDirectionListener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        int newDirection = scrollY > oldScrollY ? DIRECTION_DOWN : DIRECTION_UP;
        if (newDirection != direction) {
            direction = newDirection;
            if (onDirectionListener != null) {
                onDirectionListener.onDirectionChanged(newDirection);
            }
        }
    }

    public int getDirection() {
        return direction;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setBuiltInZoomControls(false);
        settings.setDefaultFontSize(16);
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);
        setRelativeFontSize(Preferences.Main.getWebViewSize());
        setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_base));
    }

    private int relativeScale = 100;
    private float fontScale = 1.0f;
    private int paddingBottom = 0;

    public int getRelativeScale() {
        return relativeScale;
    }

    public float getFontScale() {
        return fontScale;
    }


    @Deprecated
    @Override
    public void setInitialScale(int scaleInPercent) {
        super.setInitialScale(scaleInPercent);
        Log.e("SUKA", "SET INIT SCALE " + scaleInPercent);
        setPaddingBottom(paddingBottom);
    }


    //0.0f, 1.0f, 2.3f, etc
    public void setRelativeScale(float scale) {
        try {
            relativeScale = (int) (scale * (App.getInstance().getDensity() * 100));
            fontScale = scale;
        } catch (Exception ignore) {
        }
        setInitialScale(relativeScale);
    }

    public void setRelativeFontSize(int fontSize) {
        setRelativeScale(fontSize / 16f);
    }

    public void updatePaddingBottom(){
        setPaddingBottom(paddingBottom);
    }
    public void setPaddingBottom(int padding) {
        paddingBottom = padding;
        Log.d("kurwa", "setPaddingBottom " + padding + " : " + App.getInstance().getDensity() + " : " + fontScale + " : " + relativeScale);
        evalJs("setPaddingBottom(" + ((paddingBottom / App.getInstance().getDensity()) * (1 / fontScale)) + ");");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                evaluateJavascript(script, null);
            } catch (Exception error) {
                loadUrl("javascript:" + script);
            }
        } else {
            loadUrl("javascript:" + script);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script, ValueCallback<String> resultCallback) {
        evaluateJavascript(script, resultCallback);
    }

    private OnStartActionModeListener actionModeListener;

    public interface OnStartActionModeListener {
        void OnStart(ActionMode actionMode, ActionMode.Callback callback, int type);
    }

    public void setActionModeListener(OnStartActionModeListener actionModeListener) {
        this.actionModeListener = actionModeListener;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return myActionMode(callback, 0);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return myActionMode(callback, type);
    }

    private ActionMode myActionMode(ActionMode.Callback callback, int type) {
        ViewParent parent = getParent();
        if (parent == null) {
            return null;
        }
        ActionMode actionMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            actionMode = super.startActionMode(callback, type);
        } else {
            actionMode = super.startActionMode(callback);
        }
        if (actionModeListener != null)
            actionModeListener.OnStart(actionMode, callback, type);
        return actionMode;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        requestFocusNodeHref(new Handler(msg -> {
            HitTestResult result = getHitTestResult();
            DialogsHelper.handleContextMenu(getContext(), result.getType(), result.getExtra(), (String) msg.getData().get("url"));
            return true;
        }).obtainMessage());
    }

    public void destroy() {
        setActionModeListener(null);
        setWebChromeClient(null);
        setWebViewClient(null);
        loadUrl("about:blank");
        clearHistory();
        clearSslPreferences();
        clearDisappearingChildren();
        clearFocus();
        clearFormData();
        clearMatches();
    }
}
