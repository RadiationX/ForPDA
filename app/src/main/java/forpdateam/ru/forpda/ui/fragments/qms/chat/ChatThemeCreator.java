package forpdateam.ru.forpda.ui.fragments.qms.chat;

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
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser;
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatPresenter;

/**
 * Created by radiationx on 11.06.17.
 */

public class ChatThemeCreator {
    private QmsChatFragment fragment;
    private QmsChatPresenter presenter;
    private ViewStub viewStub;
    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;

    private String userNick, themeTitle;

    ChatThemeCreator(QmsChatFragment fragment, QmsChatPresenter presenter) {
        this.fragment = fragment;
        this.presenter = presenter;
        viewStub = (ViewStub) this.fragment.findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_new_theme);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) this.fragment.findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) this.fragment.findViewById(R.id.qms_theme_title_field);
        this.userNick = this.presenter.getNick();
        this.themeTitle = this.presenter.getTitle();
        initCreatorViews();
    }

    private void searchUser(String nick) {
        presenter.findUser(nick);
    }

    void onShowSearchRes(List<? extends ForumUser> res) {
        List<String> nicks = new ArrayList<>();
        for (ForumUser user : res) {
            nicks.add(user.getNick());
        }
        nickField.setAdapter(new ArrayAdapter<>(nickField.getContext(), android.R.layout.simple_dropdown_item_1line, nicks));
    }

    private void initCreatorViews() {
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

    void sendNewTheme() {
        if (userNick == null || userNick.isEmpty()) {
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_nick, Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_title, Toast.LENGTH_SHORT).show();
        } else if (fragment.getMessagePanel().getMessage().isEmpty()) {
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_message, Toast.LENGTH_SHORT).show();
        } else {
            this.fragment.onCreateNewTheme(userNick, titleField.getText().toString(), fragment.getMessagePanel().getMessage());
        }
    }

    public void setVisible(boolean isVisible) {
        viewStub.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        //editItem.setVisible(isVisible);
        //doneItem.setVisible(isVisible);
    }

    public interface ThemeCreatorInterface {
        void onCreateNewTheme(String nick, String title, String message);
    }
}
