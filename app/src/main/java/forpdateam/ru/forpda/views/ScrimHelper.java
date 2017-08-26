package forpdateam.ru.forpda.views;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.Log;
import android.view.View;

/**
 * Created by radiationx on 26.08.17.
 */

public class ScrimHelper {
    private ScrimListener scrimListener;
    private boolean scrim = false;

    public ScrimHelper(AppBarLayout appBarLayout, CollapsingToolbarLayout toolbarLayout) {
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (scrimListener == null) return;
            if (appBarLayout1.getHeight() + verticalOffset <= toolbarLayout.getScrimVisibleHeightTrigger()) {
                if (!scrim) {
                    scrim = true;
                    scrimListener.onScrimChanged(true);
                }
            } else {
                if (scrim) {
                    scrim = false;
                    scrimListener.onScrimChanged(false);
                }
            }
        });
    }

    public void setScrimListener(ScrimListener scrimListener) {
        this.scrimListener = scrimListener;
    }

    public interface ScrimListener {
        void onScrimChanged(boolean scrim);
    }
}
