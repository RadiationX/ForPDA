package forpdateam.ru.forpda.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.menu.MenuBuilder;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.utils.ErrorHandler;
import forpdateam.ru.forpda.utils.FastBlur;
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

    private TextView nick, group, sign, about;
    private ImageView avatar;
    private LinearLayout countList, infoBlock, contactList, devicesList;
    private EditText noteText;
    private CircularProgressView progressView;
    private int profileId = 0;


    public ProfileFragment() {
        setTabUrl(LINk);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Matcher matcher = Pattern.compile("showuser=(\\d*)").matcher(getTabUrl());
        if(matcher.find())
            profileId = Integer.parseInt(matcher.group(1));
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
        about = (TextView) findViewById(R.id.profile_about_text);
        avatar = (ImageView) findViewById(R.id.profile_avatar);
        countList = (LinearLayout) findViewById(R.id.profile_list_counts);
        infoBlock = (LinearLayout) findViewById(R.id.profile_list_information);
        contactList = (LinearLayout) findViewById(R.id.profile_list_contacts);
        devicesList = (LinearLayout) findViewById(R.id.profile_list_devices);
        progressView = (CircularProgressView) findViewById(R.id.profile_progress);

        fab.setImageDrawable(AppCompatResources.getDrawable(App.getContext(), R.drawable.contact_qms));
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
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
        findViewById(R.id.profile_save_note).setOnClickListener(view1 -> Toast.makeText(getContext(), "save : " + noteText.getText().toString(), Toast.LENGTH_SHORT).show());
        //toolbar.setTitleTextColor(Color.TRANSPARENT);


        return view;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d("kek", "oncreate menu");
        menu.add("Ссылка").setOnMenuItemClickListener(menuItem -> {
            Toast.makeText(getContext(), profileId+" lolka", Toast.LENGTH_SHORT).show();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d("kek", "onprepare menu");
        super.onPrepareOptionsMenu(menu);

    }


    @Override
    public void onOptionsMenuClosed(Menu menu) {
        Log.d("kek", "onclose menu");
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        Log.d("kek", "ondestroy menu");
        super.onDestroyOptionsMenu();
    }

    @Override
    public void loadData() {
        compositeSubscription.add(
                Api.Profile().get(getTabUrl())
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

    private void addContactItem(int iconRes, String data) {
        ContactItem contactItem = new ContactItem(getContext());
        contactItem.setIcon(iconRes);
        contactItem.setOnClickListener(view1 -> Toast.makeText(getContext(), data + "", Toast.LENGTH_SHORT).show());
        contactList.addView(contactItem);
    }

    private void addDeviceItem(String text, String link) {
        DeviceItem deviceItem = new DeviceItem(getContext());
        deviceItem.setText(text);
        deviceItem.setOnClickListener(view1 -> Toast.makeText(getContext(), link + "", Toast.LENGTH_SHORT).show());
        devicesList.addView(deviceItem);
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
        if (profile.getNick() == null) return;
        ImageLoader.getInstance().displayImage(profile.getAvatar(), avatar, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                blur(loadedImage, toolbarBackground, imageUri);
                new Handler().postDelayed(() -> {
                    progressView.stopAnimation();
                    progressView.setVisibility(View.GONE);
                }, 500);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });

        findViewById(R.id.profile_block_counts).setVisibility(View.VISIBLE);
        findViewById(R.id.profile_block_information).setVisibility(View.VISIBLE);
        setTitle(profile.getNick());
        nick.setText(profile.getNick());
        group.setText(profile.getGroup());
        if (profile.getSign() != null) {
            sign.setText(profile.getSign());
            sign.setMovementMethod(new LinkMovementMethod());
            sign.setVisibility(View.VISIBLE);
        }

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

        if (profile.getContacts().size() > 0) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(view1 -> Toast.makeText(getContext(), "qms : " + profile.getContacts().get(0).first, Toast.LENGTH_SHORT).show());
        }
        if (profile.getContacts().size() > 1) {
            for (int i = 1; i < profile.getContacts().size(); i++)
                addContactItem(getIconRes(profile.getContacts().get(i).second), profile.getContacts().get(i).first);
            findViewById(R.id.profile_block_contacts).setVisibility(View.VISIBLE);
        }
        if (profile.getDevices().size() > 0) {
            for (Pair<String, String> device : profile.getDevices()) {
                addDeviceItem(device.second, device.first);
            }
            findViewById(R.id.profile_block_devices).setVisibility(View.VISIBLE);
        }

        if (profile.getNote() != null) {
            noteText.setText(profile.getNote());
            findViewById(R.id.profile_block_note).setVisibility(View.VISIBLE);
        }
        if (profile.getAbout() != null) {
            about.setText(profile.getAbout());
            findViewById(R.id.profile_block_about).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }

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

        public void setIcon(int iconRes) {
            if (iconRes == R.drawable.contact_icq) {
                int px = dpToPx(20);
                findViewById(R.id.icon).setPadding(px, px, px, px);
            }
            ((ImageView) findViewById(R.id.icon)).setImageDrawable(AppCompatResources.getDrawable(App.getContext(), iconRes));
        }
    }

    class DeviceItem extends LinearLayout {
        public DeviceItem(Context context) {
            super(context);
            inflate(context, R.layout.profile_device_list_item, this);
        }

        public void setText(String text) {
            ((TextView) findViewById(R.id.item_text)).setText(text);
        }
    }

    private void blur(Bitmap bkg, ImageView view, String url) {
        Log.d("kek", "start blur");
        //bkg = Bitmap.createScaledBitmap(bkg, view.getWidth(), view.getHeight(), false);

        bkg = centerCrop(bkg, view.getWidth(), view.getHeight());
        Log.d("kek", "end crop");
        float scaleFactor = 8;
        int radius = 24;

        Bitmap overlay = Bitmap.createBitmap((int) (view.getWidth() / scaleFactor),
                (int) (view.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        overlay.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        canvas.drawBitmap(bkg, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        Log.d("kek", "do blur");
        FastBlur.doBlur(overlay, radius, true);
        Log.d("kek", "end do blur");
        view.setImageBitmap(overlay);
        //storeImage(overlay, url);
        Log.d("kek", "end blur");
    }

    public static Bitmap centerCrop(final Bitmap src, final int w, final int h) {
        return crop(src, w, h, 0.5f, 0.5f);
    }

    public static Bitmap crop(final Bitmap src, final int w, final int h,
                              final float horizontalCenterPercent, final float verticalCenterPercent) {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0
                || verticalCenterPercent > 1) {
            throw new IllegalArgumentException(
                    "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                            + "1.0f, inclusive.");
        }
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src;
        }
        final Matrix m = new Matrix();
        final float scale = Math.max(
                (float) w / srcWidth,
                (float) h / srcHeight);
        m.setScale(scale, scale);
        final int srcCroppedW, srcCroppedH;
        int srcX, srcY;
        srcCroppedW = Math.round(w / scale);
        srcCroppedH = Math.round(h / scale);
        srcX = (int) (srcWidth * horizontalCenterPercent - srcCroppedW / 2);
        srcY = (int) (srcHeight * verticalCenterPercent - srcCroppedH / 2);
        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);
        final Bitmap cropped = Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */);
        return cropped;
    }
}
