package forpdateam.ru.forpda.fragments.forum;

import android.content.Context;
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
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.Forum;
import forpdateam.ru.forpda.api.forum.models.ForumItem;
import forpdateam.ru.forpda.api.forum.models.ForumItemSecond;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.TabFragment;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumFragment extends TabFragment {
    private Subscriber<ForumItem> mainSubscriber = new Subscriber<>();

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
        mainSubscriber.subscribe(Api.Forum().getForumsSearch(), this::onLoadThemes, new ForumItem(), null);
    }


    private void onLoadThemes(ForumItem forumRoot) {
        TreeNode root = TreeNode.root();
        recourse(forumRoot, root);
        AndroidTreeView tView = new AndroidTreeView(getActivity(), root);
        ((ViewGroup) view.findViewById(R.id.fragment_content)).addView(tView.getView());
        List<ForumItem> list = new ArrayList<>();
        transformToList(list, forumRoot);
        Log.d("SUKA", "TRANSFORM SIZE "+list.size());
    }

    private void recourse(ForumItem rootForum, TreeNode rootNode) {
        if (rootForum.getForums() == null) return;
        for (ForumItem item : rootForum.getForums()) {
            String s = "";
            for (int i = 0; i < item.getLevel(); s += "-", i++) ;
            s += item.getTitle();
            Log.d("SUKA", s);
            TreeNode child = new TreeNode(s);
            recourse(item, child);
            rootNode.addChild(child);
        }
    }

    private void transformToList(List<ForumItem> list, ForumItem rootForum) {
        if (rootForum.getForums() == null) return;
        for (ForumItem item : rootForum.getForums()) {
            list.add(item);
            transformToList(list, item);
        }
    }
}
