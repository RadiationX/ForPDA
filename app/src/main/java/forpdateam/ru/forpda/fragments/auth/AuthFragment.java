package forpdateam.ru.forpda.fragments.auth;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.auth.AuthParser;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.ourparser.Html;

/**
 * Created by radiationx on 29.07.16.
 */
public class AuthFragment extends TabFragment {
    private EditText nick, password, captcha;
    private ImageView captchaImage, avatar;
    private AuthForm authForm;
    private Button send;

    private LinearLayout mainForm;
    private RelativeLayout complete;
    private TextView completeText;
    private CircularProgressView progressView;
    private Subscriber<AuthForm> mainSubscriber = new Subscriber<>();
    private Subscriber<Boolean> loginSubscriber = new Subscriber<>();
    private Subscriber<ProfileModel> profileSubscriber = new Subscriber<>();

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
        baseInflateFragment(inflater, R.layout.activity_auth);
        nick = (EditText) findViewById(R.id.auth_login);
        password = (EditText) findViewById(R.id.auth_password);
        captcha = (EditText) findViewById(R.id.auth_captcha);
        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        avatar = (ImageView) findViewById(R.id.auth_avatar);
        mainForm = (LinearLayout) findViewById(R.id.auth_main_form);
        complete = (RelativeLayout) findViewById(R.id.auth_complete);
        completeText = (TextView) findViewById(R.id.auth_complete_text);
        progressView = (CircularProgressView) findViewById(R.id.auth_progress);
        viewsReady();
        view.findViewById(R.id.fragment_content).setBackgroundResource(R.color.colorPrimary);
        toolbar.setVisibility(View.GONE);
        send = (Button) findViewById(R.id.auth_send);
        send.setOnClickListener(view -> tryLogin());
        MyTW myTW = new MyTW();
        nick.addTextChangedListener(myTW);
        password.addTextChangedListener(myTW);
        captcha.addTextChangedListener(myTW);
        return view;
    }

    private class MyTW extends SimpleTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!nick.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && captcha.getText().toString().length() == 4) {
                if (!send.isEnabled())
                    send.setEnabled(true);
            } else {
                if (send.isEnabled())
                    send.setEnabled(false);
            }
        }
    }

    @Override
    public void loadData() {
        mainSubscriber.subscribe(Api.Auth().getForm(), this::onLoadForm, new AuthForm(), view1 -> loadData());
    }

    private void onLoadForm(AuthForm authForm) {
        if (authForm.getBody() == null) return;
        this.authForm = authForm;
        ImageLoader.getInstance().displayImage(authForm.getCaptchaImageUrl(), captchaImage);
    }

    private void tryLogin() {
        authForm.setCaptcha(captcha.getText().toString());
        authForm.setNick(nick.getText().toString());
        authForm.setPassword(password.getText().toString());
        loginSubscriber.subscribe(Api.Auth().tryLogin(authForm), this::showLoginResult, false, view1 -> loadData());
        //showLoginResult(false);
    }

    private void showLoginResult(boolean b) {
        if (b) {
            loadProfile();
        }
    }

    public void loadProfile() {

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(225);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainForm.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mainForm.startAnimation(animation);
        complete.setVisibility(View.VISIBLE);
        AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(375);
        complete.startAnimation(animation1);

        profileSubscriber.subscribe(Api.Profile().get("http://4pda.ru/forum/index.php?showuser=".concat(Api.Auth().getUserId())), this::onProfileLoad, new ProfileModel());
    }

    private void onProfileLoad(ProfileModel profile) {
        ImageLoader.getInstance().displayImage(profile.getAvatar(), avatar);
        completeText.setText(Html.fromHtml("Привет <b>" + profile.getNick() + "</b>!"));
        completeText.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        completeText.startAnimation(animation);
        AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation1.setDuration(225);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressView.setVisibility(View.GONE);
                progressView.stopAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progressView.startAnimation(animation1);
        new Handler().postDelayed(() -> {
            Api.Auth().doOnLogin();
            TabManager.getInstance().remove(getTag());
        }, 2500);
    }
}
