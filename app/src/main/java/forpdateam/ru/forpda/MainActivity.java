package forpdateam.ru.forpda;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import forpdateam.ru.forpda.test.LoginActivity;

public class MainActivity extends AppCompatActivity {
    Button testLogin;
    Button clearCookies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testLogin = (Button) findViewById(R.id.button);
        clearCookies = (Button) findViewById(R.id.button3);
        testLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        clearCookies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("kek", App.getInstance().getPreferences().getString("cookie_member_id", "default"));
                App.getInstance().getPreferences().edit().remove("cookie_member_id").remove("cookie_pass_hash").apply();
                Log.d("kek", App.getInstance().getPreferences().getString("cookie_member_id", "default"));
                Toast.makeText(MainActivity.this, "Cookies deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
