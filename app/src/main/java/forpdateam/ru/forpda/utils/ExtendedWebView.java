package forpdateam.ru.forpda.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ViewParent;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView {
    public ExtendedWebView(Context context) {
        super(context);
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            evaluateJavascript(js, null);
        else
            loadUrl("javascript:" + js);
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
        actionModeListener.OnStart(actionMode, callback, type);
        return actionMode;
    }
}
