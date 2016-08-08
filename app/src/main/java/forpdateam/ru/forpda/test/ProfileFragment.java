package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trello.rxlifecycle.FragmentEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabFragment;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/forum/index.php?showuser=2556269#";
    private Subscription subscription;

    private Date date;
    private TextView text;

    public static ProfileFragment newInstance(String tabTitle) {
        ProfileFragment fragment = new ProfileFragment();
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
        loadData();
        return view;
    }

    private void loadData() {
        subscription = Api.Profile().getRx(LINk)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new ProfileModel();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(FragmentEvent.PAUSE))
                .subscribe(this::bindUi);
    }

    private void bindUi(ProfileModel profile) {
        String temp = "";
        String postfix = " \n\n";
        if (profile != null) {
            temp += profile.getAvatar() + postfix;
            temp += profile.getNick() + postfix;
            temp += profile.getStatus() + postfix;
            temp += profile.getGroup() + postfix;
            temp += profile.getRegDate() + postfix;
            temp += profile.getAlerts() + postfix;
            temp += profile.getOnlineDate() + postfix;
            temp += profile.getSign() + postfix;
            temp += profile.getGender() + postfix;
            temp += profile.getBirthDay() + postfix;
            temp += profile.getUserTime() + postfix;
            ArrayList<Pair<String, String>> list = profile.getContacts();
            for (Pair<String, String> pair : list) {
                temp += (pair != null ? pair.first + " : " + pair.second : "null") + postfix;
            }
            list = profile.getDevices();
            for (Pair<String, String> pair : list) {
                temp += (pair != null ? pair.first + " : " + pair.second : "null") + postfix;
            }
            temp += (profile.getKarma() != null ? profile.getKarma().first + " : " + profile.getKarma().second : "null") + postfix;
            temp += (profile.getSitePosts() != null ? profile.getSitePosts().first + " : " + profile.getSitePosts().second : "null") + postfix;
            temp += (profile.getComments() != null ? profile.getComments().first + " : " + profile.getComments().second : "null") + postfix;
            temp += (profile.getReputation() != null ? profile.getReputation().first + " : " + profile.getReputation().second : "null") + postfix;
            temp += (profile.getTopics() != null ? profile.getTopics().first + " : " + profile.getTopics().second : "null") + postfix;
            temp += (profile.getPosts() != null ? profile.getPosts().first + " : " + profile.getPosts().second : "null") + postfix;
        }

        Log.d("kek", "time: " + (new Date().getTime() - date.getTime()));
        text.setText(temp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }
}
