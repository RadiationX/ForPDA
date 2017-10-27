package forpdateam.ru.forpda;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.acra.dialog.CrashReportDialog;

/**
 * Created by radiationx on 27.10.17.
 */

public class CustomCrashDialog extends CrashReportDialog {
    @NonNull
    @Override
    protected View buildCustomView(@Nullable Bundle savedInstanceState) {
        View view = super.buildCustomView(savedInstanceState);
        view.setPadding(App.px24, App.px8, App.px24, 0);
        return view;
    }
}
