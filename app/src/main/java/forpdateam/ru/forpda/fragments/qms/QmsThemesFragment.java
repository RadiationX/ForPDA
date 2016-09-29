package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.IntentHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends TabFragment {
    public final static String defaultTitle = "Диалоги";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private String userId;
    private String avatarUrl;
    private String userNick;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private QmsThemesAdapter adapter;
    private QmsThemesAdapter.OnItemClickListener onItemClickListener =
            (view1, position, adapter1) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, adapter1.getItem(position).getName());
                args.putString(TabFragment.SUBTITLE_ARG, getTitle());
                args.putString(QmsChatFragment.USER_ID_ARG, userId);
                args.putString(QmsChatFragment.USER_AVATAR_ARG, avatarUrl);
                args.putString(QmsChatFragment.THEME_ID_ARG, adapter1.getItem(position).getId());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            };

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID_ARG);
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        setWhiteBackground();
        inflater.inflate(R.layout.fragment_qms_themes, (ViewGroup) view.findViewById(R.id.fragment_content), true);
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
            args.putString(QmsNewThemeFragment.USER_ID_ARG, userId);
            args.putString(QmsNewThemeFragment.USER_NICK_ARG, userNick);
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsNewThemeFragment.class).setArgs(args).build());
        });
        fab.setVisibility(View.VISIBLE);
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
        getCompositeDisposable().add(Api.Qms().getThemesList(userId)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new QmsThemes();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadThemes, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadThemes(QmsThemes qmsThemes) {
        refreshLayout.setRefreshing(false);
        userNick = qmsThemes.getNick();
        if (qmsThemes.getThemes().size() == 0 && userNick != null) {
            Bundle args = new Bundle();
            args.putString(QmsNewThemeFragment.USER_ID_ARG, userId);
            args.putString(QmsNewThemeFragment.USER_NICK_ARG, userNick);
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsNewThemeFragment.class).setArgs(args).build());
            //new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 500);
        }
        adapter = new QmsThemesAdapter(qmsThemes.getThemes());
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        setTitle(createTitle(userNick));
    }

    public static String createTitle(String userNick) {
        return defaultTitle.concat(" с ").concat(userNick);
    }

}
