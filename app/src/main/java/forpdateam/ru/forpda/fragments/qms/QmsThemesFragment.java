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
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.utils.IntentHandler;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends TabFragment {
    public final static String defaultTitle = "Диалоги";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private int userId;
    private String avatarUrl;
    private String userNick;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private QmsThemesAdapter adapter;
    private Realm realm;
    private RealmResults<QmsThemes> results;
    private QmsThemesAdapter.OnItemClickListener onItemClickListener =
            theme -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, theme.getName());
                args.putString(TabFragment.SUBTITLE_ARG, getTitle());
                args.putInt(QmsChatFragment.USER_ID_ARG, userId);
                args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
                args.putInt(QmsChatFragment.THEME_ID_ARG, theme.getId());
                args.putString(QmsChatFragment.THEME_TITLE_ARG, theme.getName());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            };
    private Subscriber<QmsThemes> mainSubscriber = new Subscriber<>();

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Override
    public boolean isUseCache() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            userId = getArguments().getInt(USER_ID_ARG);
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        setWhiteBackground();
        baseInflateFragment(inflater, R.layout.fragment_qms_themes);
        tryShowAvatar();
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setImageDrawable(App.getAppDrawable(R.drawable.ic_create_white_24dp));
        fab.setOnClickListener(view1 -> {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, userId);
            args.putString(QmsChatFragment.USER_NICK_ARG, userNick);
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
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + userId));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Qms().getThemesList(userId), this::onLoadThemes, new QmsThemes(), v -> loadData());
    }

    private void onLoadThemes(QmsThemes data) {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);

        if (data.getThemes().size() == 0)
            return;
        userNick = data.getNick();
        setTitle(createTitle(userNick));
        if (data.getThemes().size() == 0 && userNick != null) {
            Bundle args = new Bundle();
            args.putInt(QmsChatFragment.USER_ID_ARG, userId);
            args.putString(QmsChatFragment.USER_NICK_ARG, userNick);
            args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            //new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 500);
        }
        if (results != null) {
            realm.beginTransaction();
            try {
                results.deleteFromRealm(results.indexOf(results.last()));
            } catch (Exception ignore) {
            } finally {
                realm.commitTransaction();
            }
        }
        realm.executeTransactionAsync(r -> r.copyToRealmOrUpdate(data), this::bindView);
    }

    private void bindView() {
        results = realm.where(QmsThemes.class).equalTo("userId", userId).findAll();

        if (results == null) return;
        if (results.size() != 0 && results.last().getThemes().size() != 0) {
            adapter.addAll(results.last().getThemes());
        }
    }

    public static String createTitle(String userNick) {
        //return defaultTitle.concat(" с ").concat(userNick);
        return userNick;
    }

}
