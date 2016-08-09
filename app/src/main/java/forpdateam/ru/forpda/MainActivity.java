package forpdateam.ru.forpda;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.test.LoginFragment;
import forpdateam.ru.forpda.test.NewsListFragment;
import forpdateam.ru.forpda.test.ProfileFragment;
import forpdateam.ru.forpda.test.QmsFragment;
import forpdateam.ru.forpda.test.ThemeFragment;
import forpdateam.ru.forpda.utils.permission.RxPermissions;

public class MainActivity extends AppCompatActivity implements TabManager.UpdateListener {
    private TabDrawer tabDrawer;
    private RxPermissions permissions;
    private boolean lastHamburgerState = true;
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();
    private final View.OnClickListener toggleListener = view -> tabDrawer.toggleState();
    private final View.OnClickListener removeTabListener = view -> TabManager.getInstance().remove(TabManager.getActiveTag());
    public Toolbar toolbar;

    public MainActivity() {
        TabManager.init(this, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabManager.getInstance().loadState(savedInstanceState);
        TabManager.getInstance().update();
        tabDrawer = new TabDrawer(this);
        tabDrawer.getDrawerLayout().addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (MainActivity.this.getCurrentFocus() != null)
                    ((InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                TabManager.getInstance().getActive().hidePopupWindows();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        permissions = RxPermissions.getInstance(this);
    }


    public void setHamburgerState(boolean state) {
        if (toolbar == null || state == lastHamburgerState) return;
        if (state) {
            toolbar.setNavigationOnClickListener(toggleListener);
            tabDrawer.getDrawerLayout().addDrawerListener(tabDrawer.getToggle());
        } else {
            toolbar.setNavigationOnClickListener(removeTabListener);
            tabDrawer.getDrawerLayout().removeDrawerListener(tabDrawer.getToggle());
        }
        lastHamburgerState = state;
        ValueAnimator anim = ValueAnimator.ofFloat(state ? 1.0f : 0.0f, state ? 0.0f : 1.0f);
        anim.addUpdateListener(valueAnimator -> tabDrawer.getToggle().onDrawerSlide(tabDrawer.getDrawerLayout(), (float) valueAnimator.getAnimatedValue()));
        anim.setInterpolator(interpolator);
        anim.setDuration(225);
        anim.start();
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
        Log.d("kek", "onselect " + fragment);
    }

    @Override
    public void onChange() {
        updateTabList();
    }

    @Override
    public void onBackPressed() {
        if (TabManager.getInstance().getSize() > 1) {
            if (!TabManager.getInstance().getActive().onBackPressed()) {
                TabManager.getInstance().remove(TabManager.getInstance().getActive());
            }
        } else {
            new AlertDialog.Builder(this)
                    .setPositiveButton("yes", (dialogInterface, i) -> {
                        super.onBackPressed();
                    })
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("kekos", "ACTIVE TAB " + TabManager.getActiveIndex() + " : " + TabManager.getActiveTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null)
            menu.clear();
        else
            menu = new MenuBuilder(this);


        menu.add("login").setOnMenuItemClickListener(menuItem -> {
            TabManager.getInstance().add(LoginFragment.newInstance("login"));
            return false;
        });
        menu.add("logout").setOnMenuItemClickListener(menuItem -> {
            new Task().execute();
            return false;
        });
        menu.add("news").setOnMenuItemClickListener(menuItem -> {
            TabManager.getInstance().add(NewsListFragment.newInstance("news"));
            return false;
        });
        menu.add("profile").setOnMenuItemClickListener(menuItem -> {
            TabManager.getInstance().add(ProfileFragment.newInstance("profile"));
            return false;
        });
        menu.add("qms").setOnMenuItemClickListener(menuItem -> {
            TabManager.getInstance().add(QmsFragment.newInstance("qms-huis"));
            return false;
        });
        menu.add("theme").setOnMenuItemClickListener(menuItem -> {
            TabManager.getInstance().add(ThemeFragment.newInstance("theme"));
            return false;
        });
        return true;
    }

    class Task extends AsyncTask {
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Api.Login().tryLogout();
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (exception != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(exception.getMessage())
                        .create()
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "logout complete", Toast.LENGTH_LONG).show();
            }
        }
    }
}
