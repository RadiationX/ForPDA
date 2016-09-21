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

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsContactsFragment extends TabFragment {
    public final static String defaultTitle = "Контакты";

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private QmsContactsAdapter adapter;
    private QmsContactsAdapter.OnItemClickListener onItemClickListener =
            (view1, position, adapter1) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, QmsThemesFragment.createTitle(adapter1.getItem(position).getNick()));
                args.putString(QmsThemesFragment.USER_ID_ARG, adapter1.getItem(position).getId());
                args.putString(QmsThemesFragment.USER_AVATAR_ARG, adapter1.getItem(position).getAvatar());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsThemesFragment.class).setArgs(args).build());
            };
    private QmsContactsAdapter.OnLongItemClickListener onLongItemClickListener = (view1, position, adapter1) -> {
        CharSequence[] items = {"Удалить"};
        new AlertDialog.Builder(getContext())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        deleteDialog(adapter1.getItem(position).getId());
                    }
                }).show();
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        setWhiteBackground();
        inflater.inflate(R.layout.fragment_qms_contacts, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_contacts);
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
            private ArrayList<QmsContact> searchContacts = new ArrayList<>();
            private QmsContactsAdapter searchAdapter;

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("kek", "on query changed start");
                searchContacts.clear();
                if (!newText.isEmpty()) {
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (adapter.getItem(i).getNick().toLowerCase().contains(newText.toLowerCase())) {
                            searchContacts.add(adapter.getItem(i));
                        }
                    }
                    searchAdapter = new QmsContactsAdapter(searchContacts);
                    searchAdapter.setOnItemClickListener(onItemClickListener);
                    adapter.setOnLongItemClickListener(onLongItemClickListener);
                    recyclerView.setAdapter(searchAdapter);
                } else {
                    recyclerView.setAdapter(adapter);
                }
                Log.d("kek", "on query changed end");
                return false;
            }
        });

        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setOnClickListener(view1 -> TabManager.getInstance().add(new TabFragment.Builder<>(QmsNewThemeFragment.class).build()));
        fab.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public boolean onBackPressed() {
        Log.d("kek", "onbackpressed qms");
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
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        getCompositeSubscription().add(Api.Qms().getContactList()
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadContacts, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    public void deleteDialog(String mid) {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        getCompositeSubscription().add(Api.Qms().deleteDialog(mid)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return "";
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDeletedDialog, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadContacts(ArrayList<QmsContact> contacts) {
        Log.d("kek", "contacts loaded");
        adapter = new QmsContactsAdapter(contacts);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }

    private void onDeletedDialog(String res) {
        loadData();
    }

}
