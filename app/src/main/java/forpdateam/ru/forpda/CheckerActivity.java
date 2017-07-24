package forpdateam.ru.forpda;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 24.07.17.
 */

public class CheckerActivity extends AppCompatActivity {
    public final static String JSON_LINK = "https://raw.githubusercontent.com/radiationx/forpda/master/check.json";
    public final static String JSON_SOURCE = "json_source";
    private Toolbar toolbar;
    private TextView currentInfo;
    private TextView updateInfo;
    private Button updateButton;
    private LinearLayout updateContent;
    private View divider;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        currentInfo = (TextView) findViewById(R.id.current_info);
        updateInfo = (TextView) findViewById(R.id.update_info);
        updateButton = (Button) findViewById(R.id.update_button);
        updateContent = (LinearLayout) findViewById(R.id.update_content);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        divider = findViewById(R.id.divider);

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back);


        currentInfo.setText(generateCurrentInfo(BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE));

        toolbar.getMenu().add("Обновить")
                .setIcon(App.getAppDrawable(toolbar.getContext(), R.drawable.ic_toolbar_refresh))
                .setOnMenuItemClickListener(item -> {
                    refreshInfo();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        String jsonSource = null;
        if (getIntent() != null) {
            jsonSource = getIntent().getStringExtra(JSON_SOURCE);

        }
        Log.e("SUKA_CHECK", "" + getIntent() + " : " + jsonSource);

        if (jsonSource != null) {
            checkSource(jsonSource);
        } else {
            refreshInfo();
        }
    }

    private void setRefreshing(boolean isRefreshing) {
        if (isRefreshing) {
            progressBar.setVisibility(View.VISIBLE);
            updateInfo.setVisibility(View.GONE);
            updateContent.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateInfo.setVisibility(View.VISIBLE);
            updateContent.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }
    }

    private void refreshInfo() {
        setRefreshing(true);
        updateContent.removeAllViews();
        Observable.fromCallable(() -> {
            NetworkResponse response = Client.getInstance().get(JSON_LINK);
            String body;
            body = response.getBody();
            return body;
        })
                .onErrorReturn(throwable -> "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkSource);
    }

    private void checkSource(String jsonSource) {
        setRefreshing(false);
        if (jsonSource.length() == 0) {
            return;
        }
        try {
            final JSONObject jsonBody = new JSONObject(jsonSource);
            final JSONObject updateObject = jsonBody.getJSONObject("update");
            checkUpdate(updateObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate(JSONObject updateObject) throws JSONException {
        final int currentVersionCode = BuildConfig.VERSION_CODE;
        final int versionCode = Integer.parseInt(updateObject.getString("version_code"));

        if (versionCode > currentVersionCode) {
            final String versionName = updateObject.getString("version_name");
            final String versionBuild = updateObject.getString("version_build");
            final String buildDate = updateObject.getString("build_date");

            final String linkGit = updateObject.getString("link_github");
            final String link4pda = updateObject.getString("link_4pda");

            final JSONObject changesObject = updateObject.getJSONObject("changes");
            final JSONArray important = changesObject.getJSONArray("important");
            final JSONArray added = changesObject.getJSONArray("added");
            final JSONArray fixed = changesObject.getJSONArray("fixed");
            final JSONArray changed = changesObject.getJSONArray("changed");

            updateInfo.setText(generateCurrentInfo(versionName, buildDate));
            addSection("Важно", important);
            addSection("Добавлено", added);
            addSection("Исправлено", fixed);
            addSection("Изменено", changed);

            updateInfo.setVisibility(View.VISIBLE);
            updateContent.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            updateButton.setOnClickListener(v -> {
                if (ClientHelper.getAuthState()) {
                    CharSequence[] items = {"github.com", "4pda.ru"};
                    new AlertDialog.Builder(CheckerActivity.this)
                            .setTitle("Загрузить с")
                            .setItems(items, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        IntentHandler.handleDownload(linkGit);
                                        break;
                                    case 1:
                                        IntentHandler.handleDownload(link4pda);
                                        break;
                                }
                            })
                            .show();

                } else {
                    IntentHandler.handleDownload(linkGit);
                }
            });
        } else {
            updateInfo.setText("Обновлений нет");
            updateInfo.setVisibility(View.VISIBLE);
            updateContent.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            updateButton.setOnClickListener(null);
        }
    }

    private void addSection(String title, JSONArray array) {
        if (array == null || array.length() == 0) {
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, App.px24);

        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setPadding(0, 0, 0, App.px8);
        sectionTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(sectionTitle);

        StringBuilder stringBuilder = new StringBuilder();


        for (int i = 0; i < array.length(); i++) {
            try {
                String item = array.getString(i);
                stringBuilder.append("— ").append(item);
                if (i + 1 < array.length()) {
                    stringBuilder.append("<br>");
                }
            } catch (JSONException ignore) {
            }
        }

        TextView sectionText = new TextView(this);
        sectionText.setText(Utils.spannedFromHtml(stringBuilder.toString()));
        sectionText.setPadding(App.px8, 0, 0, 0);
        root.addView(sectionText);

        updateContent.addView(root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private String generateCurrentInfo(String name, String date) {
        return "Версия " + name + "\n" + "Сборка от " + date;
    }
}
