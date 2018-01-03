package forpdateam.ru.forpda.ui.fragments.forum;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.Di;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd;
import forpdateam.ru.forpda.presentation.forum.ForumPresenter;
import forpdateam.ru.forpda.presentation.forum.ForumView;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumFragment extends TabFragment implements ForumView {
    public final static String ARG_FORUM_ID = "ARG_FORUM_ID";

    @InjectPresenter
    ForumPresenter presenter;

    @ProvidePresenter
    ForumPresenter provideForumPresenter() {
        return new ForumPresenter(Di.get().forumRepository);
    }

    private NestedScrollView treeContainer;
    private RealmResults<ForumItemFlatBd> results;
    private DynamicDialogMenu<ForumFragment, ForumItemTree> dialogMenu;
    private TreeNode.TreeNodeClickListener nodeClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        if (item.getForums() == null) {
            Bundle args = new Bundle();
            args.putInt(TopicsFragment.TOPICS_ID_ARG, item.getId());
            TabManager.get().add(TopicsFragment.class, args);
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        dialogMenu.disallowAll();
        if (item.getLevel() > 0)
            dialogMenu.allow(0);
        dialogMenu.allow(1);
        if (ClientHelper.getAuthState()) {
            dialogMenu.allow(2);
            dialogMenu.allow(3);
        }
        dialogMenu.allow(4);

        dialogMenu.show(getContext(), ForumFragment.this, item);
        return false;
    };
    TreeNode root;
    AndroidTreeView tView;
    int forumId = -1;


    public ForumFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_forum));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            forumId = getArguments().getInt(ARG_FORUM_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_forum);
        treeContainer = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        setListsBackground();

        dialogMenu = new DynamicDialogMenu<>();
        dialogMenu.addItem(getString(R.string.open_forum), (context, data) -> presenter.navigateToForum(data));
        dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> presenter.copyLink(data));
        dialogMenu.addItem(getString(R.string.mark_read), (context, data) -> {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.mark_read) + "?")
                    .setPositiveButton(R.string.ok, (dialog, which) -> ForumHelper.markRead(o -> Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_SHORT).show(), data.getId()))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        dialogMenu.addItem(getString(R.string.add_to_favorites), (context, data) -> {
            FavoritesHelper.addForumWithDialog(getContext(), aBoolean -> {
                Toast.makeText(getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }, data.getId());
        });
        dialogMenu.addItem(getString(R.string.fragment_title_search), (context, data) -> presenter.navigateToSearch(data));
    }

    @Override
    protected void addBaseToolbarMenu(Menu menu) {
        super.addBaseToolbarMenu(menu);
        menu.add(R.string.forum_refresh)
                .setOnMenuItemClickListener(item -> {
                    loadData();
                    return false;
                });
        menu.add(R.string.mark_all_read)
                .setOnMenuItemClickListener(item -> {
                    new AlertDialog.Builder(getContext())
                            .setMessage(getString(R.string.mark_all_read) + "?")
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                ForumHelper.markAllRead(o -> {
                                    loadData();
                                });
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return false;
                });
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        presenter.loadForums();
        return true;
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
        presenter.getCacheForums();
    }

    @Override
    public void showForums(ForumItemTree forumRoot) {
        tView = new AndroidTreeView(getContext());
        root = TreeNode.root();
        recourse(forumRoot, root);
        tView.setRoot(root);

        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(DefaultForumHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        treeContainer.removeAllViews();
        treeContainer.addView(tView.getView());

        if (forumId != -1) {
            scrollToForum(forumId);
            forumId = -1;
        }
    }

    private void scrollToForum(int id) {
        final TreeNode targetNode = findNodeById(id, root);

        if (targetNode != null) {
            TreeNode upToParent = targetNode;
            while (upToParent.getParent() != null) {
                tView.expandNode(upToParent);
                upToParent = upToParent.getParent();
            }
        }
    }

    private TreeNode findNodeById(int id, TreeNode root) {
        if (root.getValue() != null && ((ForumItemTree) root.getValue()).getId() == id) return root;
        if (root.getChildren() == null && root.getChildren().isEmpty()) return null;
        for (TreeNode item : root.getChildren()) {
            TreeNode node = findNodeById(id, item);
            if (node != null) return node;
        }
        return null;
    }

    private void recourse(ForumItemTree rootForum, TreeNode rootNode) {
        if (rootForum.getForums() == null) return;
        for (ForumItemTree item : rootForum.getForums()) {
            TreeNode child = new TreeNode(item);
            recourse(item, child);
            rootNode.addChild(child);
        }
    }

}
