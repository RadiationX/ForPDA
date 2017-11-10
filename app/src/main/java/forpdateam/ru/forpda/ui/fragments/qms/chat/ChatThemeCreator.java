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
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.common.rx.Subscriber;
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher;

/**
 * Created by radiationx on 11.06.17.
 */

public class ChatThemeCreator {
    private QmsChatFragment fragment;
    private ViewStub viewStub;
    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;
    private MenuItem doneItem, editItem;
    private Subscriber<List<ForumUser>> searchUserSubscriber;
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
        this.userId = this.fragment.currentChat.getUserId();
        this.userNick = this.fragment.currentChat.getNick();
        this.themeTitle = this.fragment.currentChat.getTitle();
        initCreatorViews();
    }

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new ArrayList<>());
    }

    private void onShowSearchRes(List<ForumUser> res) {
        List<String> nicks = new ArrayList<>();
        for (ForumUser user : res) {
            nicks.add(user.getNick());
        }
        nickField.setAdapter(new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_dropdown_item_1line, nicks));
    }

    private void initCreatorViews() {
        titleField.addTextChangedListener(textWatcher);
        nickField.addTextChangedListener(textWatcher);
        editItem = fragment.getMenu().add(R.string.change)
                .setIcon(App.getVecDrawable(fragment.getContext(), R.drawable.ic_fab_create))
                .setOnMenuItemClickListener(menuItem -> {
                    viewStub.setVisibility(View.VISIBLE);
                    doneItem.setVisible(true);
                    editItem.setVisible(false);
                    return false;
                });
        doneItem = fragment.getMenu().add(R.string.ok)
                .setIcon(App.getVecDrawable(fragment.getContext(), R.drawable.ic_toolbar_done))
                .setOnMenuItemClickListener(menuItem -> {
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
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_nick, Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_title, Toast.LENGTH_SHORT).show();
        } else if (fragment.getMessagePanel().getMessage().isEmpty()) {
            Toast.makeText(fragment.getContext(), R.string.chat_creator_enter_message, Toast.LENGTH_SHORT).show();
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
