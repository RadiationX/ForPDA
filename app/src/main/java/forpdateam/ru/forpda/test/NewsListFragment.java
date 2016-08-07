package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle.FragmentEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabFragment;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/";
    private Subscription subscription;

    private Date date;
    private TextView text;

    public static NewsListFragment newInstance(String tabTitle){
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString("TabTitle", tabTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_newslist, container, false);
        setTitle(getArguments().getString("TabTitle"));
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        loadData();
        return view;
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
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
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
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }
}
