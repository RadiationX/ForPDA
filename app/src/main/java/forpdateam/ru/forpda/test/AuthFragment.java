package forpdateam.ru.forpda.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.auth.AuthParser;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.fragments.TabFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by radiationx on 29.07.16.
 */
public class AuthFragment extends TabFragment{
    private CompositeDisposable mCompositeSubscription = new CompositeDisposable();
    private Throwable throwable = null;
    private EditText nick, password, captcha;
    private ImageView captchaImage;
    private AuthForm authForm;
    private Button send;

    @Override
    public String getTabUrl() {
        return AuthParser.authFormUrl;
    }

    @Override
    public boolean isAlone() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.activity_auth, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        nick = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
        captcha = (EditText) findViewById(R.id.editText3);
        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        viewsReady();
        send = (Button) findViewById(R.id.button2);
        send.setOnClickListener(view -> tryLogin());
        return view;
    }

    @Override
    public void loadData() {
        mCompositeSubscription.add(Api.Auth().getForm()
                .onErrorReturn(throwable -> {
                    this.throwable = throwable;
                    throwable.printStackTrace();
                    return new AuthForm();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::bindUi, throwable -> Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void bindUi(AuthForm authForm) {
        this.authForm = authForm;
        if (throwable != null) {
            new AlertDialog.Builder(getContext())
                    .setMessage(throwable.getMessage())
                    .setPositiveButton("Ok", null)
                    .show();
        } else {
            ImageLoader.getInstance().displayImage(authForm.getCaptchaImageUrl(), captchaImage);
        }
    }

    private void tryLogin() {
        authForm.setCaptcha(captcha.getText().toString());
        authForm.setNick(nick.getText().toString());
        authForm.setPassword(password.getText().toString());
        mCompositeSubscription.add(Api.Auth().tryLogin(authForm)
                .onErrorReturn(throwable -> {
                    this.throwable = throwable;
                    throwable.printStackTrace();
                    return false;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showLoginResult, throwable -> Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void showLoginResult(boolean b) {
        if (b) {
            Toast.makeText(getContext(), "AuthParser Complete", Toast.LENGTH_SHORT).show();
            //new Handler().postDelayed(this::finish, 500);
            Api.Auth().doOnLogin();
        } else {
            if (throwable != null) {
                new AlertDialog.Builder(getContext())
                        .setMessage(throwable.getMessage())
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            loadData();
                        })
                        .show();
            }
        }
        throwable = null;
    }
}
