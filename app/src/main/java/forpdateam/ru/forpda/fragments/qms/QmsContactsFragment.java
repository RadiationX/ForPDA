package forpdateam.ru.forpda.fragments.qms;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.qms.QmsContactBd;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ContentController;
import forpdateam.ru.forpda.views.FunnyContent;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsFragment extends RecyclerFragment implements QmsContactsAdapter.OnItemClickListener<IQmsContact> {
    private QmsContactsAdapter adapter;
    private Subscriber<ArrayList<QmsContact>> mainSubscriber = new Subscriber<>(this);
    private Subscriber<String> helperSubscriber = new Subscriber<>(this);
    private Realm realm;
    private RealmResults<QmsContactBd> results;
    private AlertDialogMenu<QmsContactsFragment, IQmsContact> dialogMenu;

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };

    public QmsContactsFragment() {
        configuration.setAlone(true);
        configuration.setMenu(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_contacts));
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
        initFabBehavior();
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        fab.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_fab_create));
        fab.setOnClickListener(view1 -> TabManager.getInstance().add(QmsChatFragment.class));
        fab.setVisibility(View.VISIBLE);

        adapter = new QmsContactsAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        bindView();
        App.get().subscribeQms(notification);
        return view;
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        toolbar.inflateMenu(R.menu.qms_contacts_menu);
        MenuItem searchItem = getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));
        }

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private ArrayList<QmsContactBd> searchContacts = new ArrayList<>();

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContacts.clear();
                if (!newText.isEmpty()) {
                    for (QmsContactBd contact : results) {
                        if (contact.getNick().toLowerCase().contains(newText.toLowerCase()))
                            searchContacts.add(contact);
                    }
                    adapter.addAll(searchContacts);
                } else {
                    adapter.addAll(results);
                }
                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.user));
        getMenu().add(R.string.blacklist)
                .setOnMenuItemClickListener(item -> {
                    TabManager.getInstance().add(QmsBlackListFragment.class);
                    return false;
                });
    }


    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        if (getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            recyclerView.setAdapter(adapter);
            toolbar.collapseActionView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void loadData() {
        super.loadData();
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().getContactList(), this::onLoadContacts, new ArrayList<>(), v -> loadData());
    }

    private void onLoadContacts(ArrayList<QmsContact> data) {
        setRefreshing(false);
        recyclerView.scrollToPosition(0);

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact contact : data) {
                bdList.add(new QmsContactBd(contact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);
    }

    private ArrayList<QmsContact> currentItems = new ArrayList<>();

    private void bindView() {
        setRefreshing(false);
        if (realm.isClosed()) return;
        results = realm.where(QmsContactBd.class).findAll();

        if (results.isEmpty()) {
            if(!contentController.contains(ContentController.TAG_NO_DATA)){
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_contacts)
                        .setTitle(R.string.funny_contacts_nodata_title);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        currentItems.clear();
        for (QmsContactBd qmsContactBd : results) {
            QmsContact contact = new QmsContact(qmsContactBd);
            currentItems.add(contact);
        }
        int count = 0;
        for (QmsContact contact : currentItems) {
            if (contact.getCount() > 0) {
                count += contact.getCount();
            }
        }

        ClientHelper.setQmsCount(count);
        ClientHelper.getInstance().notifyCountsChanged();

        adapter.addAll(currentItems);
    }

    private void handleEvent(TabNotification event) {
        SparseIntArray sparseArray = new SparseIntArray();

        for (NotificationEvent loadedEvent : event.getLoadedEvents()) {
            int count = sparseArray.get(loadedEvent.getUserId());
            count += loadedEvent.getMsgCount();
            sparseArray.put(loadedEvent.getUserId(), count);
        }
        for (int i = sparseArray.size() - 1; i >= 0; i--) {
            int id = sparseArray.keyAt(i);
            int count = sparseArray.valueAt(i);
            for (QmsContact item : currentItems) {
                if (item.getId() == id) {
                    item.setCount(count);
                    Collections.swap(currentItems, currentItems.indexOf(item), 0);
                    break;
                }
            }
        }

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact qmsContact : currentItems) {
                bdList.add(new QmsContactBd(qmsContact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);

        //adapter.notifyDataSetChanged();
        /*ArrayList<IFavItem> newItems = new ArrayList<>();
        newItems.addAll(currentItems);
        refreshList(newItems);*/
    }

    public void updateCount(int id, int count) {
        /*for (QmsContact item : currentItems) {
            if (item.getId() == id) {
                item.setCount(count);
                break;
            }
        }
        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(QmsContactBd.class);
            List<QmsContactBd> bdList = new ArrayList<>();
            for (QmsContact qmsContact : currentItems) {
                bdList.add(new QmsContactBd(qmsContact));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);*/
    }

    public void deleteDialog(int mid) {
        setRefreshing(true);
        helperSubscriber.subscribe(RxApi.Qms().deleteDialog(mid), this::onDeletedDialog, "");
    }

    private void onDeletedDialog(String res) {
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        App.get().unSubscribeQms(notification);
    }

    @Override
    public void onItemClick(IQmsContact item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getNick());
        args.putInt(QmsThemesFragment.USER_ID_ARG, item.getId());
        args.putString(QmsThemesFragment.USER_AVATAR_ARG, item.getAvatar());
        TabManager.getInstance().add(QmsThemesFragment.class, args);
    }

    @Override
    public boolean onItemLongClick(IQmsContact item) {
        if (dialogMenu == null) {
            dialogMenu = new AlertDialogMenu<>();
            dialogMenu.addItem(getString(R.string.profile), (context, data) -> {
                IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + data.getId());
            });
            dialogMenu.addItem(getString(R.string.add_to_blacklist), (context, data) -> {
                mainSubscriber.subscribe(RxApi.Qms().blockUser(data.getNick()), qmsContacts -> {
                    if (!qmsContacts.isEmpty()) {
                        Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
                    }
                }, new ArrayList<>());
            });
            dialogMenu.addItem(getString(R.string.delete), (context, data) -> context.deleteDialog(data.getId()));
            dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> {
                String title = String.format(getString(R.string.dialogs_Nick), data.getNick());
                String url = "http://4pda.ru/forum/index.php?act=qms&mid=" + data.getId();
                NotesAddPopup.showAddNoteDialog(context1.getContext(), title, url);
            });
        }
        new AlertDialog.Builder(getContext())
                .setItems(dialogMenu.getTitles(), (dialog, which) -> dialogMenu.onClick(which, QmsContactsFragment.this, item)).show();
        return false;
    }
}
