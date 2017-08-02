package forpdateam.ru.forpda.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.SoundEffectConstants;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.jsinterfaces.IBase;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.DialogsHelper;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView implements IBase {
    public final static int DIRECTION_NONE = 0;
    public final static int DIRECTION_UP = 1;
    public final static int DIRECTION_DOWN = 2;
    private int direction = DIRECTION_NONE;
    private int relativeScale = 100;
    private float fontScale = 1.0f;
    private int paddingBottom = 0;
    boolean isJsReady = false;

    private OnDirectionListener onDirectionListener;
    private AudioManager audioManager;
    private Handler mHandler = new Handler();
    private Thread mUiThread;
    private Queue<Runnable> actionsForWebView = new LinkedList<>();
    private JsLifeCycleListener jsLifeCycleListener;

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
        mUiThread = Thread.currentThread();
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        addJavascriptInterface(this, IBase.JS_BASE_INTERFACE);
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setBuiltInZoomControls(false);
        settings.setDefaultFontSize(16);
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);
        setRelativeFontSize(Preferences.Main.getWebViewSize());
        setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_base));
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        isJsReady = false;
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        isJsReady = false;
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(String url) {
        isJsReady = false;
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        isJsReady = false;
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isJsReady = false;
        for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isJsReady = false;
        for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();
    }

    @Deprecated
    @Override
    public void setInitialScale(int scaleInPercent) {
        super.setInitialScale(scaleInPercent);
        Log.e("EWV", "SET INIT SCALE " + scaleInPercent);
        setPaddingBottom(paddingBottom);
    }


    //0.0f, 1.0f, 2.3f, etc
    public void setRelativeScale(float scale) {
        try {
            relativeScale = (int) (scale * (App.getInstance().getDensity() * 100));
            fontScale = scale;
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        setInitialScale(relativeScale);
    }

    public void setRelativeFontSize(int fontSize) {
        setRelativeScale(fontSize / 16f);
    }

    public void updatePaddingBottom() {
        setPaddingBottom(paddingBottom);
    }

    public void setPaddingBottom(int padding) {
        paddingBottom = padding;
        evalJs("setPaddingBottom(" + ((paddingBottom / App.getInstance().getDensity()) * (1 / fontScale)) + ");");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        try {
            evalJs(script, null);
        } catch (Exception error) {
            error.printStackTrace();
            loadUrl("javascript:" + script);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script, ValueCallback<String> resultCallback) {
        syncWithJs(() -> evaluateJavascript(script, resultCallback));
    }

    /*
    * JS LIFECYCLE
    * */


    @Override
    @JavascriptInterface
    public void playClickEffect() {
        run(this::tryPlayClickEffect);
    }

    @Override
    @JavascriptInterface
    public void domContentLoaded() {
        run(() -> {
            Log.d("EWV", "domContentLoaded " + isJsReady);
            isJsReady = true;
            for (Runnable action : actionsForWebView) {
                try {
                    run(action);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actionsForWebView.clear();

            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onPageComplete(actions);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actions.add("nativeEvents.onNativeDomComplete();");

            String script = "";
            for (String action : actions) {
                script += action;
            }
            evalJs(script);
        });
    }

    @Override
    @JavascriptInterface
    public void onPageLoaded() {
        run(() -> {
            Log.d("EWV", "onPageLoaded " + isJsReady);
            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onPageComplete(actions);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actions.add("nativeEvents.onNativePageComplete();");

            String script = "";
            for (String action : actions) {
                script += action;
            }
            evalJs(script);
        });
    }


    public void tryPlayClickEffect() {
        try {
            audioManager.playSoundEffect(SoundEffectConstants.CLICK);
        } catch (Exception ignore) {
        }
    }

    public final void run(final Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }

    public void setJsLifeCycleListener(JsLifeCycleListener jsLifeCycleListener) {
        this.jsLifeCycleListener = jsLifeCycleListener;
    }

    public interface JsLifeCycleListener {
        void onDomContentComplete(final ArrayList<String> actions);

        void onPageComplete(final ArrayList<String> actions);
    }


    public void syncWithJs(final Runnable runnable) {
        if (!isJsReady) {
            actionsForWebView.add(runnable);
        } else {
            mHandler.post(runnable);
        }
    }




    /*
    * OVERRIDE CONTEXT MENU
    * */

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
