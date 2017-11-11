package forpdateam.ru.forpda.common.mvp;

import android.os.Bundle;

/**
 * Created by radiationx on 05.11.17.
 */

public interface IBasePresenter<ViewT> {
    void onCreate(Bundle savedInstanceState);

    void onAttach();

    void onSaveInstanceState(Bundle outState);

    void onDetach();

    void onDestroyView();

    void onDestroy();
}
