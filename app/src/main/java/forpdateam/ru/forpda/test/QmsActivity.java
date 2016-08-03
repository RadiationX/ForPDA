package forpdateam.ru.forpda.test;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatItem;
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
    private EditText searchText;
    private Button search;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);
        text = (TextView) findViewById(R.id.textView2);
        container = (LinearLayout) findViewById(R.id.container);
        findViewById(R.id.search_field).setVisibility(View.VISIBLE);
        searchText = (EditText) findViewById(R.id.search);
        search = (Button) findViewById(R.id.search_nick);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(searchText.getText().toString());
            }
        });
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

    private void loadChat(String url) {
        Log.d("kek", "load cahat " + url);
        mCompositeSubscription.add(Api.Qms().getChat(url)
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
                .subscribe(this::showChat));
    }

    private void search(String nick) {
        mCompositeSubscription.add(Api.Qms().search(nick)
                .timeout(2, TimeUnit.SECONDS)
                .retry(2)
                .onErrorResumeNext(throwable -> {
                    Log.d("kek", "error return next");
                    return null;
                })
                .onErrorReturn(throwable -> {
                    Log.d("kek", throwable.getMessage());
                    throwable.printStackTrace();
                    return new String[]{};
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(this::showResult));
    }

    private void bindUi(ArrayList<QmsContact> contacts) {
        String temp = "";
        if (contacts != null) {
            for (QmsContact contact : contacts) {
                //temp+=contact.getNick()+"\n";
                Button button = new Button(this);
                button.setText(contact.getNick() + (contact.getCount().isEmpty() ? "" : " : " + contact.getCount()));
                button.setOnClickListener(view -> {
                    mid = contact.getId();
                    loadThreads("http://4pda.ru/forum/index.php?act=qms&mid=" + mid);
                });
                container.addView(button);
            }
        }

        Log.d("kek", "time: " + (new Date().getTime() - date.getTime()));
        text.setText(temp);
    }


    String mid, tid;
    int lastThreads = -1;

    private void addText(ArrayList<QmsThread> threads) {
        //String temp = "";
        if (threads != null) {
            if (lastThreads != -1) {
                for (int i = 0; i < lastThreads; i++) {
                    container.removeViewAt(0);
                }
            }
            lastThreads = threads.size();
            for (QmsThread thread : threads) {
                //temp += thread.getName() + (thread.getCountNew().isEmpty() ? "" : " : " + thread.getCountNew() + " /") + " " + thread.getCountMessages() + "\n";
                Button button = new Button(this);
                button.setBackgroundColor(Color.parseColor("#55ff55"));
                button.setText(thread.getName() + (thread.getCountNew().isEmpty() ? "" : " : " + thread.getCountNew() + " /") + " " + thread.getCountMessages());
                button.setOnClickListener(view -> loadChat("http://4pda.ru/forum/index.php?act=qms&mid=" + mid + "&t=" + thread.getId()));
                container.addView(button, 0);
            }
        }
        //text.setText(temp);
    }

    private void showChat(ArrayList<QmsChatItem> threads) {
        Log.d("kekos", threads.size() + " size");
        String temp = "";
        if (threads != null) {
            for (QmsChatItem item : threads) {
                temp += (item.isDate() ? item.getDate() : ((item.getWhoseMessage() ? "Я" : "Он") + ":\n") + item.getContent()) + "\n\n";
            }
        }
        text.setText(temp);
    }

    private void showResult(String[] res){
        String temp = "";
        if(res!=null){
            for(String nick: res){
                temp+=nick+"\n";
            }
        }
        Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
