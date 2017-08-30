package forpdateam.ru.forpda.fragments.news.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.api.news.Constants;
import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesAdapter;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.fragments.news.main.timeline.NewsListAdapter;
import forpdateam.ru.forpda.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsMainFragment extends TabFragment implements
        NewsListAdapter.ItemClickListener {
    private SwipeRefreshLayout refreshLayout;
    private NewsListAdapter adapter;
    private NewsApi mApi;
    private String category = Constants.NEWS_CATEGORY_ALL;
    private CompositeDisposable mDisposable;
    //private Realm realm;

    public NewsMainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration.setDefaultTitle(getString(R.string.default_news_title));
        configuration.setAlone(true);
        //configuration.setUseCache(true);
        //realm = Realm.getDefaultInstance();
        mDisposable = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        baseInflateFragment(inflater, R.layout.news_list_fragment);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.news_list_progress);
        progressBar.setVisibility(View.GONE);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_list_refresh);
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setVisibility(View.VISIBLE);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.news_list);

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));

        adapter = new NewsListAdapter();
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        mApi = new NewsApi();
        viewsReady();
        return view;
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshLayout.setRefreshing(true);
        page = 1;
        loadDataNews(page, true);
    }

    /*@Override
    public void loadCacheData() {
        super.loadCacheData();
        if (realm.isClosed()) return;
        List<News> list = realm.where(News.class).findAll();
        d("from realm " + list.size());
        if (list.size() > 0) adapter.insertData(list);
        else loadData();
    }
*/
    @Override
    public void onPause() {
        super.onPause();
        if (mDisposable.isDisposed()) {
            mDisposable.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable.isDisposed()) {
            mDisposable.clear();
        }

        /*if (realm != null) {
            realm.close();
        }*/
    }

    private void d(String msg) {
        Log.d("PARENT", msg);
    }

    private AlertDialogMenu<NewsMainFragment, NewsItem> favoriteDialogMenu, showedFavoriteDialogMenu;

    @Override
    public boolean onLongItemClick(View view, NewsItem item, int position) {
        if (favoriteDialogMenu == null) {
            favoriteDialogMenu = new AlertDialogMenu<>();
            showedFavoriteDialogMenu = new AlertDialogMenu<>();
            favoriteDialogMenu.addItem("Скопировать ссылку", (context, data) -> {
                Utils.copyToClipBoard("https://4pda.ru/index.php?p="+data.getId());
            });
            favoriteDialogMenu.addItem("Поделиться", (context, data) -> {
                Utils.shareText("https://4pda.ru/index.php?p="+data.getId());
            });
        }
        showedFavoriteDialogMenu.clear();

        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(0));
        showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(1));
        new AlertDialog.Builder(getContext())
                .setItems(showedFavoriteDialogMenu.getTitles(), (dialog, which) -> {
                    showedFavoriteDialogMenu.onClick(which, NewsMainFragment.this, item);
                })
                .show();
        return true;
    }

    @Override
    public void onItemClick(View view, NewsItem item, int position) {
        ViewCompat.setTransitionName(view, String.valueOf(position) + "_image");
        Bundle args = new Bundle();
        args.putInt(NewsDetailsFragment.ARG_NEWS_ID, item.getId());
        args.putString(NewsDetailsFragment.ARG_NEWS_TITLE, item.getTitle());
        args.putString(NewsDetailsFragment.ARG_NEWS_AUTHOR_NICK, item.getAuthor());
        args.putString(NewsDetailsFragment.ARG_NEWS_DATE, item.getDate());
        args.putString(NewsDetailsFragment.ARG_NEWS_IMAGE, item.getImgUrl());
        args.putBoolean(NewsDetailsFragment.OTHER_CASE, true);
        TabManager.getInstance().add(NewsDetailsFragment.class, args);
    }

    int page = 1;

    @Override
    public void onLoadMoreClick() {
        page++;
        loadDataNews(page, false);
    }

    private void loadDataNews(int page, boolean withClear) {
        mDisposable.add(RxApi.NewsList().getNews(category, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    onLoadNews(res, withClear);
                }, throwable -> {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.news_opps, Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadNews(List<NewsItem> list, boolean withClear) {
        refreshLayout.setRefreshing(false);
        if (withClear) {
            adapter.clear();
        }
        int startIndex = adapter.getItemCount();
        Log.d("SUKA", "ADAPTER SIZE " + adapter.getItemCount() + " : " + withClear);
        if (list.size() > 0) {
            adapter.addAll(list);
        }
        adapter.notifyDataSetChanged();
        //adapter.notifyItemRangeInserted(startIndex, adapter.getItemCount());
    }

    private void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
