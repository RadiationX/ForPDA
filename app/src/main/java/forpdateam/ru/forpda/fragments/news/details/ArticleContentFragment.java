package forpdateam.ru.forpda.fragments.news.details;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.views.ExtendedWebView;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleContentFragment extends Fragment {
    public final static String JS_INTERFACE = "INews";
    private ExtendedWebView webView;
    private DetailsPage article;

    public ArticleContentFragment setArticle(DetailsPage article) {
        this.article = article;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        webView = ((MainActivity) getActivity()).getWebViewsProvider().pull(getContext());
        registerForContextMenu(webView);
        webView.setWebViewClient(new ArticleWebViewClient());
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
        return webView;
    }

    @JavascriptInterface
    public void toComments() {
        webView.runInUiThread(() -> {
            ((NewsDetailsFragment) getParentFragment()).getFragmentsPager().setCurrentItem(1);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            ((MainActivity) getActivity()).getWebViewsProvider().push(webView);
        }
    }

    private class ArticleWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }

        private boolean handleUri(Uri uri) {
            IntentHandler.handle(uri.toString());
            return true;
        }
    }
}
