package forpdateam.ru.forpda.utils;

import java.util.PriorityQueue;

/**
 * Created by radiationx on 20.10.16.
 */

public class WebViewQueue<E> extends PriorityQueue<E> {
    public WebViewQueue() {
        super(10);
    }

    public E get(){
        return element();
    }
}
