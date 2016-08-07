package forpdateam.ru.forpda;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.test.LoginFragment;
import forpdateam.ru.forpda.test.NewsListFragment;
import forpdateam.ru.forpda.test.ProfileFragment;
import forpdateam.ru.forpda.test.QmsFragment;
import forpdateam.ru.forpda.test.ThemeFragment;
import forpdateam.ru.forpda.utils.permission.RxPermissions;

public class MainActivity extends AppCompatActivity implements TabManager.UpdateListener {
    public TabManager tabManager = new TabManager(this, this);
    private TabDrawer tabDrawer;
    private RxPermissions permissions;
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabManager.loadState(savedInstanceState);
        tabManager.update();
        tabDrawer = new TabDrawer(this);

        permissions = RxPermissions.getInstance(this);
    }

    public void updateTabList() {
        tabDrawer.notifyTabsChanged();
    }

    @Override
    public void onAddTab(TabFragment fragment) {

    }

    @Override
    public void onRemoveTab(TabFragment fragment) {

    }

    @Override
    public void onSelectTab(TabFragment fragment) {

    }

    @Override
    public void onChange() {
        updateTabList();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null)
            menu.clear();
        else
            menu = new MenuBuilder(this);

        menu.add("login").setOnMenuItemClickListener(menuItem -> {
            tabManager.add(LoginFragment.newInstance("login"));
            return false;
        });
        menu.add("logout").setOnMenuItemClickListener(menuItem -> {
            new Task().execute();
            return false;
        });
        menu.add("news").setOnMenuItemClickListener(menuItem -> {
            tabManager.add(NewsListFragment.newInstance("news"));
            return false;
        });
        menu.add("profile").setOnMenuItemClickListener(menuItem -> {
            tabManager.add(ProfileFragment.newInstance("profile"));
            return false;
        });
        menu.add("qms").setOnMenuItemClickListener(menuItem -> {
            tabManager.add(QmsFragment.newInstance("qms"));
            return false;
        });
        menu.add("theme").setOnMenuItemClickListener(menuItem -> {
            tabManager.add(ThemeFragment.newInstance("theme"));
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
