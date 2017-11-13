package forpdateam.ru.forpda.ui.fragments.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.mentions.models.MentionItem;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.ui.views.ContentController;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;
import forpdateam.ru.forpda.ui.views.FunnyContent;
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionsFragment extends RecyclerFragment implements MentionsContract.View, MentionsAdapter.OnItemClickListener<MentionItem> {
    private DynamicDialogMenu<MentionsFragment, MentionItem> dialogMenu;
    private MentionsContract.Presenter presenter;
    private MentionsAdapter adapter;
    private PaginationHelper paginationHelper;

    public MentionsFragment() {
        configuration.setAlone(true);
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_mentions));
        presenter = new MentionsPresenter(this);
        registerPresenter(presenter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        paginationHelper = new PaginationHelper(getActivity());
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                loadData();
            }
        });
        adapter = new MentionsAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        presenter.getMentions(paginationHelper.getCurrentPage());
        return true;
    }

    @Override
    public void showMentions(MentionsData data) {
        if (data.getItems().isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                FunnyContent funnyContent = new FunnyContent(getContext())
                        .setImage(R.drawable.ic_notifications)
                        .setTitle(R.string.funny_mentions_nodata_title)
                        .setDesc(R.string.funny_mentions_nodata_desc);
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA);
            }
            contentController.showContent(ContentController.TAG_NO_DATA);
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA);
        }

        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getTitle());
        listScrollTop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginationHelper != null)
            paginationHelper.destroy();
    }

    @Override
    public void onItemClick(MentionItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle(item.getLink(), args);
    }

    @Override
    public boolean onItemLongClick(MentionItem item) {
        if (dialogMenu == null) {
            dialogMenu = new DynamicDialogMenu<>();

            dialogMenu.addItem(getString(R.string.copy_link), (context, data) -> {
                Utils.copyToClipBoard(data.getLink());
            });
            dialogMenu.addItem(getString(R.string.add_to_favorites), (context, data) -> {
                int id = 0;
                Matcher matcher = Pattern.compile("showtopic=(\\d+)").matcher(data.getLink());
                if (matcher.find()) {
                    id = Integer.parseInt(matcher.group(1));
                }
                FavoritesHelper.addWithDialog(getContext(), aBoolean -> {
                    Toast.makeText(getContext(), aBoolean ? getString(R.string.favorites_added) : getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                }, id);
            });
        }
        dialogMenu.disallowAll();
        dialogMenu.allow(0);
        if (item.isTopic() && ClientHelper.getAuthState()) {
            dialogMenu.allow(1);
        }
        dialogMenu.show(getContext(), MentionsFragment.this, item);
        return false;
    }
}
