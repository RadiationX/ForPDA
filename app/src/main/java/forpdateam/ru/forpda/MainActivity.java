package forpdateam.ru.forpda;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.test.LoginActivity;
import forpdateam.ru.forpda.test.NewsListActivity;
import forpdateam.ru.forpda.utils.permission.RxPermissions;

public class MainActivity extends AppCompatActivity {
    Button testLogin;
    Button testNewsList;
    private RxPermissions permissions;
    Button testLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions = RxPermissions.getInstance(this);

        testLogin = (Button) findViewById(R.id.button);
        testNewsList = (Button) findViewById(R.id.button3);
        testLogout = (Button) findViewById(R.id.button4);
        testLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        testNewsList.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewsListActivity.class);
            startActivity(intent);
        });
        testLogout.setOnClickListener(view -> new Task().execute());
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
            }
        }
    }
}
