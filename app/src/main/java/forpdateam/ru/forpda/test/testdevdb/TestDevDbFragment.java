package forpdateam.ru.forpda.test.testdevdb;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.ArrayList;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.mvp.MvpFragment;

/**
 * Created by isanechek on 04.08.16.
 */

public class TestDevDbFragment extends MvpFragment<TestDevDbPresenter> implements DevDbMvpView {
    private static final String TAG = "TestDevDbFragment";
    private static final int LAYOUT = R.layout.test_dev_db_fragment;
    private static final String LINk = "http://4pda.ru/devdb/phones/";

    public static TestDevDbFragment newInstance() {
        return new TestDevDbFragment();
    }

    public TestDevDbFragment() {
    }

    private Button startBtn;
    private ProgressBar progressBar;
    private LinearLayout containerLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        startBtn = (Button) view.findViewById(R.id.start_btn);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_devdb);
        startBtn.setOnClickListener(view1 -> presenter.loadData(LINk));
        containerLayout = (LinearLayout) view.findViewById(R.id.container);
        return view;
    }

    @NonNull
    @Override
    protected TestDevDbPresenter getPresenter() {
        return new TestDevDbPresenter();
    }

    @Override
    public void onError(int errorCode, String errorText) {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        Log.e(TAG, "onError: " + errorText);
    }

    @Override
    public void setProgress(boolean visible) {
        if (visible) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void loadData(ArrayList<DevCatalog> brands) {
        Stream.of(brands).forEach(brand -> {
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(8, 8, 8, 8);
            textView.setTextSize(16f);
            textView.setText(brand.getTitle());
            containerLayout.addView(textView);
        });
    }
}
