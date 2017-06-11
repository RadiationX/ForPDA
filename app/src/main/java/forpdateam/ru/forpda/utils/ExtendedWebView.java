package forpdateam.ru.forpda.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ViewParent;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView {
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

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setBuiltInZoomControls(false);
        settings.setDefaultFontSize(16);
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            evaluateJavascript(script, null);
        else
            loadUrl("javascript:" + script);
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
    
    public void destroy(){
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
