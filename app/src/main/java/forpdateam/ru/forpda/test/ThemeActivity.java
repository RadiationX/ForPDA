package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 05.08.16.
 */
public class ThemeActivity extends RxAppCompatActivity {
    private static final String LINk = "http://4pda.ru/forum/index.php?showtopic=541046";
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Date date;
    private TextView text;
    private LinearLayout container;
    private EditText searchText;
    private Button search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        container = (LinearLayout) findViewById(R.id.container);
        findViewById(R.id.search_field).setVisibility(View.VISIBLE);
        searchText = (EditText) findViewById(R.id.search);
        search = (Button) findViewById(R.id.search_nick);
        search.setOnClickListener(view -> loadPage(searchText.getText().toString()));
        loadPage(LINk);
    }

    private void loadPage(String url) {
        mCompositeSubscription.add(Api.Theme().getPage(url)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .onErrorResumeNext(throwable -> {
                    Log.d("kek", "error return next");
                    return null;
                })
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ThemePage();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(this::bindUi));
    }


    private void bindUi(ThemePage page) {
        getSupportActionBar().setTitle(page.getTitle());
        getSupportActionBar().setSubtitle(page.getDesc());
        String temp = "";
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
