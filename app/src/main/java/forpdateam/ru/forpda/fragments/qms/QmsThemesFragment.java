package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.qms.QmsThemeBd;
import forpdateam.ru.forpda.data.realm.qms.QmsThemesBd;
import forpdateam.ru.forpda.fragments.RecyclerFragment;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends RecyclerFragment implements QmsThemesAdapter.OnItemClickListener<IQmsTheme> {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private MenuItem blackListMenuItem;
    private MenuItem noteMenuItem;
    private String avatarUrl;
    private QmsThemes currentThemes = new QmsThemes();
    private QmsThemesAdapter adapter;
    private Realm realm;
    private Subscriber<QmsThemes> mainSubscriber = new Subscriber<>(this);
    private Subscriber<ArrayList<QmsContact>> contactsSubscriber = new Subscriber<>(this);
    private AlertDialogMenu<QmsThemesFragment, IQmsTheme> dialogMenu;

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };

    public QmsThemesFragment() {
        //configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_dialogs));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            currentThemes.setUserId(getArguments().getInt(USER_ID_ARG));
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initFabBehavior();
        tryShowAvatar();
        contentController.setFirstLoad(false);
        viewsReady();


        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setImageDrawable(App.getVecDrawable(getContext(), R.drawable.ic_fab_create));
        fab.setOnClickListener(view1 -> {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
            args.putString(QmsChatFragment.USER_NICK_ARG, currentThemes.getNick());
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
            TabManager.getInstance().add(QmsChatFragment.class, args);
        });
        fab.setVisibility(View.VISIBLE);
        adapter = new QmsThemesAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        bindView();
        App.get().subscribeQms(notification);
        return view;
    }

    private void tryShowAvatar() {
        if (avatarUrl != null) {
            ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + currentThemes.getUserId()));
            toolbarImageView.setContentDescription(App.get().getString(R.string.user_avatar));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }


    @Override
    public void loadData() {
        super.loadData();
        setRefreshing(true);

        refreshToolbarMenuItems(false);

        mainSubscriber.subscribe(RxApi.Qms().getThemesList(currentThemes.getUserId()), this::onLoadThemes, currentThemes, v -> loadData());
    }

    private void onLoadThemes(QmsThemes themes) {
        setRefreshing(false);

        recyclerView.scrollToPosition(0);
        currentThemes = themes;

        setTabTitle(String.format(getString(R.string.dialogs_Nick), currentThemes.getNick()));
        setTitle(currentThemes.getNick());
        if (currentThemes.getThemes().isEmpty() && currentThemes.getNick() != null) {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
            args.putString(QmsChatFragment.USER_NICK_ARG, currentThemes.getNick());
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
            TabManager.getInstance().add(QmsChatFragment.class, args);
            //new Handler().postDelayed(() -> TabManager.get().remove(getTag()), 500);
        }

        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.where(QmsThemesBd.class).equalTo("userId", currentThemes.getUserId()).findAll().deleteAllFromRealm();
            QmsThemesBd qmsThemesBd = new QmsThemesBd(currentThemes);
            r.copyToRealmOrUpdate(qmsThemesBd);
            qmsThemesBd.getThemes().clear();
        }, this::bindView);
    }

    private void bindView() {
        if (realm.isClosed()) return;
        refreshToolbarMenuItems(true);
        RealmResults<QmsThemesBd> results = realm
                .where(QmsThemesBd.class)
                .equalTo("userId", currentThemes.getUserId())
                .findAll();

        QmsThemesBd qmsThemesBd = results.last(null);
        if (qmsThemesBd == null) {
            return;
        }
        currentItems.clear();
        for (QmsThemeBd qmsThemeBd : qmsThemesBd.getThemes()) {
            QmsTheme qmsTheme = new QmsTheme(qmsThemeBd);
            currentItems.add(qmsTheme);
        }
        adapter.addAll(currentItems);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<QmsTheme> currentItems = new ArrayList<>();

    private void handleEvent(TabNotification event) {
        if (event.getType() == NotificationEvent.Type.READ) {
            for (QmsTheme item : currentItems) {
                if (item.getId() == event.getEvent().getSourceId()) {
                    item.setCountNew(0);
                    break;
                }
            }
        } else {
            SparseIntArray sparseArray = new SparseIntArray();
            for (NotificationEvent loadedEvent : event.getLoadedEvents()) {
                int count = sparseArray.get(loadedEvent.getSourceId());
                count += loadedEvent.getMsgCount();
                sparseArray.put(loadedEvent.getSourceId(), count);
            }
            for (int i = sparseArray.size() - 1; i >= 0; i--) {
                int id = sparseArray.keyAt(i);
                int count = sparseArray.valueAt(i);
                for (QmsTheme item : currentItems) {
                    if (item.getId() == id) {
                        item.setCountMessages(item.getCountMessages() + count);
                        item.setCountNew(count);
                        Collections.swap(currentItems, currentItems.indexOf(item), 0);
                        break;
                    }
                }
            }
        }



        QmsContactsFragment contactsFragment = (QmsContactsFragment) TabManager.getInstance().getByClass(QmsContactsFragment.class);
        if (contactsFragment != null) {
            int count = 0;
            for (QmsTheme qmsTheme : currentItems) {
                count += qmsTheme.getCountNew();
            }
            contactsFragment.updateCount(currentThemes.getUserId(), count);
        }
        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.where(QmsThemesBd.class).equalTo("userId", currentThemes.getUserId()).findAll().deleteAllFromRealm();
            currentThemes.getThemes().clear();
            currentThemes.getThemes().addAll(currentItems);
            QmsThemesBd qmsThemesBd = new QmsThemesBd(currentThemes);
            r.copyToRealmOrUpdate(qmsThemesBd);
            qmsThemesBd.getThemes().clear();
        }, this::bindView);
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        blackListMenuItem = getMenu().add(R.string.add_to_blacklist)
                .setOnMenuItemClickListener(item -> {
                    contactsSubscriber.subscribe(RxApi.Qms().blockUser(currentThemes.getNick()), qmsContacts -> {
                        if (!qmsContacts.isEmpty()) {
                            Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
                        }
                    }, new ArrayList<>());
                    return false;
                });
        noteMenuItem = getMenu().add(R.string.create_note)
                .setOnMenuItemClickListener(item -> {
                    String title = String.format(getString(R.string.dialogs_Nick), currentThemes.getNick());
                    String url = "https://4pda.ru/forum/index.php?act=qms&mid=" + currentThemes.getUserId();
                    NotesAddPopup.showAddNoteDialog(getContext(), title, url);
                    return true;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            blackListMenuItem.setEnabled(true);
            noteMenuItem.setEnabled(true);
        } else {
            blackListMenuItem.setEnabled(false);
            noteMenuItem.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        App.get().unSubscribeQms(notification);
    }

    @Override
    public void onItemClick(IQmsTheme item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getName());
        args.putString(TabFragment.TAB_SUBTITLE, getTitle());
        args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
        args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
        args.putInt(QmsChatFragment.THEME_ID_ARG, item.getId());
        args.putString(QmsChatFragment.THEME_TITLE_ARG, item.getName());
        TabManager.getInstance().add(QmsChatFragment.class, args);
    }

    @Override
    public boolean onItemLongClick(IQmsTheme item) {
        if (dialogMenu == null) {
            dialogMenu = new AlertDialogMenu<>();
            dialogMenu.addItem(getString(R.string.delete), (context, data) -> {
                mainSubscriber.subscribe(RxApi.Qms().deleteTheme(currentThemes.getUserId(), data.getId()), this::onLoadThemes, currentThemes, v -> loadData());
            });
            dialogMenu.addItem(getString(R.string.create_note), (context1, data) -> {
                String title = String.format(getString(R.string.dialog_Title_Nick), data.getName(), currentThemes.getNick());
                String url = "https://4pda.ru/forum/index.php?act=qms&mid=" + currentThemes.getUserId() + "&t=" + data.getId();
                NotesAddPopup.showAddNoteDialog(context1.getContext(), title, url);
            });
        }
        new AlertDialog.Builder(getContext())
                .setItems(dialogMenu.getTitles(), (dialog, which) -> dialogMenu.onClick(which, QmsThemesFragment.this, item)).show();
        return false;
    }
}
