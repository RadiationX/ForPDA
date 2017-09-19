package forpdateam.ru.forpda.fragments.forum;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.data.realm.forum.ForumItemFlatBd;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumFragment extends TabFragment {
    public final static String ARG_FORUM_ID = "ARG_FORUM_ID";
    private Subscriber<ForumItemTree> mainSubscriber = new Subscriber<>(this);
    private NestedScrollView treeContainer;
    private Realm realm;
    private RealmResults<ForumItemFlatBd> results;
    private static AlertDialogMenu<ForumFragment, ForumItemTree> forumMenu, showedForumMenu;
    private AlertDialog updateDialog;
    private TreeNode.TreeNodeClickListener nodeClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        if (item.getForums() == null) {
            Bundle args = new Bundle();
            args.putInt(TopicsFragment.TOPICS_ID_ARG, item.getId());
            TabManager.getInstance().add(TopicsFragment.class, args);
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = (node, value) -> {
        ForumItemTree item = (ForumItemTree) value;
        if (forumMenu == null) {
            forumMenu = new AlertDialogMenu<>();
            showedForumMenu = new AlertDialogMenu<>();
            forumMenu.addItem(getString(R.string.open_forum), (context, data) -> {
                Bundle args = new Bundle();
                args.putInt(TopicsFragment.TOPICS_ID_ARG, data.getId());
                TabManager.getInstance().add(TopicsFragment.class, args);
            });
            forumMenu.addItem(getString(R.string.copy_link), (context, data) -> Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(data.getId()))));
            forumMenu.addItem(getString(R.string.mark_read), (context, data) -> {

            });
        }
        showedForumMenu.clear();
        if (item.getLevel() > 0)
            showedForumMenu.addItem(forumMenu.get(0));
        showedForumMenu.addItem(forumMenu.get(1));
        showedForumMenu.addItem(forumMenu.get(2));
        new AlertDialog.Builder(getContext())
                .setItems(showedForumMenu.getTitles(), (dialogInterface, i) -> showedForumMenu.onClick(i, ForumFragment.this, item))
                .show();
        return false;
    };
    TreeNode root;
    AndroidTreeView tView;
    int forumId = -1;


    public ForumFragment() {
        configuration.setUseCache(true);
        configuration.setDefaultTitle(App.getInstance().getString(R.string.fragment_title_forum));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            forumId = getArguments().getInt(ARG_FORUM_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setListsBackground();
        baseInflateFragment(inflater, R.layout.fragment_forum);
        treeContainer = (NestedScrollView) findViewById(R.id.nested_scroll_view);

        viewsReady();

        return view;
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add(R.string.forum_refresh)
                .setOnMenuItemClickListener(item -> {
                    loadData();
                    return false;
                });
        getMenu().add(R.string.mark_all_read)
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
    public void loadData() {
        super.loadData();
        updateDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.refreshing)
                .setMessage(R.string.loading_data)
                .setCancelable(false)
                .show();
        mainSubscriber.subscribe(RxApi.Forum().getForums(), this::onLoadThemes, new ForumItemTree(), null);
    }

    @Override
    public void loadCacheData() {
        super.loadCacheData();
        if (realm.isClosed()) return;
        results = realm.where(ForumItemFlatBd.class).findAll();
        if (updateDialog != null && updateDialog.isShowing()) {
            if (results.size() != 0) {
                updateDialog.setMessage(getString(R.string.update_complete));
            } else {
                updateDialog.setMessage(getString(R.string.error_occurred));
            }
            new Handler().postDelayed(() -> {
                if (updateDialog != null)
                    updateDialog.cancel();
            }, 500);
        }
        if (results.size() == 0) {
            loadData();
        } else {
            bindView();
        }
    }

    private void onLoadThemes(ForumItemTree forumRoot) {
        updateDialog.setMessage(getString(R.string.update_data_base));

        if (forumRoot.getForums() == null) {
            updateDialog.setMessage(getString(R.string.error_occurred));
            new Handler().postDelayed(() -> {
                if (updateDialog != null)
                    updateDialog.cancel();
            }, 500);
            return;
        }


        if (realm.isClosed()) return;
        realm.executeTransactionAsync(r -> {
            r.delete(ForumItemFlatBd.class);
            List<ForumItemFlatBd> items = new ArrayList<>();
            transformToList(items, forumRoot);
            r.copyToRealmOrUpdate(items);
            items.clear();
        }, this::loadCacheData);
        //setSubtitle(data.getAll() <= 1 ? null : "" + data.getCurrent() + "/" + data.getAll());


    }

    public void transformToList(List<ForumItemFlatBd> list, ForumItemTree rootForum) {
        if (rootForum.getForums() == null) return;
        for (ForumItemTree item : rootForum.getForums()) {
            list.add(new ForumItemFlatBd(item));
            transformToList(list, item);
        }
    }

    private void bindView() {
        //adapter.addAll(results);
        ForumItemTree rootForum = new ForumItemTree();

        Api.Forum().transformToTree(results, rootForum);

        tView = new AndroidTreeView(getContext());
        root = TreeNode.root();
        recourse(rootForum, root);
        tView.setRoot(root);

        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(DefaultForumHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        treeContainer.removeAllViews();
        treeContainer.addView(tView.getView());

        //int id = 427;
        //int id = 828;
        //int id = 282;
        //int id = 269;
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
        if (root.getChildren() == null && root.getChildren().size() == 0) return null;
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


    public static boolean checkIsLink(int id) {
        Realm realm = Realm.getDefaultInstance();
        boolean res = realm.where(ForumItemFlatBd.class).equalTo("parentId", id).findAll().size() == 0;
        realm.close();
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
