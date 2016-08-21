package forpdateam.ru.forpda;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.permission.RxPermissions;

public class MainActivity extends AppCompatActivity implements TabManager.TabListener {
    public final static String DEF_TITLE = "ForPDA";
    private TabDrawer tabDrawer;
    private MenuDrawer menuDrawer;
    private final View.OnClickListener toggleListener = view -> menuDrawer.toggleState();
    private final View.OnClickListener removeTabListener = view -> backHandler();

    public View.OnClickListener getToggleListener() {
        return toggleListener;
    }

    public View.OnClickListener getRemoveTabListener() {
        return removeTabListener;
    }

    public MainActivity() {
        TabManager.init(this, this);
    }

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
                if (MainActivity.this.getCurrentFocus() != null)
                    ((InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
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
        RxPermissions permissions = RxPermissions.getInstance(this);

        TabManager.getInstance().loadState(savedInstanceState);
        TabManager.getInstance().update();
        //IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("kek", "onnewintent "+intent);
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
        backHandler();
    }
    public void backHandler(){
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

        menu.add("logout").setOnMenuItemClickListener(menuItem -> {
            new Task().execute();
            return false;
        });
        menu.add("test").setOnMenuItemClickListener(menuItem -> {
            IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost");
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
                Api.Auth().tryLogout();
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
                Api.Auth().doOnLogout();
            }
        }
    }
}
