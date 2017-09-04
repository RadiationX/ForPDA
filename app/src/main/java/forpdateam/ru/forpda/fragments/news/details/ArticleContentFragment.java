package forpdateam.ru.forpda.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.api.devdb.models.Device;
import forpdateam.ru.forpda.api.news.models.DetailsPage;
import forpdateam.ru.forpda.fragments.devdb.device.SubDeviceFragment;
import forpdateam.ru.forpda.views.ExtendedWebView;

/**
 * Created by radiationx on 03.09.17.
 */

public class ArticleContentFragment extends Fragment {
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
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", article.getHtml(), "text/html", "utf-8", null);
        return webView;
    }
}
