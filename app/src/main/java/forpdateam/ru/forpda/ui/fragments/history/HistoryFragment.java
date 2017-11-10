package forpdateam.ru.forpda.ui.fragments.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.data.realm.history.HistoryItemBd;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 06.09.17.
 */

public class HistoryFragment extends RecyclerFragment implements HistoryAdapter.OnItemClickListener<HistoryItemBd> {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault());
    private HistoryAdapter adapter;
    private Realm realm;
    private DynamicDialogMenu<HistoryFragment, HistoryItemBd> dialogMenu;

    public HistoryFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_history));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(this::loadCacheData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        viewsReady();
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add("Удалить историю")
                .setOnMenuItemClickListener(item -> {
                    clear();
                    return false;
                });
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
        if (!realm.isClosed()) {
            setRefreshing(true);
            RealmResults<HistoryItemBd> results = realm.where(HistoryItemBd.class).findAllSorted("unixTime", Sort.DESCENDING);
            if (results.isEmpty()) {
                if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                    FunnyContent funnyContent = new FunnyContent(getContext())
                            .setImage(R.drawable.ic_history)
                            .setTitle(R.string.funny_history_nodata_title)
                            .setDesc(R.string.funny_history_nodata_desc);
                    contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
                }
                contentController.showContent(ContentController.TAG_NO_DATA);
            } else {
                contentController.hideContent(ContentController.TAG_NO_DATA);
            }
            adapter.addAll(results);
        }
        setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onItemClick(HistoryItemBd item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle(item.getUrl(), args);
    }

    @Override
    public boolean onItemLongClick(HistoryItemBd item) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> {
                Utils.copyToClipBoard(data.getUrl());
            });
            dialogMenu.addItem(getString(R.string.delete), (context, data) -> {
                context.delete(data.getId());
            });
        }
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), HistoryFragment.this, item);
        return true;
    }

    private void delete(int id) {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            realm1.where(HistoryItemBd.class)
                    .equalTo("id", id)
                    .findAll()
                    .deleteAllFromRealm();
        }, this::loadCacheData);
    }

    private void clear() {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            realm1.delete(HistoryItemBd.class);
        }, this::loadCacheData);
    }

    public static void addToHistory(int id, String url, String title) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(realm1 -> {
            HistoryItemBd item = realm1
                    .where(HistoryItemBd.class)
                    .equalTo("id", id)
                    .findFirst();
            if (item == null) {
                HistoryItemBd newItem = new HistoryItemBd();
                newItem.setTitle(title);
                newItem.setId(id);
                newItem.setUrl(url);
                newItem.setUnixTime(System.currentTimeMillis());
                newItem.setDate(dateFormat.format(new Date(newItem.getUnixTime())));
                realm1.insert(newItem);
            } else {
                item.setUrl(url);
                item.setUnixTime(System.currentTimeMillis());
                item.setDate(dateFormat.format(new Date(item.getUnixTime())));
                realm1.insertOrUpdate(item);
            }
        }, () -> {
            realm.close();
            HistoryFragment historyFragment = (HistoryFragment) TabManager.get().getByClass(HistoryFragment.class);
            if (historyFragment == null) {
                return;
            }
            historyFragment.loadCacheData();
        });

    }


}
