package forpdateam.ru.forpda;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import forpdateam.ru.forpda.utils.LocaleHelper;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 23.07.17.
 */

public class WebVewNotFoundActivity extends AppCompatActivity {
    private ImageView getInGp, getIn4pda;
    private Button tryStart;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wv_not_found);
        getInGp = (ImageView) findViewById(R.id.get_in_gp);
        getIn4pda = (ImageView) findViewById(R.id.get_in_4pda);
        tryStart = (Button) findViewById(R.id.wv_try_start);

        getInGp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview")).addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK));
        });

        getIn4pda.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://4pda.ru/forum/index.php?showtopic=705513")).addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK));
        });

        tryStart.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class)
                    .putExtra(MainActivity.CHECK_WEBVIEW, false);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
