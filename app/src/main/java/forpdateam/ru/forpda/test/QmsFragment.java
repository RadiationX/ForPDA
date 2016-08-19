package forpdateam.ru.forpda.test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle.FragmentEvent;

import java.util.ArrayList;
import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatItem;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThread;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist";
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private Date date;
    private TextView text;
    private LinearLayout container;
    private EditText searchText;
    private Button search;

    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_base, container, false);
        inflater.inflate(R.layout.activity_newslist, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        text = (TextView) findViewById(R.id.textView2);
        this.container = (LinearLayout) findViewById(R.id.container);
        findViewById(R.id.search_field).setVisibility(View.VISIBLE);
        searchText = (EditText) findViewById(R.id.search);
        search = (Button) findViewById(R.id.search_nick);
        search.setOnClickListener(view -> search(searchText.getText().toString()));
        date = new Date();
        /*IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=2556269");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=2556269");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=2556269");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=getlastpost");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=findpost&p=51813850");
        IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&st=22460#entry51805351");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=findpost&pid=51805351");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=idx");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=fav");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=boardrules");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&mid=5106086");
        IntentHandler.handle("http://4pda.ru/forum/index.php?act=qms&mid=5106086&t=3127574");
        IntentHandler.handle("http://4pda.ru/devdb/");
        IntentHandler.handle("http://4pda.ru/devdb/phones/");
        IntentHandler.handle("http://4pda.ru/devdb/phones/acer");
        IntentHandler.handle("http://4pda.ru/devdb/acer_liquid_z410_duo");
        IntentHandler.handle("http://4pda.ru/2016/08/04/315172/");
        IntentHandler.handle("http://4pda.ru/reviews/tag/smart-watches/");
        IntentHandler.handle("http://4pda.ru/articles/");
        IntentHandler.handle("http://4pda.ru/special/polzovatelskoe-testirovanie-alcatel-idol-4s/");
        IntentHandler.handle("");*/
        return view;
    }

    @Override
    public void loadData() {
        mCompositeSubscription.add(Api.Qms().getContactList(LINk)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void loadThreads(String url) {
        mCompositeSubscription.add(Api.Qms().getThreadList(url)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::addText, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void loadChat(String url) {
        mCompositeSubscription.add(Api.Qms().getChat(url)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::showChat, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void search(String nick) {
        mCompositeSubscription.add(Api.Qms().search(nick)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new String[]{};
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::showResult, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void bindUi(ArrayList<QmsContact> contacts) {
        String temp = "";
        if (contacts != null) {
            for (QmsContact contact : contacts) {
                //temp+=contact.getNick()+"\n";
                Button button = new Button(getContext());
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
                Button button = new Button(getContext());
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

    private void showResult(String[] res) {
        String temp = "";
        if (res != null) {
            for (String nick : res) {
                temp += nick + "\n";
            }
        }
        Toast.makeText(getContext(), temp, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
