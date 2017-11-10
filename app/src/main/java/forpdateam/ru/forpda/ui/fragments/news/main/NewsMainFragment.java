package forpdateam.ru.forpda.ui.fragments.news.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.news.Constants;
import forpdateam.ru.forpda.api.news.models.NewsItem;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.ui.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.ui.fragments.news.main.timeline.NewsListAdapter;
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.PauseOnScrollListener;

/**
 * Created by isanechek on 8/8/17.
 */

public class NewsMainFragment extends RecyclerFragment implements NewsListAdapter.ItemClickListener {
    private NewsListAdapter adapter;
    private String category = Constants.NEWS_CATEGORY_ALL;
    private DynamicDialogMenu<NewsMainFragment, NewsItem> dialogMenu;

    public NewsMainFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_news_list));
        configuration.setAlone(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        setCardsBackground();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, true));
        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        recyclerView.addOnScrollListener(pauseOnScrollListener);
        adapter = new NewsListAdapter();
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add(R.string.fragment_title_search)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_search))
                .setOnMenuItemClickListener(item -> {
                    String url = "https://4pda.ru/?s=";
                    Bundle args = new Bundle();
                    args.putString(TabFragment.ARG_TAB, url);
                    TabManager.get().add(SearchFragment.class, args);
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        page = 1;
        loadDataNews(page, true);
        return true;
    }


    @Override
    public boolean onLongItemClick(View view, NewsItem item, int position) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> {
                Utils.copyToClipBoard("https://4pda.ru/index.php?p=" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.share), (context, data) -> {
                Utils.shareText("https://4pda.ru/index.php?p=" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> {
                String title = data.getTitle();
                String url = "https://4pda.ru/index.php?p=" + data.getId();
                NotesAddPopup.showAddNoteDialog(context1.getContext(), title, url);
            });
        }
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), NewsMainFragment.this, item);
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
        args.putInt(NewsDetailsFragment.ARG_NEWS_COMMENTS_COUNT, item.getCommentsCount());
        args.putBoolean(NewsDetailsFragment.OTHER_CASE, true);
        TabManager.get().add(NewsDetailsFragment.class, args);
    }

    @Override
    public void onNickClick(View view, NewsItem item, int position) {
        if (item.getAuthorId() != 0) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + item.getAuthorId());
        }
    }

    int page = 1;

    @Override
    public void onLoadMoreClick() {
        page++;
        loadDataNews(page, false);
    }

    private void loadDataNews(int page, boolean withClear) {
        subscribe(RxApi.NewsList().getNews(category, page), list -> onLoadNews(list, withClear), new ArrayList<>(), v -> loadDataNews(page, withClear));
    }

    private void onLoadNews(List<NewsItem> list, boolean withClear) {
        setRefreshing(false);
        if (withClear) {
            if (!list.isEmpty()) {
                adapter.clear();
                adapter.addAll(list);
            }
        } else adapter.insertMore(list);
    }

    private void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
