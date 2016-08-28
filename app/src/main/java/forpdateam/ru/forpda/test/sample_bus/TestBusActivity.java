package forpdateam.ru.forpda.test.sample_bus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.rxbus.TestEvent;
import forpdateam.ru.forpda.utils.RxUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by isanechek on 28.08.16.
 */

public class TestBusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_bus);

        Button button = (Button) findViewById(R.id.bus_btn);
        /*Шлем эвент*/
        button.setOnClickListener(view -> App.getInstance()
                .bus()
                .send(new TestEvent.Message("Click msg time: " + System.currentTimeMillis())));

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bus_fragment_container, new TestBusFragment())
                    .commit();
        }
    }

    public static class TestBusFragment extends Fragment {

        private Subscription busSubscription;
        private TextView textView;
        public TestBusFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.test_bus_fragment, container, false);
            textView = (TextView) view.findViewById(R.id.test_click_bus_result);
            return view;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            /*Ловим эвент*/
            busSubscription = App.getInstance()
                    .bus()
                    .toObserverable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        if (o instanceof TestEvent.Message) {
                            textView.setText(((TestEvent.Message) o).message);
                        }
                    });

        }

        @Override
        public void onPause() {
            super.onPause();
            RxUtils.unsubscribe(busSubscription);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            RxUtils.unsubscribe(busSubscription);
        }
    }
}
