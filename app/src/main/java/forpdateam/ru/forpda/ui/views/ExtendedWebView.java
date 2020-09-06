package forpdateam.ru.forpda.ui.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
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
import forpdateam.ru.forpda.common.webview.DialogsHelper;
import forpdateam.ru.forpda.common.webview.jsinterfaces.IBase;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView implements IBase {
    private final static String LOG_TAG = ExtendedWebView.class.getSimpleName();
    public final static int DIRECTION_NONE = 0;
    public final static int DIRECTION_UP = 1;
    public final static int DIRECTION_DOWN = 2;
    private int direction = DIRECTION_NONE;
    private int relativeScale = 100;
    private float fontScale = 1.0f;
    private int paddingBottom = 0;
    boolean isJsReady = false;

    private OnDirectionListener onDirectionListener;
    private OnScrollListener onScrollListener;
    private AudioManager audioManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Thread mUiThread;
    private Queue<Runnable> actionsForWebView = new LinkedList<>();
    private JsLifeCycleListener jsLifeCycleListener;

    private DialogsHelper dialogsHelper;

    public interface OnDirectionListener {
        void onDirectionChanged(int direction);
    }

    public interface OnScrollListener {
        void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY);
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

    @Override
    public void onPause() {
        super.onPause();
        Log.e(LOG_TAG, "onPause " + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(LOG_TAG, "onResume " + this);
    }

    public void setOnDirectionListener(OnDirectionListener onDirectionListener) {
        this.onDirectionListener = onDirectionListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        if (onScrollListener != null) {
            onScrollListener.onScrollChange(scrollX, scrollY, oldScrollX, oldScrollY);
        }
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
        settings.setMinimumFontSize(1);
        settings.setMinimumLogicalFontSize(1);
        settings.setDefaultFontSize(16);
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        setRelativeFontSize(16);
        setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_base));
        settings.setTextZoom((int) (getResources().getConfiguration().fontScale * 100));

        Log.e("kokosina", "fontscale " + (getResources().getConfiguration().fontScale) + " : " + getResources().getConfiguration().densityDpi + " : " + getResources().getDisplayMetrics().density + " : " + getResources().getDisplayMetrics().densityDpi + " : " + getResources().getDisplayMetrics().scaledDensity + " : " + getResources().getDisplayMetrics().xdpi);
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
        Log.e("kikosina", "onAttachedToWindow");
        //requestFocus();
        isJsReady = false;
        /*for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();*/
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e("kikosina", "onDetachedFromWindow");
        isJsReady = false;
        /*for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();*/
    }

    //@Deprecated
    @Override
    public void setInitialScale(int scaleInPercent) {
        super.setInitialScale(scaleInPercent);
        Log.d(LOG_TAG, "SET INIT SCALE " + scaleInPercent);
        setPaddingBottom(paddingBottom);
    }


    //0.0f, 1.0f, 2.3f, etc
    public void setRelativeScale(float scale) {
        try {
            relativeScale = (int) (scale * (getResources().getDisplayMetrics().density * 100));
            fontScale = scale;
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        setInitialScale(relativeScale);
    }

    public void setRelativeFontSize(int fontSize) {
        //setRelativeScale(fontSize / 16f);
        getSettings().setDefaultFontSize(fontSize);
        //fontScale = fontSize / 16f;
        updatePaddingBottom();
    }

    public void updatePaddingBottom() {
        setPaddingBottom(paddingBottom);
    }

    public void setPaddingBottom(int padding) {
        Log.e("kokosina", "setPaddingBottom " + padding + " : " + fontScale + " : " + ((paddingBottom / getResources().getDisplayMetrics().density) * (1 / fontScale)));
        paddingBottom = padding;

        evalJs("setPaddingBottom(" + ((paddingBottom / getResources().getDisplayMetrics().density) * (1 / fontScale)) + ");");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        //Log.d("EWV", "evalJs: " + script);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                evalJs(script, null);
            } catch (Exception error) {
                error.printStackTrace();
                loadUrl("javascript:" + script);
            }
        } else {
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
        runInUiThread(this::tryPlayClickEffect);
    }

    @Override
    @JavascriptInterface
    public void domContentLoaded() {
        runInUiThread(() -> {
            Log.d(LOG_TAG, "domContentLoaded " + isJsReady);
            isJsReady = true;
            for (Runnable action : actionsForWebView) {
                try {
                    runInUiThread(action);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actionsForWebView.clear();

            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onDomContentComplete(actions);
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
        runInUiThread(() -> {
            Log.d(LOG_TAG, "onPageLoaded " + isJsReady);
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

    public final void runInUiThread(final Runnable action) {
        //Log.d(LOG_TAG, "runInUiThread " + (Thread.currentThread() == mUiThread));
        if (Thread.currentThread() == mUiThread) {
            action.run();
        } else {
            mHandler.post(action);
        }
    }

    public void setJsLifeCycleListener(JsLifeCycleListener jsLifeCycleListener) {
        this.jsLifeCycleListener = jsLifeCycleListener;
    }

    public interface JsLifeCycleListener {
        void onDomContentComplete(final ArrayList<String> actions);

        void onPageComplete(final ArrayList<String> actions);
    }


    public void syncWithJs(final Runnable action) {
        //Log.d(LOG_TAG, "syncWithJs " + isJsReady);
        if (!isJsReady) {
            actionsForWebView.add(action);
        } else {
            try {
                runInUiThread(action);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }




    /*
     * OVERRIDE CONTEXT MENU
     * */

    @JavascriptInterface
    public void onActionModeComplete() {
        runInUiThread(() -> {
            if (currentActionMode != null) {
                currentActionMode.finish();
            }
        });
    }

    private OnStartActionModeListener actionModeListener;
    private ActionMode currentActionMode = null;

    public interface OnStartActionModeListener {
        void onCreate(ActionMode actionMode, ActionMode.Callback callback);

        boolean onClick(ActionMode actionMode, MenuItem item);
    }

    public void setActionModeListener(OnStartActionModeListener actionModeListener) {
        this.actionModeListener = actionModeListener;
    }

    public void setDialogsHelper(DialogsHelper dialogsHelper) {
        this.dialogsHelper = dialogsHelper;
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

        ActionMode.Callback customCallback = getActionModeCallback(callback);
        ActionMode actionMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            actionMode = super.startActionMode(customCallback, type);
        } else {
            actionMode = super.startActionMode(customCallback);
        }

        currentActionMode = actionMode;
        if (actionModeListener != null) {
            actionModeListener.onCreate(actionMode, customCallback);
        }
        return actionMode;
    }

    private ActionMode.Callback getActionModeCallback(ActionMode.Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ActionMode.Callback2() {

                @Override
                public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                    if (callback instanceof ActionMode.Callback2) {
                        ((ActionMode.Callback2) callback).onGetContentRect(mode, view, outRect);
                    } else {
                        super.onGetContentRect(mode, view, outRect);
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return callback.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return callback.onPrepareActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (actionModeListener != null && actionModeListener.onClick(mode, item)) {
                        return true;
                    }
                    return callback.onActionItemClicked(mode, item);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    currentActionMode = null;
                    callback.onDestroyActionMode(mode);
                }
            };
        } else {
            return new ActionMode.Callback() {

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return callback.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return callback.onPrepareActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (actionModeListener != null && actionModeListener.onClick(mode, item)) {
                        return true;
                    }
                    return callback.onActionItemClicked(mode, item);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    currentActionMode = null;
                    callback.onDestroyActionMode(mode);
                }
            };
        }
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        requestFocusNodeHref(new Handler(msg -> {
            HitTestResult result = getHitTestResult();
            if (dialogsHelper != null) {
                dialogsHelper.handleContextMenu(getContext(), result.getType(), result.getExtra(), (String) msg.getData().get("url"));
            }
            return true;
        }).obtainMessage());
    }

    public void endWork() {
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
