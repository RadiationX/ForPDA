package forpdateam.ru.forpda.ui.fragments.other;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.common.webview.CustomWebViewClient;
import forpdateam.ru.forpda.ui.activities.MainActivity;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.views.ExtendedWebView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 09.11.17.
 */

public class GoogleCaptchaFragment extends TabFragment {
    private ExtendedWebView webView;
    private String content = "";

    public GoogleCaptchaFragment() {
        configuration.setDefaultTitle("Проверка");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            content = getArguments().getString("content", "1");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        fragmentContent.addView(webView);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        setSubtitle("Это из-за VPN/Proxy и т.д.");
        webView.setWebViewClient(new CaptchaWebViewClient());
    }

    class CaptchaWebViewClient extends CustomWebViewClient {
        @Override
        public boolean handleUri(Uri uri) {
            Log.e("SUKA", uri.toString());
            if (Pattern.compile("https://4pda.ru/cdn-cgi/l/chk_captcha").matcher(uri.toString()).find()) {
                NetworkRequest nr = new NetworkRequest.Builder().url(uri.toString()).withoutBody().build();
                Observable.fromCallable(() -> Client.get().request(nr))
                        .onErrorReturn(throwable -> new NetworkResponse(null))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(GoogleCaptchaFragment.this::onResponse);
            }
            return true;
        }
    }

    @Override
    public boolean loadData() {
        super.loadData();
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", content, "text/html", "utf-8", null);
        return true;
    }

    private void onResponse(NetworkResponse response) {
        Toast.makeText(App.getContext(), "Приложение будет перезапущено", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> {
            Activity activity = App.getActivity();
            if (activity == null) {
                Toast.makeText(App.getContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show();
            }
            MainActivity.restartApplication(activity);
        }, 1000);
    }
}
