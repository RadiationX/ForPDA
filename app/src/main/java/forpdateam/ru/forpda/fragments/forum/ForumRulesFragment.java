package forpdateam.ru.forpda.fragments.forum;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.forum.models.ForumRules;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.CustomWebViewClient;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ExtendedWebView;

/**
 * Created by radiationx on 16.10.17.
 */

public class ForumRulesFragment extends TabFragment {
    public final static String JS_INTERFACE = "IRules";
    private ExtendedWebView webView;
    private Subscriber<ForumRules> mainSubscriber = new Subscriber<>(this);
    protected int searchViewTag = 0;

    public ForumRulesFragment() {
        configuration.setAlone(true);
        configuration.setMenu(true);
        configuration.setDefaultTitle("Правила форума");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        fragmentContent.addView(webView);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setJsLifeCycleListener(new ExtendedWebView.JsLifeCycleListener() {
            @Override
            public void onDomContentComplete(ArrayList<String> actions) {
                setRefreshing(false);
            }

            @Override
            public void onPageComplete(ArrayList<String> actions) {

            }
        });
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        addSearchOnPageItem(getMenu());
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Forum().getRules(true), this::onLoad, new ForumRules(), view1 -> loadData());
        return true;
    }

    private void onLoad(ForumRules rules) {
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", rules.getHtml(), "text/html", "utf-8", null);
        new Handler().postDelayed(() -> {
            if (isAdded())
                setRefreshing(false);
        }, 1000);
    }

    @JavascriptInterface
    public void copyRule(String text) {
        if (getContext() == null)
            return;
        webView.runInUiThread(() -> {
            if (getContext() == null)
                return;
            new AlertDialog.Builder(getContext())
                    .setMessage("Скопировать правило в буфер обмена?")
                    .setPositiveButton(R.string.ok, (dialog, which) -> Utils.copyToClipBoard(text))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void addSearchOnPageItem(Menu menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu);
        MenuItem searchOnPageMenuItem = menu.findItem(R.id.action_search);
        searchOnPageMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        /*MenuItemCompat.setOnActionExpandListener(searchOnPageMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                return true;
            }
        });*/
        SearchView searchView = (SearchView) searchOnPageMenuItem.getActionView();
        searchView.setTag(searchViewTag);

        searchView.setOnSearchClickListener(v -> {
            if (searchView.getTag().equals(searchViewTag)) {
                ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                if (searchClose != null)
                    ((ViewGroup) searchClose.getParent()).removeView(searchClose);

                ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(App.px48, App.px48);
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true);

                AppCompatImageButton btnNext = new AppCompatImageButton(searchView.getContext());
                btnNext.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search_next));
                btnNext.setBackgroundResource(outValue.resourceId);

                AppCompatImageButton btnPrev = new AppCompatImageButton(searchView.getContext());
                btnPrev.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search_prev));
                btnPrev.setBackgroundResource(outValue.resourceId);

                ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
                ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

                btnNext.setOnClickListener(v1 -> findNext(true));
                btnPrev.setOnClickListener(v1 -> findNext(false));
                searchViewTag++;
            }
        });

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                findText(newText);
                return false;
            }
        });
    }

    protected void findNext(boolean next) {
        webView.findNext(next);
    }

    protected void findText(String text) {
        webView.findAllAsync(text);
    }
}
