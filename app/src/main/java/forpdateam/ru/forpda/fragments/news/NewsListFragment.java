package forpdateam.ru.forpda.fragments.news;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.models.NewsItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru";

    private Date date;
    private TextView text;
    private Realm realm;

    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.activity_newslist, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        text = (TextView) findViewById(R.id.textView2);
        viewsReady();
        date = new Date();
        return view;
    }

    @Override
    public void loadData() {
        getCompositeDisposable().add(Api.NewsList().getRx(LINk)
                .onErrorReturn(throwable -> {
                    ErrorHandler.handle(this, throwable, view1 -> loadData());
                    return new ArrayList<>();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi));
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
}
