package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by radiationx on 11.06.17.
 */

public class WebViewsProvider {
    private Queue<ExtendedWebView> availableWebViews = new LinkedList<>();
    private Timer webViewCleaner = new Timer();

    public WebViewsProvider() {
        webViewCleaner.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("FORPDA_LOG", "try remove webview " + this);
                if (availableWebViews.size() > 0) {
                    Log.d("FORPDA_LOG", "remove webview " + availableWebViews.element().getTag());
                    availableWebViews.remove();
                }
            }
        }, 0, 60000);
    }

    public ExtendedWebView pull(Context context) {
        ExtendedWebView webView;
        if (availableWebViews.size() > 0) {
            webView = availableWebViews.poll();
        } else {
            webView = new ExtendedWebView(context);
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        Log.d("SUKA", "PULL WEBVIEW "+webView);
        return webView;
    }

    public void push(ExtendedWebView webView) {
        ViewGroup parent = ((ViewGroup) webView.getParent());
        if (parent != null) {
            parent.removeView(webView);
        }
        if (availableWebViews.size() < 10) {
            availableWebViews.add(webView);
        }
        Log.d("SUKA", "PUSH WEBVIEW "+webView);
    }

    public void destroy() {
        webViewCleaner.cancel();
        webViewCleaner.purge();
        availableWebViews.clear();
    }
}
