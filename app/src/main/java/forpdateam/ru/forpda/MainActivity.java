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
import forpdateam.ru.forpda.views.drawers.DrawerHeader;
import forpdateam.ru.forpda.views.drawers.Drawers;

public class MainActivity extends AppCompatActivity implements TabManager.TabListener {
    public final static String DEF_TITLE = "ForPDA";
    private Queue<ExtendedWebView> webViews = new LinkedList<>();
    private Timer webViewCleaner = new Timer();
    private Drawers drawers;
    private DrawerHeader drawerHeader;
    private final View.OnClickListener toggleListener = view -> drawers.toggleMenu();
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
        drawers = new Drawers(this, drawerLayout);
        drawers.init(savedInstanceState);
        drawerHeader = new DrawerHeader(this, drawerLayout);
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


        receiver = new NetworkStateReceiver(this);
        receiver.registerReceiver();
        final View viewDiff = findViewById(R.id.fragments_container);
        viewDiff.post(() -> {
            App.setStatusBarHeight(((View) viewDiff.getParent()).getHeight() - viewDiff.getHeight());
            App.setNavigationBarHeight(viewDiff.getRootView().getHeight() - viewDiff.getHeight() - App.getStatusBarHeight());
            Log.e("FORPDA_LOG", "SB: " + App.getStatusBarHeight() + ", NB: " + App.getNavigationBarHeight());
            drawers.setStatusBarHeight(App.getStatusBarHeight());
            //IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=2556269");
        });

        Log.e("FORPDA_LOG", "ON CREATE INTENT");
        checkIntent(getIntent());
    }

    public Drawers getDrawers() {
        return drawers;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("FORPDA_LOG", "ON NEW INTENT");
        checkIntent(intent);

        Log.d("FORPDA_LOG", "onnewintent " + intent.toString());
    }

    void checkIntent(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        new Handler().post(() -> {
            Intent newIntent = intent;
            /*if (intent.getAction().equals("FORPDA_LOG.SOSI.HUI")) {
                newIntent = new Intent();
                newIntent.setAction(Intent.ACTION_MAIN);
                newIntent.addCategory(Intent.CATEGORY_HOME);
                newIntent.setData(intent.getData());
                Log.e("FORPDA_LOG", "INTENT FORPDA_LOG " + newIntent);
            } else {
                newIntent = intent;
            }*/
            /*Log.e("FORPDA_LOG", "FLAG_ACTIVITY_NO_HISTORY " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_NO_HISTORY));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_SINGLE_TOP " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_SINGLE_TOP));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_NEW_TASK " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_MULTIPLE_TASK " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_CLEAR_TOP " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_FORWARD_RESULT " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_FORWARD_RESULT));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_PREVIOUS_IS_TOP " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_BROUGHT_TO_FRONT " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_NO_USER_ACTION " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_NO_USER_ACTION));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_REORDER_TO_FRONT " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_NO_ANIMATION " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_NO_ANIMATION));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_CLEAR_TASK " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Log.e("FORPDA_LOG", "FLAG_ACTIVITY_TASK_ON_HOME " + (newIntent.getFlags() & Intent.FLAG_ACTIVITY_TASK_ON_HOME));*/
            Log.d("FORPDA_LOG", "POST on new intent " + newIntent);
            IntentHandler.handle(newIntent.getData().toString());
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TabManager.getInstance().saveState(outState);
    }

    public void updateTabList() {
        drawers.notifyTabsChanged();
    }

    @Override
    public void onAddTab(TabFragment fragment) {
        Log.d("FORPDA_LOG", "onadd " + fragment);
    }

    @Override
    public void onRemoveTab(TabFragment fragment) {
        Log.d("FORPDA_LOG", "onremove " + fragment);
    }

    @Override
    public void onSelectTab(TabFragment fragment) {
        drawers.setActiveMenu(fragment);
        Log.d("FORPDA_LOG", "onselect " + fragment);
    }

    @Override
    public void onChange() {
        updateTabList();
    }

    @Override
    public void onBackPressed() {
        Log.d("FORPDA_LOG", "onbackpressed activity");
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

    public void backHandler(boolean fromToolbar) {
        /*if (TabManager.getInstance().getSize() <= 1) {
            super.onBackPressed();
        } else {
            if (fromToolbar || !TabManager.getInstance().getActive().onBackPressed()) {
                hideKeyboard();
                TabManager.getInstance().remove(TabManager.getInstance().getActive());
            }
        }*/

        if (fromToolbar || !TabManager.getInstance().getActive().onBackPressed()) {
            hideKeyboard();
            TabManager.getInstance().remove(TabManager.getInstance().getActive());
            if (TabManager.getInstance().getSize() < 1) {
                finish();
            }
        }
    }

    public Queue<ExtendedWebView> getWebViews() {
        return webViews;
    }

    @Override
    protected void onResumeFragments() {
        Log.e("FORPDA_LOG", "ONRESUME_FRAGMENTS");
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("kekos", "ACTIVE TAB " + TabManager.getActiveIndex() + " : " + TabManager.getActiveTag());
        receiver.registerReceiver();
        drawers.setStatusBarHeight(App.getStatusBarHeight());
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
        drawers.destroy();
        drawerHeader.destroy();
        webViewCleaner.cancel();
        webViewCleaner.purge();
        webViews.clear();
        Log.e("FORPDA_LOG", "ACTIVITY DESTROY");
    }

    class WebViewCleanerTask extends TimerTask {
        public void run() {
            Log.d("FORPDA_LOG", "try remove webview " + this);
            if (webViews.size() > 0) {
                Log.d("FORPDA_LOG", "remove webview " + webViews.element().getTag());
                webViews.remove();
            }
        }
    }
}
