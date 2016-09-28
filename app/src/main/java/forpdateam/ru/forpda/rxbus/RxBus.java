package forpdateam.ru.forpda.rxbus;

/**
 * Created by isanechek on 28.08.16.
 */

public class RxBus {

    public RxBus() {
    }


    /*Позже верну*/
//    private final Subject<Object> _bus = new SerialDisposable<>(PublishSubject.create());
//
//    public void send(Object o) {
//        _bus.onNext(o);
//    }
//
//    public Observable<Object> toObserverable() {
//        return _bus;
//    }
//
//    public boolean hasObservers() {
//        return _bus.hasObservers();
//    }
}
