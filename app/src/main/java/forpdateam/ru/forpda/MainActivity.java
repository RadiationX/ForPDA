package forpdateam.ru.forpda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import forpdateam.ru.forpda.client.NetworkStateReceiver;
import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.permission.RxPermissions;

public class MainActivity extends AppCompatActivity implements TabManager.TabListener {
    public final static String DEF_TITLE = "ForPDA";
    private Queue<ExtendedWebView> webViews = new LinkedList<>();
    private Timer webViewCleaner = new Timer();
    private TabDrawer tabDrawer;
    private MenuDrawer menuDrawer;
    private final View.OnClickListener toggleListener = view -> menuDrawer.toggleState();
    private final View.OnClickListener removeTabListener = view -> backHandler(true);


    public View.OnClickListener getToggleListener() {
        return toggleListener;
    }

    public View.OnClickListener getRemoveTabListener() {
        return removeTabListener;
    }

    public MainActivity() {
        webViewCleaner.schedule(new WebViewCleanerTask(), 0, 60000);
        TabManager.init(this, this);
    }

    private NetworkStateReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        tabDrawer = new TabDrawer(this, drawerLayout);
        menuDrawer = new MenuDrawer(this, drawerLayout);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (TabManager.getInstance().getSize() > 0)
                    TabManager.getInstance().getActive().hidePopupWindows();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        RxPermissions.getInstance(this);

        TabManager.getInstance().loadState(savedInstanceState);
        TabManager.getInstance().updateFragmentList();
        receiver = new NetworkStateReceiver(this);
        receiver.registerReceiver();
        final View viewDiff = findViewById(R.id.fragments_container);
        viewDiff.post(() -> App.setStatusBarHeight(viewDiff.getRootView().getHeight() - viewDiff.getHeight()));
        //IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost");
        checkIntent(getIntent());
    }

    public MenuDrawer getMenuDrawer() {
        return menuDrawer;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
        Log.d("kek", "onnewintent " + intent);
    }

    void checkIntent(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        new Handler().post(() -> {
            Log.d("kek", "POST onnewintent " + intent);
            IntentHandler.handle(intent.getData().toString());
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TabManager.getInstance().saveState(outState);
    }

    public void updateTabList() {
        tabDrawer.notifyTabsChanged();
    }

    @Override
    public void onAddTab(TabFragment fragment) {
        Log.d("kek", "onadd " + fragment);
    }

    @Override
    public void onRemoveTab(TabFragment fragment) {
        Log.d("kek", "onremove " + fragment);
    }

    @Override
    public void onSelectTab(TabFragment fragment) {
        menuDrawer.setActive(fragment.getClass().getSimpleName());
        Log.d("kek", "onselect " + fragment);
    }

    @Override
    public void onChange() {
        updateTabList();
    }

    @Override
    public void onBackPressed() {
        Log.d("kek", "onbackpressed activity");
        backHandler(false);
    }

    public void hideKeyboard() {
        if (MainActivity.this.getCurrentFocus() != null)
            ((InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
    }

    public void showKeyboard(View view) {
        if (MainActivity.this.getCurrentFocus() != null)
            ((InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .showSoftInput(view, 0);
    }

    public void backHandler(boolean isToolbarButton) {
        if (TabManager.getInstance().getSize() <= 1) {
            super.onBackPressed();
        } else {
            if (isToolbarButton || !TabManager.getInstance().getActive().onBackPressed()) {
                hideKeyboard();
                TabManager.getInstance().remove(TabManager.getInstance().getActive());
            }
        }
    }

    public Queue<ExtendedWebView> getWebViews() {
        return webViews;
    }

    @Override
    protected void onResumeFragments() {
        Log.e("SUKA", "ONRESUME_FRAGMENTS");
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("kekos", "ACTIVE TAB " + TabManager.getActiveIndex() + " : " + TabManager.getActiveTag());
        receiver.registerReceiver();

    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.unregisterReceiver();
        Repository.removeInstance();
    }

    class WebViewCleanerTask extends TimerTask {
        public void run() {
            Log.d("kek", "try remove webview ");
            if (webViews.size() > 0) {
                Log.d("kek", "remove webview " + webViews.element().getTag());
                webViews.remove();
            }
        }
    }
}
