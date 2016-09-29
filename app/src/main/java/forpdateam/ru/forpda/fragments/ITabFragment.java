package forpdateam.ru.forpda.fragments;

import io.reactivex.BackpressureStrategy;
import io.victoralbertos.rxlifecycle_interop.LifecycleTransformer2x;

/**
 * Created by radiationx on 07.08.16.
 */
public interface ITabFragment {
    String getDefaultTitle();

    String getTitle();

    String getTabUrl();

    String getParentTag();

    int getUID();

    void setUID();

    boolean isAlone();

    boolean onBackPressed();

    void hidePopupWindows();

    void loadData();

    <T> LifecycleTransformer2x<T> getLifeCycle(BackpressureStrategy strategy);
}
