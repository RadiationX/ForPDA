package forpdateam.ru.forpda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.client.NetworkStateReceiver;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.WebViewsProvider;
import forpdateam.ru.forpda.utils.permission.RxPermissions;
import forpdateam.ru.forpda.views.drawers.DrawerHeader;
import forpdateam.ru.forpda.views.drawers.Drawers;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class MainActivity extends AppCompatActivity implements TabManager.TabListener {
    public final static String LOG_TAG = MainActivity.class.getSimpleName();
    public final static String DEF_TITLE = "ForPDA";
    private WebViewsProvider webViewsProvider;
    private Drawers drawers;
    private DrawerHeader drawerHeader;
    private final View.OnClickListener toggleListener = view -> drawers.toggleMenu();
    private final View.OnClickListener removeTabListener = view -> backHandler(true);
    private List<SimpleTooltip> tooltips = new ArrayList<>();
    private boolean currentThemeIsDark = App.getInstance().isDarkTheme();


    public View.OnClickListener getToggleListener() {
        return toggleListener;
    }

    public View.OnClickListener getRemoveTabListener() {
        return removeTabListener;
    }

    public MainActivity() {
        webViewsProvider = new WebViewsProvider();
        TabManager.init(this, this);
    }

    private NetworkStateReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isWebViewNotFound()) {
            startActivity(new Intent(this, WebVewNotFoundActivity.class));
            finish();
            return;
        }
        currentThemeIsDark = App.getInstance().isDarkTheme();
        setTheme(currentThemeIsDark ? R.style.DarkAppTheme_NoActionBar : R.style.LightAppTheme_NoActionBar);
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
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.getInstance().getPreferences().getBoolean("drawers.tooltip.link_open", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(drawerView.findViewById(R.id.drawer_header_open_link))
                                .text("Вы можете вручную переходить по ссылкам")
                                .gravity(Gravity.BOTTOM)
                                .animated(false)
                                .modal(true)
                                .transparentOverlay(false)
                                .backgroundColor(Color.BLACK)
                                .textColor(Color.WHITE)
                                .padding((float) App.px16)
                                .onDismissListener(simpleTooltip -> tooltips.remove(simpleTooltip))
                                .build();
                        tooltip.show();
                        tooltips.add(tooltip);
                        App.getInstance().getPreferences().edit().putBoolean("drawers.tooltip.link_open", false).apply();
                    }

                }

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.getInstance().getPreferences().getBoolean("drawers.tooltip.tabs_drawer", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(drawers.getTabDrawer())
                                .text("Справа находится панель с открытыми вкладками.\nОна позволяет удобно осуществлять навигацию между ними")
                                .gravity(Gravity.START)
                                .animated(false)
                                .modal(true)
                                .transparentOverlay(false)
                                .backgroundColor(Color.BLACK)
                                .textColor(Color.WHITE)
                                .padding((float) App.px16)
                                .onDismissListener(simpleTooltip -> tooltips.remove(simpleTooltip))
                                .build();

                        tooltip.show();
                        tooltips.add(tooltip);
                        App.getInstance().getPreferences().edit().putBoolean("drawers.tooltip.tabs_drawer", false).apply();
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        RxPermissions.getInstance(this);

        receiver = new NetworkStateReceiver();
        receiver.registerReceiver();
        final View viewDiff = findViewById(R.id.fragments_container);
        viewDiff.post(() -> {
            App.setStatusBarHeight(((View) viewDiff.getParent()).getHeight() - viewDiff.getHeight());
            App.setNavigationBarHeight(viewDiff.getRootView().getHeight() - viewDiff.getHeight() - App.getStatusBarHeight());
            Log.d(LOG_TAG, "Calc SB: " + App.getStatusBarHeight() + ", NB: " + App.getNavigationBarHeight());
            drawers.setStatusBarHeight(App.getStatusBarHeight());
            //IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=2556269");
        });

        if (Preferences.Notifications.Update.isEnabled()) {
            new SimpleChecker().checkFromGitHub(this);
        }
        checkIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*Intent serviceIntent = new Intent(App.getContext(), NotificationsService.class);
        startService(serviceIntent);*/
        App.getContext().startService(new Intent(App.getContext(), NotificationsService.class).setAction(NotificationsService.CHECK_LAST_EVENTS));
    }

    public Drawers getDrawers() {
        return drawers;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent " + intent.toString());
        checkIntent(intent);
    }

    void checkIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            drawers.firstSelect();
            return;
        }

        new Handler().post(() -> {
            Log.d(LOG_TAG, "Handler.post checkIntent: " + intent);
            boolean handled = IntentHandler.handle(intent.getData().toString());
            if (!handled || TabManager.getInstance().getSize() == 0) {
                drawers.firstSelect();
            }
            setIntent(null);
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
        Log.d(LOG_TAG, "TabManager callback onAddTab " + fragment);
    }

    @Override
    public void onRemoveTab(TabFragment fragment) {
        Log.d(LOG_TAG, "TabManager callback onRemoveTab " + fragment);
    }

    @Override
    public void onSelectTab(TabFragment fragment) {
        Log.d(LOG_TAG, "TabManager callback onSelectTab " + fragment);
        drawers.setActiveMenu(fragment);
    }

    @Override
    public void onChange() {
        Log.d(LOG_TAG, "TabManager callback onChange");
        updateTabList();
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed");
        if (tooltips.size() > 0) {
            tooltips.get(tooltips.size() - 1).dismiss();
            return;
        }
        if (drawers.isMenuOpen()) {
            drawers.closeMenu();
            return;
        }
        if (drawers.isTabsOpen()) {
            drawers.closeTabs();
            return;
        }
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

    public WebViewsProvider getWebViewsProvider() {
        return webViewsProvider;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Log.d(LOG_TAG, "onResumeFragments");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onPause");
        Log.d(LOG_TAG, "TabManager active tab: " + TabManager.getActiveIndex() + " : " + TabManager.getActiveTag());
        if (receiver != null)
            receiver.registerReceiver();
        if (drawers != null)
            drawers.setStatusBarHeight(App.getStatusBarHeight());
        if (currentThemeIsDark != App.getInstance().isDarkTheme()) {
            recreate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (receiver != null)
            receiver.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (receiver != null) {
            receiver.unregisterReceiver();
        }
        if (drawers != null) {
            drawers.destroy();
        }
        if (drawerHeader != null) {
            drawerHeader.destroy();
        }
        if (webViewsProvider != null) {
            webViewsProvider.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
