package forpdateam.ru.forpda.fragments.qms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.trello.rxlifecycle.FragmentEvent;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ScrollAwareFABBehavior;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsChatAdapter;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsContactsAdapter;
import forpdateam.ru.forpda.utils.IntentHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment {
    public final static String defaultTitle = "Чат";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    private String userId;
    private String avatar;
    private String themeId;
    private TextView textView;
    private RecyclerView recyclerView;
    private QmsChatAdapter adapter;

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID_ARG);
            themeId = getArguments().getString(THEME_ID_ARG);
            avatar = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_qms_chat, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        ImageLoader.getInstance().displayImage(avatar, toolbarImageView);
        toolbarImageView.setVisibility(View.VISIBLE);
        toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser="+userId));
        recyclerView = (RecyclerView) findViewById(R.id.qms_chat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void loadData() {
        getCompositeSubscription().add(Api.Qms().getChat(userId, themeId)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::onLoadChat, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onLoadChat(ArrayList<QmsChatItem> qmsChatItems) {
        adapter = new QmsChatAdapter(qmsChatItems);
        recyclerView.setAdapter(adapter);
    }
}
