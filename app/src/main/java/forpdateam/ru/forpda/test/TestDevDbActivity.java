package forpdateam.ru.forpda.test;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.annimon.stream.Stream;
import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.ApiImpl;
import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.utils.permission.RxPermissions;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by isanechek on 30.07.16.
 */

/**
 * Не стал дальше писать на всякий. Вдруг не зайдет.))
 */

public class TestDevDbActivity extends RxAppCompatActivity {
    private static final int LAYOUT = R.layout.test_devdb_activity;
    private static final String LINk = "http://4pda.ru/devdb/phones/";
    private ApiImpl api;
    private RxPermissions permissions;
    private Subscription subscription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        api = ApiImpl.getInstance();
        permissions = RxPermissions.getInstance(this);

        /**
         * Тут опять благодоря Jake Wharton мы можем из простой обработки клика намутить целую цепочку событий
         */

        Button button = (Button) findViewById(R.id.dev_db_test_start);

        RxView.clicks(button)
                .compose(permissions.ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        loadData();
                    }
                });

    }

    private void loadData() {
        subscription = api.getBrands(Client.getInstance(), LINk)
                /**
                 * Оператор subscribeOn - в нем мы говорим что Observable(Источник данных)
                 * будет работать в другом потоке. Для этого мы используем Schedulers.io().
                 * Бывают и другие но для операций ввода - вывода используют IO.
                 * Он же и используется для похода в сеть.
                 * Можно еще самому намутить тред(ы) и передать туда.
                 */
                .subscribeOn(Schedulers.io())
                /**
                 * Оператор observeOn - тут мы указываем в каком потоке будет работать subscribe.
                 * В котором мы будем работать с полученными данными из Observable.
                 * AndroidSchedulers.mainThread() из библиотеки rxandroid.
                 * Тут думаю все понятно.
                 *
                 * Обрати внимания. Логично было бы если в subscribeOn мы указывали поток subscribe,
                 * но какой то мучачес решил по другому. Вообщем нужно первое время быть внимательнее.
                 * А то завалится.
                 */
                .observeOn(AndroidSchedulers.mainThread()) // Главное не путать
                /**
                 * Оператор compose - типа трансформация. Глянь лучше доки.
                 * В наше случае мы отслеживаем состояние активити.
                 * Чтоб если активити когда ушла на паузу мы не пытались рисовать вьюхи
                 */
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                /**
                 * Оператор subscribe - это подписчик.
                 * Вот так он выглядит без сахара 8ки и чуть больше.))
                 */
//                .subscribe(new Action1<ArrayList<DevCatalog>>() {
//                    @Override
//                    public void call(ArrayList<DevCatalog> catalogs) {
//                        /**
//                         * Тут все понятно.
//                         */
//                        bindUi(catalogs);
//
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        /**
//                         * Тут муть шо нибудь если из Observable ничего не пришло
//                         */
//                    }
//                }, new Action0() {
//                    @Override
//                    public void call() {
//                        /**
//                         * Тут мутим действие которое будет после ошибки.
//                         */
//                    }
//                });
                .subscribe(this::bindUi);
    }

    /**
     * Короче, как то так, операторов не так много. Можно писать свои.
     * Еще короче, можно намутить неограниченое количество операторов в цепочке.
     * Что позволяет нам с данными творить что хочешь. Начиная от фильтрации заканчивая мапингом.
     * То есть можно на вход подать стринг, а на выходе получить лист с обьектами.
     */

    private void bindUi(ArrayList<DevCatalog> catalog) {
        /*Java 8 сахар*/
        Stream.of(catalog).forEach(value -> logD(value.getTitle().toString()));
    }

    private void logD(String text) {
        Log.d("Test DevDb Activity", text);
    }

}
