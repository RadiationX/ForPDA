package forpdateam.ru.forpda.ui.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;
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
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.addJavascriptInterface(this, JS_INTERFACE);
        loadHtml();
        return webView;
    }

    private void loadHtml() {
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
    }

    @JavascriptInterface
    public void toComments() {
        if (getContext() == null)
            return;
        webView.runInUiThread(() -> {
            ((NewsDetailsFragment) getParentFragment()).getFragmentsPager().setCurrentItem(1);
        });
    }

    @JavascriptInterface
    public void sendPoll(String id, String answer, String from) {
        if (getContext() == null)
            return;
        webView.runInUiThread(() -> {
            int pollId = Integer.parseInt(id);
            String[] answers = answer.split(",");
            int answersId[] = new int[answers.length];
            for (int i = 0; i < answers.length; i++) {
                answersId[i] = Integer.parseInt(answers[i]);
            }
            NewsDetailsFragment fragment = ((NewsDetailsFragment) getParentFragment());
            fragment.subscribe(RxApi.NewsList().sendPoll(from, pollId, answersId), page -> {
                article.setHtml(page.getHtml());
                loadHtml();
            }, new DetailsPage());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.endWork();
            ((MainActivity) getActivity()).getWebViewsProvider().push(webView);
        }
    }
}
