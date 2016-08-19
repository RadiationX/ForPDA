package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/forum/index.php?showuser=2556269#";
    private CompositeSubscription compositeSubscription = new CompositeSubscription();


    private Date date;
    private TextView text;
    boolean isLoaded = false;

    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_base, container, false);
        inflater.inflate(R.layout.activity_newslist, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        setHasOptionsMenu(true);
        text = (TextView) findViewById(R.id.textView2);
        date = new Date();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("kek", "oncreate menu");
    }

    /*@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d("kek", "onprepare menu");
        menu.clear();
        menu.add("HYZ");
        menu.add("PIZZA");
    }*/


    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d("kek", "onclose menu");
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.d("kek", "ondestroy menu");
    }

    @Override
    public void loadData() {
        compositeSubscription.add(
                Api.Profile().getRx(LINk)
                        .onErrorReturn(throwable -> {
                            ErrorHandler.handle(this, throwable, view1 -> loadData());
                            return new ProfileModel();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::bindUi)
        );
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
        isLoaded = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }
}
