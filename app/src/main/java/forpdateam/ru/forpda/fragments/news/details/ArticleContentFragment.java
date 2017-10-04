package forpdateam.ru.forpda.fragments.news.details;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.CustomWebChromeClient;
import forpdateam.ru.forpda.utils.CustomWebViewClient;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.views.ExtendedWebView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.addJavascriptInterface(this, JS_INTERFACE);
        loadHtml();
        return webView;
    }

    private void loadHtml(){
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
    }

    @JavascriptInterface
    public void toComments() {
        webView.runInUiThread(() -> {
            ((NewsDetailsFragment) getParentFragment()).getFragmentsPager().setCurrentItem(1);
        });
    }

    @JavascriptInterface
    public void sendPoll(String id, String answer, String from) {
        webView.runInUiThread(() -> {
            int pollId = Integer.parseInt(id);
            int answerId = Integer.parseInt(answer);
            Log.d("SUKA", "NEWS SEND POLL " + pollId + " : " + answerId);
            Disposable disposable = RxApi.NewsList().sendPoll(from, pollId, answerId)
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn(throwable -> new DetailsPage())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(page -> {
                        article.setHtml(page.getHtml());
                        loadHtml();
                    }, throwable -> {

                    });
            ((NewsDetailsFragment) getParentFragment()).getDisposable().add(disposable);
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

    private class ArticleWebViewClient extends CustomWebViewClient {

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
