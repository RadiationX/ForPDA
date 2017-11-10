package forpdateam.ru.forpda.ui.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.rx.Subscriber;
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;

/**
 * Created by radiationx on 22.03.17.
 */

public class QmsBlackListFragment extends RecyclerFragment implements QmsContactsAdapter.OnItemClickListener<IQmsContact> {
    private AppCompatAutoCompleteTextView nickField;
    private QmsContactsAdapter adapter;
    private Subscriber<ArrayList<QmsContact>> mainSubscriber = new Subscriber<>(this);
    private DynamicDialogMenu<QmsBlackListFragment, IQmsContact> dialogMenu;
    private ArrayList<QmsContact> currentData;
    private Subscriber<List<ForumUser>> searchUserSubscriber = new Subscriber<>(this);

    public QmsBlackListFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_blacklist));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_black_list);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_black_list_nick_field);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        nickField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }
        });

        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new QmsContactsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add(R.string.add)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_toolbar_add))
                .setOnMenuItemClickListener(item -> {
                    String nick = "";
                    if (nickField.getText() != null)
                        nick = nickField.getText().toString();
                    blockUser(nick);
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void someClick(IQmsContact contact) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();
            dialogMenu.addItem(getString(R.string.profile), (context, data) -> IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + data.getId()));
            dialogMenu.addItem(getString(R.string.dialogs), (context, data) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, data.getNick());
                args.putInt(QmsThemesFragment.USER_ID_ARG, data.getId());
                args.putString(QmsThemesFragment.USER_AVATAR_ARG, data.getAvatar());
                TabManager.get().add(QmsThemesFragment.class, args);
            });
            dialogMenu.addItem(getString(R.string.delete), (context, data) -> context.unBlockUser(new int[]{data.getId()}));
        }
        dialogMenu.disallowAll();
        dialogMenu.allowAll();
        dialogMenu.show(getContext(), QmsBlackListFragment.this, contact);
    }


    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().getBlackList(), this::onLoadContacts, new ArrayList<>(), v -> loadData());
        return true;
    }

    private void onLoadContacts(ArrayList<QmsContact> data) {
        setRefreshing(false);
        if (data.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_contacts)
                        .setTitle(R.string.funny_blacklist_nodata_title)
                        .setDesc(R.string.funny_blacklist_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }
        recyclerView.scrollToPosition(0);
        currentData = data;
        adapter.addAll(currentData);
    }

    private void blockUser(String nick) {
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().blockUser(nick), this::onEditedList, currentData, null);
    }

    private void unBlockUser(int[] userIds) {
        setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().unBlockUsers(userIds), this::onEditedList, currentData, null);
    }

    private void onEditedList(ArrayList<QmsContact> data) {
        setRefreshing(false);
        if (currentData == data) return;
        currentData = data;
        adapter.addAll(currentData);
        nickField.setText("");
    }

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new ArrayList<>());
    }

    private void onShowSearchRes(List<ForumUser> res) {
        List<String> nicks = new ArrayList<>();
        for (ForumUser user : res) {
            nicks.add(user.getNick());
        }
        nickField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, nicks));
    }

    @Override
    public void onItemClick(IQmsContact item) {
        someClick(item);
    }

    @Override
    public boolean onItemLongClick(IQmsContact item) {
        someClick(item);
        return false;
    }
}
