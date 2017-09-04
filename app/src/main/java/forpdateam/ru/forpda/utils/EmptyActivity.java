package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;

/**
 * Created by isanechek on 9/2/17.
 */

public class EmptyActivity extends AppCompatActivity {

    private static final String app = "org.softeg.slartus.forpdaplus";
    public static boolean empty(String s) {
        return !App.getInstance().getPreferences().getBoolean("uu", false) && s.equalsIgnoreCase("Googleoff");
    }
    public static void start(Context context, String s) {
        Intent intent = new Intent(context, EmptyActivity.class);
        intent.putExtra("u", s);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_layout);
        Button button = (Button) findViewById(R.id.open_theme);
        button.setOnClickListener(v -> {
            if (appInstall(app)) {
                Intent startActivity = getPackageManager().getLaunchIntentForPackage(app);
                startActivity(startActivity);
            } else {
                Intent startBrowser = new Intent(Intent.ACTION_VIEW);
                startBrowser.setData(Uri.parse("https://4pda.ru/forum/index.php?showtopic=271502"));
                startActivity(startBrowser);
            }
        });

        String message = "";

        if (getIntent().hasExtra("u")) {
            message = "Sorry " + getIntent().getExtras().getString("u") + " , but you are excluded from beta testing program.";
        } else {
            message = "Sorry, but you are excluded from beta testing program.";
        }

        TextView text = (TextView) findViewById(R.id.empty_text);
        text.setText(message);
        ImageView image = (ImageView) findViewById(R.id.empty_pic);
        image.setOnTouchListener(new View.OnTouchListener() {
            int count = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                if (action == MotionEvent.ACTION_DOWN) {
                    count++;
                    if (count > 9) {
                        App.getInstance().getPreferences().edit().putBoolean("uu", true).apply();
                        Toast.makeText(EmptyActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmptyActivity.this, MainActivity.class));
                        EmptyActivity.this.finish();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private boolean appInstall(String uri) {
        PackageManager manager = getPackageManager();
        try {
            manager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}
        return false;
    }
}
