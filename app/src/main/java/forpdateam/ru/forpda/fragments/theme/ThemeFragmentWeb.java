package forpdateam.ru.forpda.fragments.theme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.io.IOException;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment {
    FrameLayout container2;
    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_theme, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        container2 = (FrameLayout) findViewById(R.id.theme_view_container);
        viewsReady();

        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new WebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        webView.loadUrl("about:blank");
        container2.addView(webView);


        return view;
    }

    @Override
    public void loadData() {
        getCompositeDisposable().add(Api.Theme().getPage(getTabUrl(), true)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                }));
    }

    private void bindUi(ThemePage themePage) throws IOException {
        setTitle(themePage.getTitle());
        setSubtitle(String.valueOf(themePage.getCurrentPage()).concat("/").concat(String.valueOf(themePage.getAllPagesCount())));
        webView.loadDataWithBaseURL(getTabUrl(), themePage.getHtml(), "text/html", "utf-8", null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((ViewGroup) webView.getParent()).removeAllViews();
        webView.loadUrl("about:blank");
        if (getMainActivity().getWebViews().size() < 10) {
            getMainActivity().getWebViews().add(webView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
