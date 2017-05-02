package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.bdobjects.qms.QmsThemesBd;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends TabFragment {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private String avatarUrl;
    private QmsThemes currentThemes = new QmsThemes();
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private QmsThemesAdapter adapter;
    private Realm realm;
    private RealmResults<QmsThemesBd> results;
    private QmsThemesAdapter.OnItemClickListener onItemClickListener =
            theme -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, theme.getName());
                args.putString(TabFragment.TAB_SUBTITLE, getTitle());
                args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
                args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
                args.putInt(QmsChatFragment.THEME_ID_ARG, theme.getId());
                args.putString(QmsChatFragment.THEME_TITLE_ARG, theme.getName());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            };
    private Subscriber<QmsThemes> mainSubscriber = new Subscriber<>(this);

    public QmsThemesFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle("Диалоги");
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
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        tryShowAvatar();
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setOnClickListener(view1 -> {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
            args.putString(QmsChatFragment.USER_NICK_ARG, currentThemes.getNick());
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
        });
        fab.setVisibility(View.VISIBLE);
        adapter = new QmsThemesAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        bindView();
        return view;
    }

    private void tryShowAvatar() {
        if (avatarUrl != null) {
            ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + currentThemes.getUserId()));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Qms().getThemesList(currentThemes.getUserId()), this::onLoadThemes, currentThemes, v -> loadData());
    }

    private void onLoadThemes(QmsThemes themes) {
        refreshLayout.setRefreshing(false);

        recyclerView.scrollToPosition(0);
        currentThemes = themes;

        setTabTitle("Диалоги с ".concat(currentThemes.getNick()));
        setTitle(currentThemes.getNick());
        if (currentThemes.getThemes().size() == 0 && currentThemes.getNick() != null) {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, currentThemes.getUserId());
            args.putString(QmsChatFragment.USER_NICK_ARG, currentThemes.getNick());
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            //new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 500);
            return;
        }
        if (currentThemes.getThemes().size() == 0)
            return;
        if (results != null) {
            realm.beginTransaction();
            try {
                results.deleteFromRealm(results.indexOf(results.last()));
            } catch (Exception ignore) {
            } finally {
                realm.commitTransaction();
            }
        }
        realm.executeTransactionAsync(r -> {
            QmsThemesBd qmsThemesBd = new QmsThemesBd(currentThemes);
            r.copyToRealmOrUpdate(qmsThemesBd);
            qmsThemesBd.getThemes().clear();
        }, this::bindView);
    }

    private void bindView() {
        results = realm.where(QmsThemesBd.class).equalTo("userId", currentThemes.getUserId()).findAll();

        if (results.size() != 0 && results.last().getThemes().size() != 0) {
            adapter.addAll(results.last().getThemes());
        }
    }
}
