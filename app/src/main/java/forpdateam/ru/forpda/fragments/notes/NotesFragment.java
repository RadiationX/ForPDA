package forpdateam.ru.forpda.fragments.notes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.data.models.notes.NoteItem;
import forpdateam.ru.forpda.data.realm.history.HistoryItemBd;
import forpdateam.ru.forpda.data.realm.notes.NoteItemBd;
import forpdateam.ru.forpda.fragments.ListFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.fragments.history.HistoryFragment;
import forpdateam.ru.forpda.fragments.notes.adapters.NotesAdapter;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 06.09.17.
 */

public class NotesFragment extends ListFragment implements NotesAdapter.ClickListener, NotesAddPopup.NoteActionListener {
    private NotesAdapter adapter;
    private Realm realm;
    private AlertDialogMenu<NotesFragment, NoteItem> dialogMenu, showedDialogMenu;

    public NotesFragment() {
        configuration.setDefaultTitle("Заметки");
        configuration.setUseCache(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCardsBackground();
        adapter = new NotesAdapter();
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadCacheData);
        recyclerView.addItemDecoration(new BrandFragment.SpacingItemDecoration(App.px8, false));
        setCardsBackground(recyclerView);
        return view;
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu()
                .add("Добавить")
                .setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_add))
                .setOnMenuItemClickListener(item -> {
                    new NotesAddPopup(getContext(), null, this);
                    return true;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
        if (!realm.isClosed()) {
            refreshLayout.setRefreshing(true);
            RealmResults<NoteItemBd> results = realm.where(NoteItemBd.class).findAllSorted("id", Sort.DESCENDING);

            ArrayList<NoteItem> nonBdResult = new ArrayList<>();
            for (NoteItemBd item : results) {
                nonBdResult.add(new NoteItem(item));
            }
            adapter.addAll(nonBdResult);
        }
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onAddNote(NoteItem item) {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            NoteItemBd itemBd = realm1.where(NoteItemBd.class).equalTo("id", item.getId()).findFirst();
            if (itemBd != null) {
                itemBd.setTitle(item.getTitle());
                itemBd.setLink(item.getLink());
                itemBd.setContent(item.getContent());
            } else {
                itemBd = new NoteItemBd(item);
            }
            realm1.insertOrUpdate(itemBd);
        }, this::loadCacheData);
    }

    public void deleteNote(long id) {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            realm1.where(NoteItemBd.class)
                    .equalTo("id", id)
                    .findAll()
                    .deleteAllFromRealm();
        }, this::loadCacheData);
    }

    @Override
    public void onItemClick(NoteItem item, int position) {
        try {
            IntentHandler.handle(item.getLink());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onLongItemClick(NoteItem item, int position) {
        if (dialogMenu == null) {
            dialogMenu = new AlertDialogMenu<>();
            showedDialogMenu = new AlertDialogMenu<>();
            dialogMenu.addItem("Скопировать ссылку", (context, data) -> {
                Utils.copyToClipBoard("" + data.getLink());
            });
            dialogMenu.addItem("Редактировать", (context, data) -> {
                new NotesAddPopup(context.getContext(), data, context);
            });
            dialogMenu.addItem("Удалить", (context, data) -> {
                context.deleteNote(data.getId());
            });
        }
        showedDialogMenu.clear();
        showedDialogMenu.addItem(dialogMenu.get(0));
        showedDialogMenu.addItem(dialogMenu.get(1));
        showedDialogMenu.addItem(dialogMenu.get(2));

        new AlertDialog.Builder(getContext())
                .setItems(showedDialogMenu.getTitles(), (dialog, which) -> {
                    showedDialogMenu.onClick(which, NotesFragment.this, item);
                })
                .show();
        return true;
    }

    public static void addNote(NoteItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(realm1 -> {
            realm1.insertOrUpdate(new NoteItemBd(item));
        }, () -> {
            realm.close();
            NotesFragment notesFragment = (NotesFragment) TabManager.getInstance().getByClass(NotesFragment.class);
            if (notesFragment == null) {
                return;
            }
            notesFragment.loadCacheData();
        });

    }


}
