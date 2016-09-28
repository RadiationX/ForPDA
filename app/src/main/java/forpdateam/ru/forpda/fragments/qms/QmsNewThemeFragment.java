package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.TabFragment;
import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.09.16.
 */

public class QmsNewThemeFragment extends TabFragment {
    public final static String defaultTitle = "Новый диалог";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";

    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;
    private AppCompatEditText messField;

    private String userId, userNick;

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID_ARG);
            userNick = getArguments().getString(USER_NICK_ARG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_qms_new_theme, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) findViewById(R.id.qms_theme_title_field);
        messField = (AppCompatEditText) findViewById(R.id.qms_theme_mess_field);
        if (userId != null || userNick != null) {
            nickField.setVisibility(View.GONE);
            setTitle(defaultTitle.concat(" с ").concat(userNick != null ? userNick : userId));
        } else {
            nickField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUser(s.toString());
                    setTitle(defaultTitle.concat(s.length() > 0 ? " с " : "").concat(s.toString()));
                    userNick = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        toolbar.getMenu().add("Отправить").setIcon(App.getAppDrawable(R.drawable.ic_send_white_24dp)).setOnMenuItemClickListener(menuItem -> {
            sendNewTheme();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return view;
    }

    private void sendNewTheme() {
        if (userNick == null || userNick.isEmpty()) {
            Toast.makeText(getContext(), "Введите ник пользователя", Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Введите название темы", Toast.LENGTH_SHORT).show();
        } else if (messField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
        } else {
            getCompositeDisposable().add(Api.Qms().sendNewTheme(userNick, titleField.getText().toString(), messField.getText().toString())
                    .onErrorReturn(throwable -> {
                        throwable.printStackTrace();
                        return "";
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.getLifeCycle(BackpressureStrategy.LATEST))
                    .subscribe(this::onCreateNewTheme, throwable -> {
                        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
        }
    }

    private void searchUser(String nick) {
        getCompositeDisposable().add(Api.Qms().search(nick)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new String[]{};
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onShowSearchRes, throwable -> {
                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void onShowSearchRes(String[] res) {
        nickField.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, res));
    }

    private void onCreateNewTheme(String res) {
        Matcher matcher = Pattern.compile("<div class=\"list-group-item msgbox error\">([^<]*<a[^>]*?>[^<]*?<[^>]*a>|)([\\s\\S]*?)</div>").matcher(res);
        if (matcher.find()) {
            Toast.makeText(getContext(), matcher.group(2), Toast.LENGTH_SHORT).show();
            return;
        }
        matcher = Pattern.compile("<form[^>]*?mid=(\\d*)&t=(\\d*)[^>]*>").matcher(res);
        if(matcher.find()){
            Toast.makeText(getContext(), "Диалог успешно создан", Toast.LENGTH_SHORT).show();
            Bundle args = new Bundle();
            args.putString(QmsChatFragment.USER_ID_ARG, matcher.group(1));
            args.putString(QmsChatFragment.THEME_ID_ARG, matcher.group(2));
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 500);
        }
    }
}
