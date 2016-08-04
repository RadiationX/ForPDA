package forpdateam.ru.forpda.test.testdevdb;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import forpdateam.ru.forpda.R;

/**
 * Created by isanechek on 30.07.16.
 */

public class TestDevDbActivity extends AppCompatActivity {
    private static final int LAYOUT = R.layout.test_devdb_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, TestDevDbFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }
}
