package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.rx.Subscriber;

/**
 * Created by radiationx on 22.03.17.
 */

public class QmsBlackListFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private AppCompatAutoCompleteTextView nickField;
    private QmsContactsAdapter adapter;
    private Subscriber<ArrayList<QmsContact>> mainSubscriber = new Subscriber<>(this);
    private AlertDialogMenu<QmsBlackListFragment, IQmsContact> contactDialogMenu;
    private ArrayList<QmsContact> currentData;
    private Subscriber<String[]> searchUserSubscriber = new Subscriber<>(this);

    public QmsBlackListFragment() {
        configuration.setDefaultTitle("Изгои мира сего");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_black_list);
        viewStub.inflate();
        setWhiteBackground();
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_black_list_nick_field);
        viewsReady();
        nickField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }
        });
        toolbar.getMenu().add("Добавить").setIcon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_add_gray_24dp)).setOnMenuItemClickListener(item -> {
            String nick = "";
            if (nickField.getText() != null)
                nick = nickField.getText().toString();
            blockUser(nick);
            return false;
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new QmsContactsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnLongItemClickListener(this::someClick);
        adapter.setOnItemClickListener(this::someClick);
        return view;
    }

    private void someClick(IQmsContact contact) {
        if (contactDialogMenu == null) {
            contactDialogMenu = new AlertDialogMenu<>();
            contactDialogMenu.addItem("Профиль", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data.getId()));
            contactDialogMenu.addItem("Диалоги", (context, data) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, data.getNick());
                args.putInt(QmsThemesFragment.USER_ID_ARG, data.getId());
                args.putString(QmsThemesFragment.USER_AVATAR_ARG, data.getAvatar());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsThemesFragment.class).setArgs(args).build());
            });
            contactDialogMenu.addItem("Удалить", (context, data) -> context.unBlockUser(new int[]{data.getId()}));
        }
        new AlertDialog.Builder(getContext())
                .setItems(contactDialogMenu.getTitles(), (dialog, which) -> contactDialogMenu.onClick(which, QmsBlackListFragment.this, contact)).show();
    }


    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().getBlackList(), this::onLoadContacts, new ArrayList<>(), v -> loadData());
    }

    private void onLoadContacts(ArrayList<QmsContact> data) {
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        currentData = data;
        adapter.addAll(currentData);
    }

    private void blockUser(String nick) {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().blockUser(nick), this::onEditedList, currentData, null);
    }

    private void unBlockUser(int[] userIds) {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().unBlockUsers(userIds), this::onEditedList, currentData, null);
    }

    private void onEditedList(ArrayList<QmsContact> data) {
        refreshLayout.setRefreshing(false);
        if (currentData == data) return;
        currentData = data;
        adapter.addAll(currentData);
        nickField.setText("");
    }

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new String[]{});
    }

    private void onShowSearchRes(String[] res) {
        nickField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, res));
    }

}
