package forpdateam.ru.forpda;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.client.NetworkStateReceiver;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.EmptyActivity;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.LocaleHelper;
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
    private boolean currentThemeIsDark = App.get().isDarkTheme();


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
        if (EmptyActivity.empty(App.get().getPreferences().getString("auth.user.nick", ""))) {
            startActivity(new Intent(this, EmptyActivity.class));
            finish();
            return;
        }
        if (App.get().isWebViewNotFound()) {
            startActivity(new Intent(this, WebVewNotFoundActivity.class));
            finish();
            return;
        }
        currentThemeIsDark = App.get().isDarkTheme();
        setTheme(currentThemeIsDark ? R.style.DarkAppTheme_NoActionBar : R.style.LightAppTheme_NoActionBar);
        setContentView(R.layout.activity_main);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        /*
        * Т.к. 2 вьюхи, делаю цвет в 2 раза прозрачнее, чтобы компенсировать это.
        * P.S. Чем больше вьюх в DrawerLayout находятся до NavigationView, тем сильнее будет затенение
        * P.S.S. Первая вьюха - контейнер фрагментов, вторая - view_for_measure
        * */
        drawerLayout.setScrimColor(0x4C000000);
        drawers = new Drawers(this, drawerLayout);
        drawers.init(savedInstanceState);
        drawerHeader = new DrawerHeader(this, drawerLayout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.get().getPreferences().getBoolean("drawers.tooltip.link_open", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(drawerView.findViewById(R.id.drawer_header_open_link))
                                .text(R.string.tooltip_link)
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
                        App.get().getPreferences().edit().putBoolean("drawers.tooltip.link_open", false).apply();
                    }

                }

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView.getId() == R.id.menu_drawer) {
                    if (App.get().getPreferences().getBoolean("drawers.tooltip.tabs_drawer", true)) {
                        SimpleTooltip tooltip = new SimpleTooltip.Builder(MainActivity.this)
                                .anchorView(drawers.getTabDrawer())
                                .text(R.string.tooltip_tabs)
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
                        App.get().getPreferences().edit().putBoolean("drawers.tooltip.tabs_drawer", false).apply();
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    if (TabManager.getInstance().getSize() > 0)
                        TabManager.getInstance().getActive().hidePopupWindows();
                }
            }
        });
        RxPermissions.getInstance(this);

        receiver = new NetworkStateReceiver();
        receiver.registerReceiver();

        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(R.id.fragments_container));
        keyboardUtil.enable();

        final View viewDiff = findViewById(R.id.view_for_measure);
        viewDiff.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> measureView(v));
        //viewDiff.post(() -> measureView(viewDiff));

        if (Preferences.Notifications.Update.isEnabled()) {
            new SimpleChecker().checkFromGitHub(this);
        }
        checkIntent(getIntent());

    }

    private void measureView(View v) {
        Log.d(LOG_TAG, "Calc SOOOKA " + ((int) (((View) v.getParent()).getTop())) + " : " + v.getTop() + " : " + v.getRootView().getTop() + " : " + v.getRootView().getHeight() + " : " + findViewById(R.id.fragments_container).getHeight());

        int lastHeight = App.getStatusBarHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            App.setStatusBarHeight((int) (((View) v.getParent()).getTop() + v.getTop()));
            App.setNavigationBarHeight(v.getRootView().getHeight() - findViewById(R.id.fragments_container).getHeight());
        }


        if (lastHeight != App.getStatusBarHeight()) {
            Log.d(LOG_TAG, "Calc SB: " + App.getStatusBarHeight() + ", NB: " + App.getNavigationBarHeight());
            App.get().getStatusBarSizeObservables().notifyObservers();
        }
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
            if (TabManager.getInstance().getSize() == 0) {
                drawers.firstSelect();
            }
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
        /*if (TabManager.get().getSize() <= 1) {
            super.onBackPressed();
        } else {
            if (fromToolbar || !TabManager.get().getActive().onBackPressed()) {
                hideKeyboard();
                TabManager.get().remove(TabManager.get().getActive());
            }
        }*/

        TabFragment active = TabManager.getInstance().getActive();
        if (active == null) {
            finish();
            return;
        }
        if (fromToolbar || !active.onBackPressed()) {
            hideKeyboard();
            TabManager.getInstance().remove(active);
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    private String lang = null;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        Log.d(LOG_TAG, "TabManager active tab: " + TabManager.getActiveIndex() + " : " + TabManager.getActiveTag());
        if (receiver != null)
            receiver.registerReceiver();
        if (lang == null) {
            lang = LocaleHelper.getLanguage(this);
        }
        if (!LocaleHelper.getLanguage(this).equals(lang)) {
            Context newContext = LocaleHelper.onAttach(this);
            new AlertDialog.Builder(this)
                    .setMessage(newContext.getString(R.string.lang_changed))
                    .setPositiveButton(newContext.getString(R.string.ok), (dialog, which) -> {
                        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton(newContext.getString(R.string.cancel), null)
                    .show();
        }
        if (currentThemeIsDark != App.get().isDarkTheme()) {
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
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
