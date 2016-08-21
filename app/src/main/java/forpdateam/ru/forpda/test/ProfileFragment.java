package forpdateam.ru.forpda.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import at.favre.lib.dali.Dali;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileFragment extends TabFragment {
    private static final String LINk = "http://4pda.ru/forum/index.php?showuser=2556269#";
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private LayoutInflater inflater;

    private TextView nick, group, sign;
    private ImageView avatar;
    private LinearLayout countList, infoBlock, contactList;
    private EditText noteText;
    private CollapsingToolbarLayout collapsingToolbarLayout;


    class CountItem extends LinearLayout {
        public CountItem(Context context) {
            super(context);
            inflate(context, R.layout.profile_counts_list_item, this);
        }

        public void setTitle(String title) {
            ((TextView) findViewById(R.id.item_title)).setText(title);
        }

        public void setDesc(String desc) {
            ((TextView) findViewById(R.id.item_desc)).setText(desc);
        }
    }

    class InfoItem extends LinearLayout {
        public InfoItem(Context context) {
            super(context);
            inflate(context, R.layout.profile_info_list_item, this);
        }

        public void setTitle(String title) {
            ((TextView) findViewById(R.id.item_title)).setText(title);
        }

        public void setDesc(String desc) {
            ((TextView) findViewById(R.id.item_desc)).setText(desc);
        }
    }

    class ContactItem extends LinearLayout {
        public ContactItem(Context context) {
            super(context);
            inflate(context, R.layout.profile_contact_list_item, this);
        }

        public void setIcon(Drawable icon) {
            ((ImageView) findViewById(R.id.icon)).setImageDrawable(icon);
        }
    }

    @Override
    public String getTabUrl() {
        return LINk;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        initBaseView(inflater, container);
        inflater.inflate(R.layout.fragment_profile, (ViewGroup) view.findViewById(R.id.fragment_content), true);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.profile_toolbar);
        viewStub.inflate();
        nick = (TextView) findViewById(R.id.profile_nick);
        group = (TextView) findViewById(R.id.profile_group);
        sign = (TextView) findViewById(R.id.profile_sign);
        avatar = (ImageView) findViewById(R.id.profile_avatar);
        countList = (LinearLayout) findViewById(R.id.profile_count_list);
        infoBlock = (LinearLayout) findViewById(R.id.profile_block_information);
        contactList = (LinearLayout) findViewById(R.id.profile_contact_list);

        fab.setImageDrawable(AppCompatResources.getDrawable(App.getContext(), R.drawable.contact_qms));
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setTitleEnabled(true);
        /*collapsingToolbarLayout.setExpandedTitleGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleMarginTop(dpToPx(216));
        collapsingToolbarLayout.setScrimVisibleHeightTrigger(dpToPx(144));
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.QText);
        collapsingToolbarLayout.setScrimAnimationDuration(225);
*/
        noteText = (EditText) findViewById(R.id.profile_note_text);
         findViewById(R.id.profile_save_note).setOnClickListener(view1 -> Toast.makeText(getContext(), "save : "+noteText.getText().toString(), Toast.LENGTH_SHORT).show());
        //toolbar.setTitleTextColor(Color.TRANSPARENT);


        setHasOptionsMenu(true);
        return view;
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }
    @Override
    public void onResume() {
        super.onResume();
        setTitle(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("kek", "oncreate menu");
    }

    /*@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d("kek", "onprepare menu");
        menu.clear();
        menu.add("HYZ");
        menu.add("PIZZA");
    }*/


    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d("kek", "onclose menu");
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.d("kek", "ondestroy menu");
    }

    @Override
    public void loadData() {
        compositeSubscription.add(
                Api.Profile().getRx(LINk)
                        .onErrorReturn(throwable -> {
                            ErrorHandler.handle(this, throwable, view1 -> loadData());
                            return new ProfileModel();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::bindUi)
        );
    }

    private void addCountItem(String title, Pair<String, String> data) {
        CountItem countItem = new CountItem(getContext());
        countItem.setTitle(title);
        countItem.setDesc(data.second);
        countItem.setOnClickListener(view1 -> Toast.makeText(getContext(), data.first + "", Toast.LENGTH_SHORT).show());
        countList.addView(countItem);
    }

    private void addInfoItem(String title, String data) {
        InfoItem infoItem = new InfoItem(getContext());
        infoItem.setTitle(title);
        infoItem.setDesc(data);
        infoBlock.addView(infoItem);
    }

    private void addContactItem(Drawable icon, String data) {
        ContactItem contactItem = new ContactItem(getContext());
        contactItem.setIcon(icon);
        contactItem.setOnClickListener(view1 -> Toast.makeText(getContext(), data + "", Toast.LENGTH_SHORT).show());
        contactList.addView(contactItem);
    }

    private int getIconRes(String name) {
        switch (name) {
            case "Вебсайт":
                return R.drawable.contact_site;
            case "ICQ":
                return R.drawable.contact_icq;
            case "Twitter":
                return R.drawable.contact_twitter;
            case "Jabber":
                return R.drawable.contact_jabber;
            case "Вконтакте":
                return R.drawable.contact_vk;
            case "Google+":
                return R.drawable.contact_google_plus;
            case "Facebook":
                return R.drawable.contact_facebook;
            case "Instagram":
                return R.drawable.contact_instagram;
            case "Mail.ru":
                return R.drawable.contact_mail_ru;
            default:
                return R.drawable.contact_site;
        }
    }


    private void bindUi(ProfileModel profile) {
        ImageLoader.getInstance().displayImage(profile.getAvatar(), avatar);
        ImageLoader.getInstance().displayImage(profile.getAvatar(), toolbarBackground, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Dali.create(getContext()).load(loadedImage).noFade().colorFilter(Color.parseColor("#bbbbbb")).concurrent().copyBitmapBeforeProcess().into(toolbarBackground);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

        setTitle(profile.getNick());
        nick.setText(profile.getNick());
        group.setText(profile.getGroup());
        sign.setText(Html.fromHtml(profile.getSign()));
        sign.setMovementMethod(new LinkMovementMethod());
        addCountItem("Постов", profile.getPosts());
        addCountItem("Тем", profile.getTopics());
        addCountItem("Репутация", profile.getReputation());
        addCountItem("Карма", profile.getKarma());
        addCountItem("Новостей", profile.getSitePosts());
        addCountItem("Комментариев", profile.getComments());

        if (profile.getGender() != null)
            addInfoItem("Пол", profile.getGender());
        if (profile.getBirthDay() != null)
            addInfoItem("Дата рождения", profile.getBirthDay());
        if (profile.getCity() != null)
            addInfoItem("Город", profile.getCity());
        if (profile.getUserTime() != null)
            addInfoItem("Время у юзера", profile.getUserTime());
        inflater.inflate(R.layout.profile_divider, infoBlock);
        if (profile.getRegDate() != null)
            addInfoItem("Регистрация", profile.getRegDate());
        if (profile.getOnlineDate() != null)
            addInfoItem("Последнее посещение", profile.getOnlineDate());
        if (profile.getAlerts() != null)
            addInfoItem("Предупреждения", profile.getAlerts());

        if(profile.getContacts().size()>0){
            for (Pair<String, String> contact : profile.getContacts()) {
                if (contact.second.equals("QMS")) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(view1 -> {
                        Toast.makeText(getContext(), "qms : " + contact.first, Toast.LENGTH_SHORT).show();
                    });
                    continue;
                }
                addContactItem(AppCompatResources.getDrawable(App.getContext(), getIconRes(contact.second)), contact.first);
            }
        }else {
            findViewById(R.id.profile_block_contacts).setVisibility(View.GONE);
        }

        if(profile.getNote()!=null){
            findViewById(R.id.profile_block_note).setVisibility(View.VISIBLE);
            noteText.setText(profile.getNote());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }
}
