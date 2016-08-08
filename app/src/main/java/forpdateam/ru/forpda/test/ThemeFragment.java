package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle.FragmentEvent;

import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabFragment;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 05.08.16.
 */
public class ThemeFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/forum/index.php?showtopic=271502&st=0";
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Date date;
    private TextView text;
    private EditText searchText;
    private Button search;

    public static ThemeFragment newInstance(String tabTitle) {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString("TabTitle", tabTitle);
        fragment.setArguments(args);
        fragment.setUID();
        return fragment;
    }

    @Override
    public String getDefaultUrl() {
        return LINk;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_newslist, container, false);
        setTitle(getArguments().getString("TabTitle"));
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        findViewById(R.id.search_field).setVisibility(View.VISIBLE);
        searchText = (EditText) findViewById(R.id.search);
        search = (Button) findViewById(R.id.search_nick);
        search.setOnClickListener(view -> loadPage(searchText.getText().toString()));
        loadPage(LINk);
        return view;
    }

    private void loadPage(String url) {
        mCompositeSubscription.add(Api.Theme().getPage(url)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::bindUi, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }


    private void bindUi(ThemePage page) {
        Log.d("kek", "bindui");

        setTitle(page.getTitle());
        setSubtitle(page.getDesc());
        String temp = "";
        temp += page.getCurrentPage() + " : " + page.isFirstPage() + " : " + page.isLastPage() + " : " + page.getAllPagesCount() + " : " + page.getPostsOnPageCount() + "\n\n\n";
        String postFix = " : ";
        for (ThemePost post : page.getPosts()) {
            temp += post.getId() + postFix;
            temp += post.getDate() + postFix;
            temp += post.getNumber() + postFix;
            temp += post.getUserAvatar() + postFix;
            temp += post.getUserName() + postFix;
            temp += post.getGroupColor() + postFix;
            temp += post.getGroup() + postFix;
            temp += post.getUserId() + postFix;
            temp += post.getReputation() + postFix;
            temp += post.isCurator() + postFix;
            temp += post.isOnline() + postFix;
            temp += post.canMinusRep() + postFix;
            temp += post.canPlusRep() + postFix;
            temp += post.canReport() + postFix;
            temp += post.canEdit() + postFix;
            temp += post.canDelete() + postFix;
            temp += post.canQuote() + postFix;
            temp += "\n\n";
        }
        text.setText(temp);
        Log.d("kek", "time " + (new Date().getTime() - date.getTime()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
