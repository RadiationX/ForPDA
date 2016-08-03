package forpdateam.ru.forpda.test;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.L;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListActivity extends RxAppCompatActivity {
    private static final String LINk = "http://4pda.ru/";
    private Subscription subscription;

    private Date date;
    private TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        loadData();
    }

    private void loadData() {
        subscription = Api.NewsList().getRx(LINk)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .onErrorResumeNext(throwable -> {
                    Log.d("kek", "error return next");
                    return null;
                })
                .onErrorReturn(throwable -> {
                    Log.d("kek", "error return");
                    Toast.makeText(NewsListActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(this::bindUi);
    }

    private void bindUi(ArrayList<NewsItem> list) {
        Log.d("kek", (list == null) + "? " + (list == null ? "" : list.size()));
        if (list == null) return;
        String titles = "";
        for (NewsItem item : list) {
            titles += item.getTitle() + ";" + "\n\n";
        }
        text.setText(titles);
        Log.d("kek", "time: " + (new Date().getTime() - date.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }
}
