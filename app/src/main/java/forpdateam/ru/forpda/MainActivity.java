package forpdateam.ru.forpda;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.client.Client;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    String time, sig;
    String capcha;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WWC());
        Button button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edittext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostTask().execute();
            }
        });

        new Task().execute();
    }

    String taskurl = "http://4pda.ru/forum/index.php?act=login";
    class Task extends AsyncTask {
        String response = "empty";

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                response = Client.getInstance().get(taskurl);
                Matcher matcher = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"").matcher(response);
                if (matcher.find()) {
                    time = matcher.group(1);
                    sig = matcher.group(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            webView.loadDataWithBaseURL("http://4pda.ru/", response, "text/html", "UTF-8", null);
        }
    }

    class PostTask extends AsyncTask {
        String response = "empty";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            capcha = editText.getText().toString();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("captcha-time", time);
                headers.put("captcha-sig", sig);
                headers.put("captcha", capcha);
                headers.put("return", "http://4pda.ru/");
                headers.put("login", "radiation15");
                headers.put("password", "you_pass");
                headers.put("remember", "1");

                response = Client.getInstance().post("http://4pda.ru/forum/index.php?act=auth", headers);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            webView.loadDataWithBaseURL("http://4pda.ru/", response, "text/html", "UTF-8", null);
        }
    }

    class WWC extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            taskurl = url;
            new Task().execute();
            return true;
        }

    }
}
