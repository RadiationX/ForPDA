package forpdateam.ru.forpda.utils;

import java.util.Observable;

/**
 * Created by radiationx on 28.05.17.
 */

public class SimpleObservable extends Observable {
    @Override
    public synchronized boolean hasChanged() {
        return true;
    }
}
