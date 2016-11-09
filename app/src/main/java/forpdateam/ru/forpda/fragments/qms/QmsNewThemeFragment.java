package forpdateam.ru.forpda.fragments.qms;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.TabFragment;

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
    private CardView messagePanel;
    private ViewStub viewStub;
    private MenuItem sendItem, doneItem, editItem;

    private String userId, userNick;

    private Subscriber<String> newThemeSubscriber = new Subscriber<>();
    private Subscriber<String[]> searchUserSubscriber = new Subscriber<>();

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
        baseInflateFragment(inflater, R.layout.fragment_qms_new_theme);
        viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.qms_new_theme_toolbar);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) findViewById(R.id.qms_theme_title_field);
        messField = (AppCompatEditText) findViewById(R.id.qms_theme_mess_field);
        messagePanel = (CardView) findViewById(R.id.qms_message_panel);
        toolbarTitleView.setVisibility(View.GONE);
        titleField.addTextChangedListener(textWatcher);
        messField.addTextChangedListener(textWatcher);
        if (userId != null || userNick != null) {
            nickField.setVisibility(View.GONE);
            ((View) nickField.getParent()).setVisibility(View.GONE);
        } else {
            nickField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUser(s.toString());
                    if (userId == null)
                        userNick = nickField.getText().toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        editItem = toolbar.getMenu().add("Изменить").setIcon(App.getAppDrawable(R.drawable.ic_create_white_24dp)).setOnMenuItemClickListener(menuItem -> {
            hideMessagePanel();
            return false;
        });
        doneItem = toolbar.getMenu().add("Ок").setIcon(App.getAppDrawable(R.drawable.ic_done_white_24dp)).setOnMenuItemClickListener(menuItem -> {
            showMessagePanel();
            return false;
        });
        sendItem = toolbar.getMenu().add("Отправить").setIcon(App.getAppDrawable(R.drawable.ic_send_white_24dp)).setOnMenuItemClickListener(menuItem -> {
            sendNewTheme();
            return false;
        });
        doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        editItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        editItem.setVisible(false);
        sendItem.setVisible(false);

        return view;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if ((userId != null || userNick.length() > 0) && titleField.getText().length() > 0 && messField.getText().length() > 0) {
                sendItem.setVisible(true);
            } else {
                sendItem.setVisible(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void showMessagePanel() {
        messagePanel.setVisibility(View.VISIBLE);
        viewStub.setVisibility(View.GONE);
        editItem.setVisible(true);
        doneItem.setVisible(false);
        toolbarSubitleView.setVisibility(View.VISIBLE);
        toolbarTitleView.setVisibility(View.VISIBLE);
        setTitle(titleField.getText().toString());
        setSubtitle(userNick != null ? userNick.length() > 0 ? userNick : null : null);
    }

    private void hideMessagePanel() {
        messagePanel.setVisibility(View.GONE);
        viewStub.setVisibility(View.VISIBLE);
        doneItem.setVisible(true);
        editItem.setVisible(false);
        toolbarTitleView.setVisibility(View.GONE);
        toolbarSubitleView.setVisibility(View.GONE);
    }

    private void sendNewTheme() {
        if (userNick == null || userNick.isEmpty()) {
            Toast.makeText(getContext(), "Введите ник пользователя", Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Введите название темы", Toast.LENGTH_SHORT).show();
        } else if (messField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
        } else {
            newThemeSubscriber.subscribe(Api.Qms().sendNewTheme(userNick, titleField.getText().toString(), messField.getText().toString()), this::onCreateNewTheme, "");
        }
    }

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(Api.Qms().search(nick), this::onShowSearchRes, new String[]{});
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
        if (matcher.find()) {
            Toast.makeText(getContext(), "Диалог успешно создан", Toast.LENGTH_SHORT).show();
            Bundle args = new Bundle();
            args.putString(QmsChatFragment.USER_ID_ARG, matcher.group(1));
            args.putString(QmsChatFragment.THEME_ID_ARG, matcher.group(2));
            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
            new Handler().postDelayed(() -> TabManager.getInstance().remove(getTag()), 500);
        }
    }
}
