package forpdateam.ru.forpda.test;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.newslist.NewsItem;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsListActivity extends AppCompatActivity {
    TextView text;
    List<NewsItem> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);
        text = (TextView) findViewById(R.id.textView2);
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
                list = Api.NewsList().get("http://4pda.ru/");
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (exception != null) {
                new AlertDialog.Builder(NewsListActivity.this)
                        .setMessage(exception.getMessage())
                        .setPositiveButton("Retry", (dialogInterface, i) -> {
                            new Task().execute();
                        })
                        .create()
                        .show();
            } else {
                String titles = "";
                for (NewsItem item : list) {
                    titles += item.getTitle() + ";" + "\n\n";
                }
                text.setText(titles);
            }
        }
    }
}
