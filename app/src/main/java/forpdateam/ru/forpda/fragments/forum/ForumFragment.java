package forpdateam.ru.forpda.fragments.forum;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.Forum;
import forpdateam.ru.forpda.api.forum.models.ForumItem;
import forpdateam.ru.forpda.api.forum.models.ForumItemSecond;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.fragments.TabFragment;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumFragment extends TabFragment {
    private Subscriber<List<ForumItemSecond>> mainSubscriber = new Subscriber<>();

    @Override
    public String getDefaultTitle() {
        return "forum))))";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        viewsReady();
        return view;
    }

    @Override
    public void loadData() {
        mainSubscriber.subscribe(Api.Forum().getForumsSearch(), this::onLoadThemes, new ArrayList<>(), null);
    }


    private void onLoadThemes(List<ForumItemSecond> items) {
        for (ForumItemSecond item : items) {
            String s = "FORUM " + item.getId() + " : " + item.getParentId() + " : " + item.getLevel() + " : ";
            for (int i = 0; i < item.getLevel(); i++) {
                s += "-";
            }
            s += item.getTitle();
            Log.d("SUKA", s);
        }
    }

}
