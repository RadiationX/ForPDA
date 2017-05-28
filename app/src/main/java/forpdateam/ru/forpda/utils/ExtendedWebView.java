package forpdateam.ru.forpda.utils;

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

    public void init() {
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setDefaultFontSize(16);
        getSettings().setTextZoom(100);
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
            WebView.HitTestResult result = getHitTestResult();
            DialogsHelper.handleContextMenu(getContext(), result.getType(), result.getExtra(), (String) msg.getData().get("url"));
            return true;
        }).obtainMessage());
    }
}
