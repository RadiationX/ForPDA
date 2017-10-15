package forpdateam.ru.forpda.fragments.auth;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.news.main.NewsMainFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.EmptyActivity;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.drawers.Drawers;

/**
 * Created by radiationx on 29.07.16.
 */
public class AuthFragment extends TabFragment {
    private EditText nick, password, captcha;
    private ImageView captchaImage, avatar;
    private AuthForm authForm;
    private Button sendButton;
    private Button skipButton;
    private ProgressBar loginProgress, captchaProgress;
    private CheckBox hiddenAuth;

    private LinearLayout mainForm;
    private RelativeLayout complete;
    private TextView completeText;
    private CircularProgressView progressView;
    private Subscriber<AuthForm> mainSubscriber = new Subscriber<>(this);
    private Subscriber<Boolean> loginSubscriber = new Subscriber<>(this);
    private Subscriber<ProfileModel> profileSubscriber = new Subscriber<>(this);

    public AuthFragment() {
        configuration.setAlone(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_auth);
        nick = (EditText) findViewById(R.id.auth_login);
        password = (EditText) findViewById(R.id.auth_password);
        captcha = (EditText) findViewById(R.id.auth_captcha);
        captchaImage = (ImageView) findViewById(R.id.captchaImage);
        captchaProgress = (ProgressBar) findViewById(R.id.captcha_progress);
        avatar = (ImageView) findViewById(R.id.auth_avatar);
        mainForm = (LinearLayout) findViewById(R.id.auth_main_form);
        complete = (RelativeLayout) findViewById(R.id.auth_complete);
        completeText = (TextView) findViewById(R.id.auth_complete_text);
        progressView = (CircularProgressView) findViewById(R.id.auth_progress);
        loginProgress = (ProgressBar) findViewById(R.id.login_progress);
        hiddenAuth = (CheckBox) findViewById(R.id.auth_hidden);
        sendButton = (Button) findViewById(R.id.auth_send);
        skipButton = (Button) findViewById(R.id.auth_skip);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewsReady();
        setListsBackground();
        skipButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage("Без авторизации будут недоступны некоторые функции приложения.")
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        Drawers drawers = getMainActivity().getDrawers();
                        drawers.selectMenuItem(NewsMainFragment.class);
                        TabManager.getInstance().remove(AuthFragment.this);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        appBarLayout.setVisibility(View.GONE);
        notifyDot.setVisibility(View.GONE);
        sendButton.setOnClickListener(v -> tryLogin());
        LoginTextWatcher loginTextWatcher = new LoginTextWatcher();
        nick.addTextChangedListener(loginTextWatcher);
        password.addTextChangedListener(loginTextWatcher);
        captcha.addTextChangedListener(loginTextWatcher);
        fragmentContainer.setFitsSystemWindows(true);
        fragmentContent.setFitsSystemWindows(true);
    }


    @Override
    protected void updateNotifyDot() {
        //Чтобы не показывалась точка.
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        mainSubscriber.subscribe(RxApi.Auth().getForm(), this::onLoadForm, new AuthForm(), view1 -> loadData());
        return true;
    }

    private void onLoadForm(AuthForm authForm) {
        if (authForm.getBody() == null) return;
        this.authForm = authForm;
        captchaProgress.setVisibility(View.VISIBLE);
        captchaImage.setVisibility(View.GONE);
        ImageLoader.getInstance().displayImage(authForm.getCaptchaImageUrl(), captchaImage, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                captchaProgress.setVisibility(View.GONE);
                captchaImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void tryLogin() {
        if (authForm == null) return;
        authForm.setCaptcha(captcha.getText().toString());
        authForm.setNick(nick.getText().toString());
        authForm.setPassword(password.getText().toString());
        authForm.setHidden(hiddenAuth.isChecked());
        loginProgress.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.INVISIBLE);
        hidePopupWindows();
        loginSubscriber.subscribe(RxApi.Auth().login(authForm), this::showLoginResult, false, view1 -> loadData());
        //showLoginResult(false);
    }

    private void showLoginResult(boolean b) {
        Log.d(AuthFragment.class.getSimpleName(), "showLoginResult " + b);
        if (b) {
            loadProfile();
        } else {
            loadData();
            captcha.setText("");
            password.setText("");
        }
        loginProgress.setVisibility(View.GONE);
        sendButton.setVisibility(View.VISIBLE);
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

        profileSubscriber.subscribe(RxApi.Profile().getProfile("https://4pda.ru/forum/index.php?showuser=".concat(Integer.toString(ClientHelper.getUserId()))), this::onProfileLoad, new ProfileModel());
    }

    private void onProfileLoad(ProfileModel profile) {
        if (EmptyActivity.empty(profile.getNick())) {
            loginProgress.setVisibility(View.GONE);
            // Надо записать иначе будет каждый раз просить авторизацию
            App.get().getPreferences().edit().putString("auth.user.nick", profile.getNick()).apply();
            EmptyActivity.start(getActivity(), profile.getNick());
            getMainActivity().finish();
            return;
        }
        App.get().getPreferences().edit().putString("auth.user.nick", profile.getNick()).apply();
        ImageLoader.getInstance().displayImage(profile.getAvatar(), avatar);
        completeText.setText(Utils.spannedFromHtml(String.format("%s, <b>%s</b>!", getString(R.string.auth_hello), profile.getNick())));
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
            ClientHelper.get().notifyAuthChanged(ClientHelper.AUTH_STATE_LOGIN);
            new Handler().postDelayed(() -> {
                TabManager.getInstance().remove(AuthFragment.this);
            }, 500);
        }, 2000);
    }

    private class LoginTextWatcher extends SimpleTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!nick.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && captcha.getText().toString().length() == 4) {
                if (!sendButton.isEnabled())
                    sendButton.setEnabled(true);
            } else {
                if (sendButton.isEnabled())
                    sendButton.setEnabled(false);
            }
        }
    }
}
