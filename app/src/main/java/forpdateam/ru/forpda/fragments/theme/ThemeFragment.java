package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.RequestFile;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.theme.editpost.EditPostFragment;
import forpdateam.ru.forpda.messagepanel.MessagePanel;
import forpdateam.ru.forpda.messagepanel.attachments.AttachmentsPopup;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.Utils;

/**
 * Created by radiationx on 20.10.16.
 */

public abstract class ThemeFragment extends TabFragment {
    //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
    protected final static int BACK_ACTION = 0, REFRESH_ACTION = 1, NORMAL_ACTION = 2;
    protected final static String JS_INTERFACE = "ITheme";
    protected int action = NORMAL_ACTION;
    protected SwipeRefreshLayout refreshLayout;
    protected ThemePage pageData;
    protected List<ThemePage> history = new ArrayList<>();
    protected Subscriber<ThemePage> mainSubscriber = new Subscriber<>();
    protected Subscriber<String> helperSubscriber = new Subscriber<>();
    protected TabLayout tabLayout;
    //Тег для вьюхи поиска. Чтобы создавались кнопки и т.д, только при вызове поиска, а не при каждом создании меню.
    protected int searchViewTag = 0;
    protected final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);
    protected MessagePanel messagePanel;
    protected AttachmentsPopup attachmentsPopup;
    protected Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>();
    protected static final int PICK_IMAGE = 1228;


    protected abstract void addShowingView();

    protected abstract void findNext(boolean next);

    protected abstract void findText(String text);

    protected abstract void saveToHistory(ThemePage themePage);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        initFabBehavior();
        baseInflateFragment(inflater, R.layout.fragment_theme);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        messagePanel = new MessagePanel(getContext(), (ViewGroup) findViewById(R.id.fragment_container), coordinatorLayout, false);
        messagePanel.addSendOnClickListener(v -> sendMessage());
        messagePanel.getSendButton().setOnLongClickListener(v -> {
            TabManager.getInstance().add(EditPostFragment.newInstance(createEditPostForm(), pageData.getTitle()));
            return true;
        });
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> pickImage());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());

        tabLayout = (TabLayout) inflater.inflate(R.layout.toolbar_theme, (ViewGroup) toolbar.getParent(), false);
        ((ViewGroup) toolbar.getParent()).addView(tabLayout, ((ViewGroup) toolbar.getParent()).indexOfChild(toolbar));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_left).setTag("first"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_left).setTag("prev"));
        tabLayout.addTab(tabLayout.newTab().setText("Выбор").setTag("selectPage"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_right).setTag("next"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chevron_double_right).setTag("last"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (refreshLayout.isRefreshing()) return;
                assert tab.getTag() != null;
                switch ((String) tab.getTag()) {
                    case "first":
                        firstPage();
                        break;
                    case "prev":
                        prevPage();
                        break;
                    case "selectPage":
                        selectPage();
                        break;
                    case "next":
                        nextPage();
                        break;
                    case "last":
                        lastPage();
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
        addShowingView();
        viewsReady();


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        collapsingToolbarLayout.setLayoutParams(params);
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(App.px56 + App.px24);

        refreshLayout.setOnRefreshListener(() -> {
            action = REFRESH_ACTION;
            loadData();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void onDestroy() {
        history.clear();
        super.onDestroy();
        messagePanel.onDestroy();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    @Override
    public boolean onBackPressed() {
        if (messagePanel.onBackPressed()) return true;
        if (toolbar.getMenu().findItem(R.id.action_search) != null && toolbar.getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            toolbar.collapseActionView();
            return true;
        }
        if (history.size() > 0) {
            action = BACK_ACTION;
            pageData = history.get(history.size() - 1);
            history.remove(history.size() - 1);
            updateView();
            return true;
        }
        return false;
    }


    /*
    *
    * LOADING POST FUNCTIONS
    *
    * */

    @Override
    public void loadData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(Api.Theme().getPage(getTabUrl(), true), this::onLoadData, new ThemePage(), v -> loadData());
    }

    protected void onLoadData(ThemePage themePage) throws Exception {
        //Log.d("suka", "check theme " + themePage + " : " + themePage.getPosts().size() + " : " + themePage.getId() + " : " + themePage.getForumId() + " : " + themePage.getUrl());
        if (refreshLayout != null)
            refreshLayout.setRefreshing(false);
        if (themePage == null || themePage.getId() == 0 || themePage.getUrl() == null) {
            return;
        }
        setTabUrl(themePage.getUrl());
        if (pageData != null) {
            saveToHistory(themePage);
        }
        pageData = themePage;
        updateFavorites(themePage);
        updateView();
    }

    protected void updateTitle() {
        setTitle(pageData.getTitle());
    }

    protected void updateSubTitle() {
        setSubtitle(String.valueOf(pageData.getCurrentPage()).concat("/").concat(String.valueOf(pageData.getAllPagesCount())));
    }

    protected void updateFavorites(ThemePage themePage) {
        if (themePage.getCurrentPage() < themePage.getAllPagesCount()) return;
        String tag = TabManager.getInstance().getTagContainClass(FavoritesFragment.class);
        if (tag == null) return;
        ((FavoritesFragment) TabManager.getInstance().get(tag)).markRead(themePage.getId());
    }

    protected void updateNavigation(ThemePage themePage) {
        tabLayout.setVisibility(View.VISIBLE);
        boolean prevDisabled = themePage.getCurrentPage() <= 1;
        boolean nextDisabled = themePage.getCurrentPage() == themePage.getAllPagesCount();
        TabLayout.Tab tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == 2) continue;
            boolean b = i < 2 ? prevDisabled : nextDisabled;
            tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getIcon() != null) {
                if (b)
                    tab.getIcon().setColorFilter(colorFilter);
                else
                    tab.getIcon().clearColorFilter();
            }
        }
    }

    protected void updateView() {
        updateNavigation(pageData);
        setTabUrl(pageData.getUrl());
        updateTitle();
        updateSubTitle();
        refreshOptionsMenu();
    }

    public void refreshOptionsMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        menu.add("Обновить").setIcon(App.getAppDrawable(R.drawable.ic_refresh_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            action = REFRESH_ACTION;
            loadData();
            return false;
        })/*.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/;
        if (pageData != null) {
            menu.add("Ссылка").setOnMenuItemClickListener(menuItem -> {
                Utils.copyToClipBoard(getTabUrl());
                return false;
            });
            addSearchOnPageItem(menu);
            menu.add("Найти в теме").setOnMenuItemClickListener(menuItem -> false);
        }

        SubMenu subMenu = menu.addSubMenu("Опции темы");
        if (pageData != null) {
            if (pageData.isInFavorite()) {
                subMenu.add("Удалить из избранного").setOnMenuItemClickListener(menuItem -> false);
            } else {
                subMenu.add("Добавить в избранное").setOnMenuItemClickListener(menuItem -> false);
            }
            subMenu.add("Открыть форум темы").setOnMenuItemClickListener(menuItem -> false);
            subMenu.add("Кто читает тему").setOnMenuItemClickListener(menuItem -> false);
            subMenu.add("Кто писал сообщения").setOnMenuItemClickListener(menuItem -> false);
        }
    }

    private void addSearchOnPageItem(Menu menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setTag(searchViewTag);

        searchView.setOnSearchClickListener(v -> {
            if (searchView.getTag().equals(searchViewTag)) {
                ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                if (searchClose != null)
                    ((ViewGroup) searchClose.getParent()).removeView(searchClose);

                ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(App.px48, App.px48);
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true);

                AppCompatImageButton btnNext = new AppCompatImageButton(searchView.getContext());
                btnNext.setImageDrawable(App.getAppDrawable(R.drawable.ic_search_next_gray_24dp));
                btnNext.setBackgroundResource(outValue.resourceId);

                AppCompatImageButton btnPrev = new AppCompatImageButton(searchView.getContext());
                btnPrev.setImageDrawable(App.getAppDrawable(R.drawable.ic_search_prev_gray_24dp));
                btnPrev.setBackgroundResource(outValue.resourceId);

                ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
                ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

                btnNext.setOnClickListener(v1 -> findNext(true));
                btnPrev.setOnClickListener(v1 -> findNext(false));
                searchViewTag++;
            }
        });

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                findText(newText);
                return false;
            }
        });
    }


    /*
    *
    * EDIT POST FUNCTIONS
    *
    * */

    private EditPostForm createEditPostForm() {
        EditPostForm form = new EditPostForm();
        form.setForumId(pageData.getForumId());
        form.setTopicId(pageData.getId());
        form.setSt(pageData.getCurrentPage() * pageData.getPostsOnPageCount());
        form.setMessage(messagePanel.getMessage());
        List<AttachmentItem> attachments = messagePanel.getAttachments();
        for (AttachmentItem item : attachments) {
            form.addAttachment(item);
        }
        return form;
    }

    public void onSendPostCompleted(ThemePage themePage) throws Exception {
        messagePanel.clearAttachments();
        messagePanel.clearMessage();
        onLoadData(themePage);
    }

    public void onEditPostCompleted(ThemePage themePage) throws Exception {
        onLoadData(themePage);
    }

    private void sendMessage() {
        messagePanel.setProgressState(true);
        EditPostForm form = createEditPostForm();
        mainSubscriber.subscribe(Api.EditPost().sendPost(form), s -> {
            messagePanel.setProgressState(false);
            onLoadData(s);
            messagePanel.clearAttachments();
            messagePanel.clearMessage();
        }, pageData, v -> loadData());
    }

    public void pickImage() {
        startActivityForResult(FilePickHelper.pickImage(PICK_IMAGE), PICK_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data.getData()));
        }
    }


    public void uploadFiles(List<RequestFile> files) {
        attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(Api.EditPost().uploadFiles(56965580, files), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        attachmentSubscriber.subscribe(Api.EditPost().deleteFiles(56965580, selectedFiles), item -> attachmentsPopup.onDeleteFiles(item), selectedFiles, null);
    }



    /*
    *
    * Post functions
    *
    * */

    public ThemePost getPostById(int postId) {
        for (ThemePost post : pageData.getPosts())
            if (post.getId() == postId)
                return post;
        return null;
    }

    public void jumpToPage(int st) {
        String url = "http://4pda.ru/forum/index.php?showtopic=";
        url = url.concat(Uri.parse(getTabUrl()).getQueryParameter("showtopic"));
        if (st != 0) url = url.concat("&st=").concat(Integer.toString(st));
        setTabUrl(url);
        loadData();
    }


    public void firstPage() {
        if (pageData.getCurrentPage() <= 0) return;
        jumpToPage(0);
    }


    public void prevPage() {
        if (pageData.getCurrentPage() <= 1) return;
        jumpToPage((pageData.getCurrentPage() - 2) * pageData.getPostsOnPageCount());
    }


    public void nextPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        jumpToPage(pageData.getCurrentPage() * pageData.getPostsOnPageCount());
    }


    public void lastPage() {
        if (pageData.getCurrentPage() == pageData.getAllPagesCount()) return;
        jumpToPage((pageData.getAllPagesCount() - 1) * pageData.getPostsOnPageCount());
    }


    public void selectPage() {
        ThemeDialogsHelper.selectPage(this, pageData);
    }


    public void showUserMenu(final String postId) {
        ThemeDialogsHelper.showUserMenu(this, getPostById(Integer.parseInt(postId)));
    }


    public void showReputationMenu(final String postId) {
        ThemeDialogsHelper.showReputationMenu(this, getPostById(Integer.parseInt(postId)));
    }


    public void showPostMenu(final String postId) {
        ThemeDialogsHelper.showPostMenu(this, getPostById(Integer.parseInt(postId)));
    }


    public void reportPost(final String postId) {
        reportPost(getPostById(Integer.parseInt(postId)));
    }


    public void insertNick(final String postId) {
        insertNick(getPostById(Integer.parseInt(postId)));
    }

    public void insertNick(ThemePost post) {
        String insert = String.format(Locale.getDefault(), "[snapback]%s[/snapback] [b]%s,[/b]\n", post.getId(), post.getNick());
        messagePanel.insertText(insert);
    }

    public void quotePost(final String text, final String postId) {
        quotePost(text, getPostById(Integer.parseInt(postId)));
    }

    public void quotePost(String text, ThemePost post) {
        String insert = String.format(Locale.getDefault(), "[quote name=\"%s\" date=\"%s\" post=%S]%s[/quote]", post.getNick(), post.getDate(), post.getId(), text);
        messagePanel.insertText(insert);
    }


    public void deletePost(final String postId) {
        deletePost(getPostById(Integer.parseInt(postId)));
    }

    public void editPost(final String postId) {
        editPost(getPostById(Integer.parseInt(postId)));
    }

    public void editPost(ThemePost post) {
        TabManager.getInstance().add(EditPostFragment.newInstance(post.getId(), pageData.getId(), pageData.getForumId(), pageData.getSt(), pageData.getTitle()));
        Toast.makeText(getContext(), "editpost ".concat(Integer.toString(post.getId())), Toast.LENGTH_SHORT).show();
    }

    public void votePost(final String postId, final boolean type) {
        votePost(getPostById(Integer.parseInt(postId)), type);
    }

    public void setHistoryBody(final String index, final String body) {
        history.get(Integer.parseInt(index)).setHtml(body.replaceAll("data-block-init=\"1\"", ""));
    }

    public void copySelectedText(final String text) {
        Utils.copyToClipBoard(text);
    }

    public void toast(final String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void log(final String text) {
        Log.d("kek", "ITheme: ".concat(text));
    }

    public void showPollResults() {
        setTabUrl(getTabUrl().replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&mode=show&poll_open=true"));
        loadData();
    }

    public void showPoll() {
        setTabUrl(getTabUrl().replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&poll_open=true"));
        loadData();
    }

    private final static String reportWarningText = "Вам не нужно указывать здесь тему и сообщение, модератор автоматически получит эту информацию.\n\n" +
            "Пожалуйста, используйте эту возможность форума только для жалоб о некорректном сообщении!\n" +
            "Для связи с модератором используйте личные сообщения.";

    public void reportPost(ThemePost post) {
        if (App.getInstance().getPreferences().getBoolean("show_report_warning", true)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Внимание!")
                    .setMessage(reportWarningText)
                    .setPositiveButton("Ок", (dialogInterface, i) -> {
                        App.getInstance().getPreferences().edit().putBoolean("show_report_warning", false).apply();
                        showReportDialog(pageData.getId(), post.getId());
                    })
                    .show();
        } else {
            showReportDialog(pageData.getId(), post.getId());
        }
    }

    @SuppressLint("InflateParams")
    public void showReportDialog(int themeId, int postId) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.report_layout, null);

        assert layout != null;
        final EditText messageField = (EditText) layout.findViewById(R.id.report_text_field);

        new AlertDialog.Builder(getContext())
                .setTitle("Жалоба на пост ".concat(getPostById(postId).getNick()))
                .setView(layout)
                .setPositiveButton("Отправить", (dialogInterface, i) -> doReportPost(themeId, postId, messageField.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    protected void doReportPost(int themeId, int postId, String message) {
        helperSubscriber.subscribe(Api.Theme().reportPost(themeId, postId, message), s -> Toast.makeText(getContext(), s.isEmpty() ? "Неизвестная ошибка" : s, Toast.LENGTH_SHORT).show(), "", v -> doReportPost(themeId, postId, message));
    }

    //Удаление сообщения
    public void deletePost(ThemePost post) {
        new AlertDialog.Builder(getContext())
                .setMessage("Удалить пост ".concat(post.getNick()).concat(" ?"))
                .setPositiveButton("Да", (dialogInterface, i) -> doDeletePost(post.getId()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    protected void doDeletePost(int postId) {
        helperSubscriber.subscribe(Api.Theme().deletePost(postId), s -> toast(!s.isEmpty() ? "Сообщение удалено" : "Ошибка"), "", v -> doDeletePost(postId));
    }

    //Изменение репутации сообщения
    public void votePost(ThemePost post, boolean type) {
        helperSubscriber.subscribe(Api.Theme().votePost(post.getId(), type), s -> toast(s.isEmpty() ? "Неизвестная ошибка" : s), "", v -> votePost(post, type));
    }

    //Изменение репутации пользователя
    @SuppressLint("InflateParams")
    public void changeReputation(ThemePost post, boolean type) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText((type ? "Повысить" : "Понизить").concat(" репутацию ").concat(post.getNick()).concat(" ?"));

        new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton("Да", (dialogInterface, i) -> doChangeReputation(post.getId(), post.getUserId(), type, messageField.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    protected void doChangeReputation(int postId, int userId, boolean type, String message) {
        helperSubscriber.subscribe(Api.Theme().changeReputation(postId, userId, type, message), s -> toast(s.isEmpty() ? "Репутация изменена" : s), "error", v -> doChangeReputation(postId, userId, type, message));
    }
}
