package forpdateam.ru.forpda.utils.ourparser;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.utils.ourparser.htmltags.BaseTag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.H1Tag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.H2Tag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.LiTag;
import forpdateam.ru.forpda.utils.ourparser.htmltags.UlTag;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by radiationx on 13.09.16.
 */
public class FastActivity extends AppCompatActivity {




    private final static int green = Color.argb(48, 0, 255, 0);
    private final static int red = Color.argb(48, 255, 0, 0);
    private final static int blue = Color.argb(255, 0, 0, 255);
    private final OkHttpClient client = new OkHttpClient();

    private int iViews = 0;
    private int iTextViews = 0;
    int iterations = 0;


    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //list = (LinearLayout) findViewById(R.id.menu_list);
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getFav(html);
    }

    private Pattern pattern = Pattern.compile("(<div class=\"article-entry\"[^>]*?>[\\s\\S]*?</div>)[^<]*?<footer");

    float coef = 1;

    private void parse(String html) {
        //coef = 8.2f;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle("loaded");
            }
        });

        final Matcher matcher = pattern.matcher(html);
        Log.d("FORPDA_LOG", "check 3");
        if (matcher.find()) {
            final String finalHtml = matcher.group(1);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final long time = System.currentTimeMillis();
                    //Document document = Document.getFav(Jsoup.getFav(finalHtml).body().html());
                    Document document = Document.parse(finalHtml);
                    Log.d("FORPDA_LOG", "time getFav: " + Math.floor((System.currentTimeMillis() - time) * coef));
                    final long time2 = System.currentTimeMillis();
                    list.addView(recurseUi(document.getRoot()));
                    Log.d("FORPDA_LOG", "time recurse ui:  " + Math.floor((System.currentTimeMillis() - time2) * coef));
                    Log.d("FORPDA_LOG", "point iterations: " + iterations);
                    Log.d("FORPDA_LOG", "point iterations views: " + iViews);
                    Log.d("FORPDA_LOG", "point iterations textviews: " + iTextViews);
                    Log.d("FORPDA_LOG", "time full:  " + Math.floor((System.currentTimeMillis() - time) * coef));
                    getSupportActionBar().setTitle("ui " + Math.floor((System.currentTimeMillis() - time) * coef));
                }
            });

        }
    }


    private final static Pattern p2 = Pattern.compile("^(b|i|u|del|sub|sup|span|a|br)$");
    private Matcher matcher;

    private BaseTag recurseUi(final Element element) {
        //Log.d("FORPDA_LOG", "element "+element.tagName()+" : "+element.getLevel());
        /*if (element.tagName().equals("br"))
            return null;*/
        BaseTag thisView = getViewByTag(element.tagName());
        if (element.tagName().equals("img")) {
            thisView.setImage("http://beardycast.com/".concat(element.attr("src")));
            thisView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(FastActivity.this, element.attr("src"), Toast.LENGTH_SHORT).show();
                }
            });
            //Log.d("FORPDA_LOG", "alt desc " + element.attr("alt"));
            if (element.attr("alt") != null) {
                TextView textView = thisView.setHtmlText(element.attr("alt"));
                thisView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setTextSize(12);
            }

            return thisView;
        }

        String html = element.getText();

        boolean text = true;

        for (int i = 0; i < element.getElements().size(); i++) {
            Element child = element.get(i);
            //Log.d("FORPDA_LOG", "child "+child.tagName()+" : "+child.getLevel());
            BaseTag newView = null;
            if (!text && child.tagName().equals("br"))
                continue;
            matcher = p2.matcher(child.tagName());
            /*boolean res1 = true, res2 = true;*/
            /*for (Element temp : child.getElements()) {
                if (temp.tagName().equals("a")) {
                    for (Element imgs : temp.getElements()) {
                        if (imgs.tagName().equals("img")) {
                            res1 = false;
                        }
                    }
                } else if (!temp.tagName().equals("br")) {
                    res2 = false;
                }
            }*/
            if (/*res1 && res2 &&*/ matcher.matches()) {

                //html = html.concat(child.tagName().equals("p") ? child.htmlNoParent() : child.html());
                html = html.concat(child.tagName().equals("p") ? child.htmlNoParent() : child.html());
                text = true;
                continue;


            } else {
                newView = recurseUi(child);
                if (text) {
                    html = html.trim();
                    //html = html.trim().replaceFirst("<br>$", "");
                    if (!html.isEmpty()) {
                        //html = html.replaceFirst("^<br>", "");
                        //thisView.setHtmlText(Html.fromHtml(html));
                        thisView.setHtmlText(html);
                        iTextViews++;
                        html = "";
                    }
                }
                //html += child.getText();
                /*if (!html.isEmpty()) {

                    //newView.setHtmlText(Html.fromHtml(html));

                }
                String attr = child.attr("align");
                if (attr != null) {
                    if (attr.equals("center")) {
                        newView.setGravity(Gravity.CENTER_HORIZONTAL);
                    } else if (attr.equals("right")) {
                        newView.setGravity(Gravity.END);
                    }
                }*/

                html = "";
                text = false;
            }
            if (newView != null)
                thisView.addView(newView);

            iterations++;
        }
        html = html.trim();
        //html = html.trim().replaceFirst("<br>$", "");
        if (!html.isEmpty()) {
            //html = html.replaceFirst("^<br>", "");
            html = html.concat(element.getAfterText());
            //thisView.setHtmlText(Html.fromHtml(html));
            thisView.setHtmlText(html);
            iTextViews++;
            html = "";
        }
        /*for(int i = 0; i<122; i++){
            thisView.addView(new BaseTag(this, ""));
        }*/
        return thisView;
    }

    private BaseTag getViewByTag(String tag) {
        switch (tag) {
            case "h1":
                return new H1Tag(this);
            case "h2":
                return new H2Tag(this);
            case "ul":
                return new UlTag(this);
            case "li":
                return new LiTag(this);
            default:
                return new BaseTag(this);
        }
    }

    public void run() throws Exception {
        Request request = new Request.Builder()
                .url("http://beardycast.com/2016/08/08/Beardygram/beardygram-5/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                parse(response.body().string());
            }
        });
    }
}

