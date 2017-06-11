package forpdateam.ru.forpda.fragments.qms.chat;

import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.rx.Subscriber;

/**
 * Created by radiationx on 11.06.17.
 */

public class ChatThemeCreator {
    private QmsChatFragment fragment;
    private ViewStub viewStub;
    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;
    private MenuItem doneItem, editItem;
    private Subscriber<List<String>> searchUserSubscriber;
    private TextWatcher textWatcher = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if ((userId != 0 || userNick.length() > 0) && titleField.getText().length() > 0) {
                doneItem.setVisible(true);
            } else {
                doneItem.setVisible(false);
            }
        }
    };
    private int userId = -1;
    private String userNick, themeTitle;

    public ChatThemeCreator(QmsChatFragment fragment) {
        this.fragment = fragment;
        searchUserSubscriber = new Subscriber<>(this.fragment);
        viewStub = (ViewStub) this.fragment.findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_new_theme);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) this.fragment.findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) this.fragment.findViewById(R.id.qms_theme_title_field);
        this.userId = this.fragment.userId;
        this.userNick = this.fragment.userNick;
        this.themeTitle = this.fragment.themeTitle;
        initCreatorViews();
    }

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new ArrayList<>());
    }

    private void onShowSearchRes(List<String> res) {
        nickField.setAdapter(new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_dropdown_item_1line, res));
    }

    private void initCreatorViews() {
        titleField.addTextChangedListener(textWatcher);
        nickField.addTextChangedListener(textWatcher);
        editItem = fragment.getMenu().add("Изменить").setIcon(App.getAppDrawable(R.drawable.ic_create_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            viewStub.setVisibility(View.VISIBLE);
            doneItem.setVisible(true);
            editItem.setVisible(false);
            return false;
        });
        doneItem = fragment.getMenu().add("Ок").setIcon(App.getAppDrawable(R.drawable.ic_done_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            viewStub.setVisibility(View.GONE);
            editItem.setVisible(true);
            doneItem.setVisible(false);
            return false;
        });
        doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        editItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        editItem.setVisible(false);
        doneItem.setVisible(false);

        if (userNick != null) {
            nickField.setVisibility(View.GONE);
        } else {
            nickField.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    userNick = s.toString();
                    searchUser(userNick);
                    fragment.setSubtitle(userNick);
                }
            });
        }
        titleField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                themeTitle = s.toString();
                fragment.setTitle(themeTitle);
            }
        });

    }

    public void sendNewTheme() {
        if (userNick == null || userNick.isEmpty()) {
            Toast.makeText(fragment.getContext(), "Введите ник пользователя", Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(fragment.getContext(), "Введите название темы", Toast.LENGTH_SHORT).show();
        } else if (fragment.getMessagePanel().getMessage().isEmpty()) {
            Toast.makeText(fragment.getContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
        } else {
            this.fragment.onCreateNewTheme(userNick, titleField.getText().toString(), fragment.getMessagePanel().getMessage());
        }
    }

    public void onNewThemeCreate() {
        viewStub.setVisibility(View.GONE);
        editItem.setVisible(false);
        doneItem.setVisible(false);
    }

    public interface ThemeCreatorInterface {
        void onCreateNewTheme(String nick, String title, String message);
    }
}
