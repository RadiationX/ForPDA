package forpdateam.ru.forpda.fragments.favorites;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemePagesAdapter;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FavoritesAdapter.OnItemClickListener onItemClickListener =
            favItem -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, favItem.getTopicTitle());
                IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + favItem.getTopicId() + "&view=getnewpost", args);
            };
    private AlertDialogMenu<FavoritesFragment, FavItem> favoriteDialogMenu;
    private FavoritesAdapter.OnLongItemClickListener onLongItemClickListener =
            favItem -> {
                if (favoriteDialogMenu == null) {
                    favoriteDialogMenu = new AlertDialogMenu<>();
                    favoriteDialogMenu.addItem("Скопировать ссылку", (context, data) -> Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data.getTopicId()))));
                    favoriteDialogMenu.addItem("Вложения", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + data.getTopicId()));
                    favoriteDialogMenu.addItem("Открыть форум темы", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + data.getForumId()));
                    favoriteDialogMenu.addItem("Изменить тип подписки", (context, data) -> {
                        new AlertDialog.Builder(context.getContext())
                                .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> context.changeFav(0, Favorites.SUB_TYPES[which1], data.getFavId()))
                                .show();
                    });
                    favoriteDialogMenu.addItem(getPinText(favItem.isPin()), (context, data) -> context.changeFav(1, data.isPin() ? "unpin" : "pin", data.getFavId()));
                    favoriteDialogMenu.addItem("Удалить", (context, data) -> context.changeFav(2, null, data.getFavId()));
                }

                int index = favoriteDialogMenu.containsIndex(getPinText(!favItem.isPin()));
                if (index != -1)
                    favoriteDialogMenu.changeTitle(index, getPinText(favItem.isPin()));

                new AlertDialog.Builder(getContext())
                        .setItems(favoriteDialogMenu.getTitles(), (dialog, which) -> {
                            Log.d("kek", "ocnlicl " + favItem + " : " + favItem.getFavId());
                            favoriteDialogMenu.onClick(which, FavoritesFragment.this, favItem);
                        })
                        .show();
            };

    private Realm realm;
    private RealmResults<FavItem> results;
    private FavoritesAdapter adapter;
    private Subscriber<FavData> mainSubscriber = new Subscriber<>();
    private Subscriber<Boolean> helperSubscriber = new Subscriber<>();
    boolean markedRead = false;
    private FavData data;

    private CharSequence getPinText(boolean b) {
        return b ? "Открепить" : "Закрепить";
    }

    @Override
    public String getDefaultTitle() {
        return "Избранное";
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Override
    public boolean isUseCache() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    protected TabLayout tabLayout;
    private int currentSt = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new FavoritesAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);

        tabLayout = (TabLayout) inflater.inflate(R.layout.theme_toolbar, (ViewGroup) toolbar.getParent(), false);
        ((ViewGroup) toolbar.getParent()).addView(tabLayout, ((ViewGroup) toolbar.getParent()).indexOfChild(toolbar));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_left).setTag("first"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_left).setTag("prev"));
        tabLayout.addTab(tabLayout.newTab().setText("Выбор").setTag("selectPage"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_right).setTag("next"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_right).setTag("last"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (refreshLayout.isRefreshing()) return;
                assert tab.getTag() != null;
                switch ((String) tab.getTag()) {
                    case "first":
                        firstPage();
                        break;
                    case "prev":
                        prevPage();
                        break;
                    case "selectPage":
                        selectPage(data);
                        break;
                    case "next":
                        nextPage();
                        break;
                    case "last":
                        lastPage();
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(App.px56 + App.px24);

        bindView();
        return view;
    }

    public void jumpToPage(int st) {
        currentSt = st;
        loadData();
    }


    public void firstPage() {
        if (data.getCurrentPage() <= 0) return;
        jumpToPage(0);
    }


    public void prevPage() {
        if (data.getCurrentPage() <= 1) return;
        jumpToPage((data.getCurrentPage() - 2) * data.getItemsPerPage());
    }


    public void nextPage() {
        if (data.getCurrentPage() == data.getAllPagesCount()) return;
        jumpToPage(data.getCurrentPage() * data.getItemsPerPage());
    }


    public void lastPage() {
        if (data.getCurrentPage() == data.getAllPagesCount()) return;
        jumpToPage((data.getAllPagesCount() - 1) * data.getItemsPerPage());
    }

    protected final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);

    protected void updateNavigation() {
        if (data.getAllPagesCount() <= 1) {
            tabLayout.setVisibility(View.GONE);
            return;
        }
        tabLayout.setVisibility(View.VISIBLE);
        boolean prevDisabled = data.getCurrentPage() <= 1;
        boolean nextDisabled = data.getCurrentPage() == data.getAllPagesCount();
        TabLayout.Tab tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == 2) continue;
            boolean b = i < 2 ? prevDisabled : nextDisabled;
            tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getIcon() != null) {
                if (b)
                    tab.getIcon().setColorFilter(colorFilter);
                else
                    tab.getIcon().clearColorFilter();
            }
        }
    }

    private void selectPage(FavData pageData) {
        final int[] pages = new int[pageData.getAllPagesCount()];

        for (int i = 0; i < pageData.getAllPagesCount(); i++)
            pages[i] = i + 1;

        final ListView listView = new ListView(getContext());
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(true);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new ThemePagesAdapter(getContext(), pages));
        listView.setItemChecked(pageData.getCurrentPage() - 1, true);
        listView.setSelection(pageData.getCurrentPage() - 1);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(listView)
                .show();

        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        listView.setOnItemClickListener((adapterView, view1, i2, l) -> {
            if (listView.getTag() != null && !((Boolean) listView.getTag())) {
                return;
            }
            jumpToPage(i2 * pageData.getItemsPerPage());
            dialog.cancel();
        });
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Favorites().get(currentSt), this::onLoadThemes, new FavData(), v -> loadData());
    }

    private void onLoadThemes(FavData data) {
        Log.d("kek", "loaded itms " + data.getItems().size() + " : " + results.size());
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);

        this.data = data;
        if (data.getItems().size() == 0)
            return;

        realm.executeTransactionAsync(r -> {
            r.delete(FavItem.class);
            r.copyToRealmOrUpdate(data.getItems());
        }, this::bindView);
        updateNavigation();
        setSubtitle(data.getAllPagesCount() <= 1 ? null : "" + data.getCurrentPage() + "/" + data.getAllPagesCount());
    }

    private void bindView() {
        results = realm.where(FavItem.class).findAll();
        if (results.size() != 0) {
            adapter.addAll(results);
        }
        Api.get().notifyObservers();
    }

    public void changeFav(int act, String type, int id) {
        helperSubscriber.subscribe(Api.Favorites().changeFav(act, type, id), this::onChangeFav, false);
    }

    public void markRead(int topicId) {
        realm.executeTransactionAsync(realm1 -> {
            FavItem favItem = realm1.where(FavItem.class).equalTo("topicId", topicId).findFirst();
            if (favItem != null) {
                favItem.setNewMessages(false);
            }
        });
        markedRead = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (markedRead) {
            bindView();
            markedRead = false;
        }
    }

    private void onChangeFav(boolean v) {
        if (!v)
            Toast.makeText(getContext(), "При выполнении операции произошла ошибка", Toast.LENGTH_SHORT).show();
        loadData();
    }
}
