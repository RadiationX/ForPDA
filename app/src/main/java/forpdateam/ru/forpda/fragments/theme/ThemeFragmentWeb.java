package forpdateam.ru.forpda.fragments.theme;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.IOException;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.10.16.
 */

public class ThemeFragmentWeb extends ThemeFragment {
    private SwipeRefreshLayout refreshLayout;
    FrameLayout container2;
    WebView webView;
    ThemePage pageData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_theme, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        container2 = (FrameLayout) findViewById(R.id.theme_view_container);
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new WebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        webView.loadUrl("about:blank");
        container2.addView(webView);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);

        webView.addJavascriptInterface(this, "ITheme");
        webView.getSettings().setJavaScriptEnabled(true);

        return view;
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
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
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);
        pageData = themePage;
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

    private ThemePost getPostById(int postId) {
        for (ThemePost post : pageData.getPosts())
            if (post.getId() == postId)
                return post;
        return null;
    }

    /*
    *
    * JavaScript Interface additional functions
    *
    * */

    private void jumpToPage(int st) {
        String url = "http://4pda.ru/forum/index.php?showtopic=";
        url = url.concat(Uri.parse(getTabUrl()).getQueryParameter("showtopic"));
        if (st != 0) url = url.concat("&st=").concat(Integer.toString(st));
        setTabUrl(url);
        loadData();
    }


    /*
    *
    * JavaScript Interface functions
    *
    * */

    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }

    @JavascriptInterface
    public void firstPage() {
        if (pageData.getCurrentPage() <= 0) return;
        run(() -> {
            jumpToPage(0);
        });
    }

    @JavascriptInterface
    public void prevPage() {
        if (pageData.getCurrentPage() <= 1) return;
        run(() -> {
            jumpToPage((pageData.getCurrentPage() - 2) * pageData.getPostsOnPageCount());
        });
    }

    @JavascriptInterface
    public void nextPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        run(() -> {
            jumpToPage(pageData.getCurrentPage() * pageData.getPostsOnPageCount());
        });
    }

    @JavascriptInterface
    public void lastPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        run(() -> {
            jumpToPage((pageData.getAllPagesCount() - 1) * pageData.getPostsOnPageCount());
        });
    }

    @JavascriptInterface
    public void selectPage() {
        run(() -> {
            final int[] pages = new int[pageData.getAllPagesCount()];

            for (int i = 0; i < pageData.getAllPagesCount(); i++)
                pages[i] = i + 1;

            LayoutInflater inflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.select_page_layout, null);

            assert view != null;
            final ListView listView = (ListView) view.findViewById(R.id.listview);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new ThemePagesAdapter(getContext(), pages));
            listView.setItemChecked(pageData.getCurrentPage() - 1, true);
            listView.setSelection(pageData.getCurrentPage() - 1);

            AlertDialog dialog = new AlertDialog.Builder(getMainActivity())
                    .setView(view)
                    .show();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
                if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                    return;
                }
                jumpToPage(i2 * pageData.getPostsOnPageCount());
                dialog.cancel();
            });

        });
    }

    @JavascriptInterface
    public void showUserMenu(final int postId) {
        run(() -> {
            Log.d("kek", getPostById(postId).getNick());
        });
    }

    @JavascriptInterface
    public void showReputationMenu(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void showPostMenu(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void reportPost(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void insertNick(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void quotePost(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void deletePost(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void editPost(final int postId) {
        run(() -> {
        });
    }

    @JavascriptInterface
    public void votePost(final int postId, final boolean type) {
        run(() -> {
            Log.d("kek", postId + " : " + type);
        });
    }
}
