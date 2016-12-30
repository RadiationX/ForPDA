package forpdateam.ru.forpda.fragments.qms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsChatAdapter;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String TAB_TAG_FOR_REMOVE = "TAB_TAG_FOR_REMOVE";
    private int userId;
    private String avatarUrl;
    private int themeId;
    private RecyclerView recyclerView;
    private CardView cardView;

    private QmsChatAdapter.OnItemClickListener onItemClickListener = message -> {
        Toast.makeText(getContext(), "ONCLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };
    private QmsChatAdapter.OnLongItemClickListener onLongItemClickListener = message -> {
        Toast.makeText(getContext(), "ON LONG CLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>();

    @Override
    public String getDefaultTitle() {
        return "Чат";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(USER_ID_ARG);
            themeId = getArguments().getInt(THEME_ID_ARG);
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        recyclerView = (RecyclerView) findViewById(R.id.qms_chat);
        cardView = (CardView) findViewById(R.id.qms_chat_input_block);
        viewsReady();
        initField();
        tryShowAvatar();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
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
        mainSubscriber.subscribe(Api.Qms().getChat(userId, themeId), this::onLoadChat, new QmsChatModel(), v -> loadData());
    }


    private void onLoadChat(QmsChatModel chat) {
        QmsChatAdapter adapter = new QmsChatAdapter(chat.getChatItemsList(), getContext());
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);
        setTitle(chat.getTitle());
        setSubtitle(chat.getNick());
        if (avatarUrl == null) {
            avatarUrl = chat.getAvatarUrl();
            tryShowAvatar();
            //TabManager.getInstance().remove(getParentTag());
        }
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }


    private void initField() {
        cardView.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) cardView.getLayoutParams();
        // TODO: 20.12.16 not work in 25.1.0
        params.setBehavior(new InputFieldBehavior(cardView.getContext(), null));
        cardView.requestLayout();
    }

    public class InputFieldBehavior extends CoordinatorLayout.Behavior<CardView> {
        private int scrolled = 0;

        public InputFieldBehavior(Context context, AttributeSet attrs) {
            super();
        }

        @Override
        public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final CardView child,
                                           final View directTargetChild, final View target, final int nestedScrollAxes) {
            return true;
        }

        @Override
        public void onNestedScroll(final CoordinatorLayout coordinatorLayout,
                                   final CardView child,
                                   final View target, final int dxConsumed, final int dyConsumed,
                                   final int dxUnconsumed, final int dyUnconsumed) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            scrolled += dyConsumed + dyUnconsumed;
            scrolled = Math.max(scrolled, -child.getMeasuredHeight() - (2 * App.px8));
            scrolled = Math.min(scrolled, 0);
            child.setTranslationY(-(float) scrolled);
        }
    }

}
