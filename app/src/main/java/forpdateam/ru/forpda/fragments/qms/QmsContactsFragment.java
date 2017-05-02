package forpdateam.ru.forpda.fragments.qms;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.bdobjects.qms.QmsContactBd;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private QmsContactsAdapter adapter;
    private Subscriber<ArrayList<QmsContact>> mainSubscriber = new Subscriber<>(this);
    private Subscriber<String> helperSubscriber = new Subscriber<>(this);
    private Realm realm;
    private RealmResults<QmsContactBd> results;
    private AlertDialogMenu<QmsContactsFragment, IQmsContact> contactDialogMenu;
    private QmsContactsAdapter.OnItemClickListener onItemClickListener =
            contact -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, contact.getNick());
                args.putInt(QmsThemesFragment.USER_ID_ARG, contact.getId());
                args.putString(QmsThemesFragment.USER_AVATAR_ARG, contact.getAvatar());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsThemesFragment.class).setArgs(args).build());
            };

    private QmsContactsAdapter.OnLongItemClickListener onLongItemClickListener = contact -> {
        if (contactDialogMenu == null) {
            contactDialogMenu = new AlertDialogMenu<>();
            contactDialogMenu.addItem("В черный список", (context, data) -> {
                mainSubscriber.subscribe(RxApi.Qms().blockUser(data.getNick()), qmsContacts -> {
                    if (qmsContacts.size() > 0) {
                        Toast.makeText(getContext(), "Пользователь добавлен в черный список", Toast.LENGTH_SHORT).show();
                    }
                }, new ArrayList<>());
            });
            contactDialogMenu.addItem("Удалить", (context, data) -> context.deleteDialog(data.getId()));
        }
        new AlertDialog.Builder(getContext())
                .setItems(contactDialogMenu.getTitles(), (dialog, which) -> contactDialogMenu.onClick(which, QmsContactsFragment.this, contact)).show();
    };


    public QmsContactsFragment() {
        configuration.setAlone(true);
        configuration.setMenu(true);
        configuration.setDefaultTitle("Контакты");
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
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        toolbar.inflateMenu(R.menu.qms_contacts_menu);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
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
                Log.d("FORPDA_LOG", "on query changed start");
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
                Log.d("FORPDA_LOG", "on query changed end");
                return false;
            }
        });

        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setOnClickListener(view1 -> TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).build()));
        fab.setVisibility(View.VISIBLE);

        adapter = new QmsContactsAdapter();
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);

        toolbar.getMenu().add("Черный список").setOnMenuItemClickListener(item -> {
            TabManager.getInstance().add(new Builder<>(QmsBlackListFragment.class).build());
            return false;
        });

        bindView();
        return view;
    }

    @Override
    public boolean onBackPressed() {
        Log.d("FORPDA_LOG", "onbackpressed qms");
        if (toolbar.getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            recyclerView.setAdapter(adapter);
            toolbar.collapseActionView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().getContactList(), this::onLoadContacts, new ArrayList<>(), v -> loadData());
    }

    private void onLoadContacts(ArrayList<QmsContact> data) {
        Log.d("FORPDA_LOG", "loaded itms " + data.size() + " : " + results.size());
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        if (data.size() == 0)
            return;

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

    private void bindView() {
        results = realm.where(QmsContactBd.class).findAll();
        if (results.size() != 0) {
            adapter.addAll(results);
        }
    }

    public void deleteDialog(int mid) {
        refreshLayout.setRefreshing(true);
        helperSubscriber.subscribe(RxApi.Qms().deleteDialog(mid), this::onDeletedDialog, "");
    }

    private void onDeletedDialog(String res) {
        loadData();
    }

}
