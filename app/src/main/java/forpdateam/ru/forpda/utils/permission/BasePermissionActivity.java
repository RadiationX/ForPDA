package forpdateam.ru.forpda.utils.permission;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;

/**
 * Created by isanechek on 11/19/16.
 */

public abstract class BasePermissionActivity extends Activity {
    private static final String KEY_ORIGINAL_PID = "key_original_pid";
    private int mOriginalProcessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mOriginalProcessId = Process.myPid();
        } else {
            mOriginalProcessId = savedInstanceState.getInt(KEY_ORIGINAL_PID, mOriginalProcessId);
            boolean restoredInAnotherProcess = mOriginalProcessId != Process.myPid();
            if (restoredInAnotherProcess) {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ORIGINAL_PID, mOriginalProcessId);
    }
}
