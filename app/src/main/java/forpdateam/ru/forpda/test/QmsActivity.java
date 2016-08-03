package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThread;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsActivity extends RxAppCompatActivity {
    private static final String LINk = "http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist";
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Date date;
    private TextView text;
    private LinearLayout container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);
        text = (TextView) findViewById(R.id.textView2);
        container = (LinearLayout) findViewById(R.id.container);
        date = new Date();
        loadContacts();
    }

    private void loadContacts() {
        mCompositeSubscription.add(Api.Qms().getContactList(LINk)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .onErrorResumeNext(throwable -> {
                    Log.d("kek", "error return next");
                    return null;
                })
                .onErrorReturn(throwable -> {
                    Log.d("kek", throwable.getMessage());
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(this::bindUi));
    }

    private void loadThreads(String url) {
        mCompositeSubscription.add(Api.Qms().getThreadList(url)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .onErrorResumeNext(throwable -> {
                    Log.d("kek", "error return next");
                    return null;
                })
                .onErrorReturn(throwable -> {
                    Log.d("kek", throwable.getMessage());
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(this::addText));
    }


    private void bindUi(ArrayList<QmsContact> contacts) {
        String temp = "";
        if (contacts != null) {
            for (QmsContact contact : contacts) {
                //temp+=contact.getNick()+"\n";
                Button button = new Button(this);
                button.setText(contact.getNick() + (contact.getCount().isEmpty() ? "" : " : " + contact.getCount()));
                button.setOnClickListener(view -> loadThreads("http://4pda.ru/forum/index.php?act=qms&mid=" + contact.getId()));
                container.addView(button);
            }
        }

        Log.d("kek", "time: " + (new Date().getTime() - date.getTime()));
        text.setText(temp);
    }

    private void addText(ArrayList<QmsThread> threads) {
        String temp = "";
        if (threads != null) {
            for (QmsThread thread : threads) {
                temp += thread.getName() + (thread.getCountNew().isEmpty() ? "" : " : " + thread.getCountNew() + " /") + " " + thread.getCountMessages() + "\n";
            }
        }
        text.setText(temp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
