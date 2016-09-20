package forpdateam.ru.forpda.fragments.qms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsTheme;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsThemesAdapter;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.IntentHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsThemesFragment extends TabFragment {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    private String userId;
    private String avatar;
    private RecyclerView recyclerView;
    private QmsThemesAdapter adapter;
    private QmsThemesAdapter.OnItemClickListener onItemClickListener =
            (view1, position, adapter1) -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.TITLE_ARG, adapter1.getItem(position).getName());
                args.putString(TabFragment.SUBTITLE_ARG, getTitle());
                args.putString(QmsChatFragment.USER_ID_ARG, userId);
                args.putString(QmsChatFragment.USER_AVATAR_ARG, avatar);
                args.putString(QmsChatFragment.THEME_ID_ARG, adapter1.getItem(position).getId());
                TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID_ARG);
            avatar = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        setWhiteBackground();
        inflater.inflate(R.layout.fragment_qms_themes, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        ImageLoader.getInstance().displayImage(avatar, toolbarImageView);
        toolbarImageView.setVisibility(View.VISIBLE);
        toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + userId));
        recyclerView = (RecyclerView) findViewById(R.id.qms_list_themes);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setImageDrawable(AppCompatResources.getDrawable(App.getContext(), R.drawable.ic_create_white_24dp));
        fab.setOnClickListener(view1 -> {
            Bundle args = new Bundle();
            args.putString(QmsNewThemeFragment.USER_ID_ARG, userId);
            args.putString(QmsNewThemeFragment.USER_NICK_ARG, getTitle());
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsNewThemeFragment.class).setArgs(args).build());
        });
        fab.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void loadData() {
        getCompositeSubscription().add(Api.Qms().getThemesList(userId)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoadThemes, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadThemes(ArrayList<QmsTheme> qmsThemes) {

        adapter = new QmsThemesAdapter(qmsThemes);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
    }

}
