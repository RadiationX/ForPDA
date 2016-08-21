package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 05.08.16.
 */
public class ThemeFragment extends TabFragment {
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Date date;
    private TextView text;
    private EditText searchText;
    private Button search;
    //private String thisUrl = "http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.activity_newslist, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        //findViewById(R.id.search_field).setVisibility(View.VISIBLE);
        /*searchText = (EditText) findViewById(R.id.search);
        search = (Button) findViewById(R.id.search_nick);
        search.setOnClickListener(view -> {
            thisUrl = searchText.getText().toString();
            loadData();
        });*/
        return view;
    }

    @Override
    public void loadData() {
        mCompositeSubscription.add(Api.Theme().getPage(getTabUrl())
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> {
                    ErrorHandler.handle(this, throwable, null);
                }));
    }

    String template =
            "%s : %s : %s : %s";

    private void bindUi(ThemePage page) {
        Log.d("kek", "bindui");

        setTitle(page.getTitle());
        setSubtitle(page.getDesc());
        String temp = "";
        temp += page.getCurrentPage() + " : " + page.isFirstPage() + " : " + page.isLastPage() + " : " + page.getAllPagesCount() + " : " + page.getPostsOnPageCount() + "\n\n\n";
        String postFix = " : ";
        for (ThemePost post : page.getPosts()) {
            temp += String.format(template, post.getId(), post.getDate(), post.getNumber(), post.getUserAvatar());
            temp += "\n\n";
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
