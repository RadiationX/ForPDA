package forpdateam.ru.forpda.test;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.login.LoginForm;

/**
 * Created by radiationx on 29.07.16.
 */
public class LoginActivity extends AppCompatActivity {
    EditText login, password, captcha;
    ImageView captchaImage;
    LoginForm loginForm;
    Button send;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
        captcha = (EditText) findViewById(R.id.editText3);
        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        send = (Button) findViewById(R.id.button2);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostTask().execute();
            }
        });
        new Task().execute();
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
                loginForm = Api.Login().loadForm();
                Log.d("kek", loginForm.getBody());
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (exception != null) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setMessage(exception.getMessage())
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Task().execute();
                            }
                        })
                        .create()
                        .show();
            } else {
                ImageLoader.getInstance().displayImage(loginForm.getCaptchaImageUrl(), captchaImage);
            }
        }
    }

    class PostTask extends AsyncTask<String, Void, Boolean> {
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginForm.setCaptcha(captcha.getText().toString());
            loginForm.setLogin(login.getText().toString());
            loginForm.setPassword(password.getText().toString());
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                return Api.Login().tryLogin(loginForm);
            } catch (Exception e) {
                exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean o) {
            if (o) {
                Toast.makeText(LoginActivity.this, "You are logined", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(exception != null ? "Error" : "Retry?")
                        .setMessage(exception != null ? exception.getMessage() : null)
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Task().execute();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }
}
