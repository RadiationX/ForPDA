package forpdateam.ru.forpda.fragments.profile;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.BlurUtil;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.ourparser.LinkMovementMethod;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import io.reactivex.Observable;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileFragment extends TabFragment {
    private LayoutInflater inflater;
    private TextView nick, group, sign, about;
    private ImageView avatar;
    private LinearLayout countList, infoBlock, contactList, devicesList;
    private EditText noteText;
    private CircularProgressView progressView;
    private Window window;
    private int statusBarColor = -1, standardColor = -1;
    private ValueAnimator statusBarValueAnimator;

    private Subscriber<ProfileModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<Boolean> saveNoteSubscriber = new Subscriber<>(this);
    private Subscriber<Bitmap> blurAvatarSubscriber = new Subscriber<>(this);

    private String tab_url = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tab_url = getArguments().getString(ARG_TAB);
        }
        if (tab_url == null || tab_url.isEmpty())
            tab_url = "http://4pda.ru/forum/index.php?showuser=".concat(Integer.toString(ClientHelper.getUserId() == 0 ? 2556269 : ClientHelper.getUserId()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_profile);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_profile);
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
        viewsReady();

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT);
        toolbarLayout.setTitleEnabled(true);
        toolbarTitleView.setVisibility(View.GONE);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED); // list other flags here by |
        toolbarLayout.setLayoutParams(params);
        /*toolbarLayout.setExpandedTitleGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        toolbarLayout.setExpandedTitleMarginTop(dpToPx(216));
        toolbarLayout.setScrimVisibleHeightTrigger(dpToPx(144));
        toolbarLayout.setExpandedTitleTextAppearance(R.style.QText);
        toolbarLayout.setScrimAnimationDuration(225);
*/
        noteText = (EditText) findViewById(R.id.profile_note_text);
        noteText.clearFocus();
        findViewById(R.id.profile_save_note).setOnClickListener(view1 -> saveNote());
        //toolbar.setTitleTextColor(Color.TRANSPARENT);

        toolbar.getMenu().add("Скопировать ссылку").setOnMenuItemClickListener(menuItem -> {
            Utils.copyToClipBoard(tab_url);
            return false;
        });
        if (getActivity() != null && getActivity().getWindow() != null) {
            window = getActivity().getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                standardColor = ContextCompat.getColor(getContext(), R.color.status_bar_color);
            }
        }
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(standardColor);
            if (statusBarValueAnimator != null) {
                statusBarValueAnimator.cancel();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isDetached() && isAdded() && isVisible() && !isHidden())
                window.setStatusBarColor(statusBarColor);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    @Override
    public void loadData() {
        mainSubscriber.subscribe(RxApi.Profile().getProfile(tab_url), this::onProfileLoad, new ProfileModel(), v -> loadData());
    }

    public void saveNote() {
        saveNoteSubscriber.subscribe(RxApi.Profile().saveNote(noteText.getText().toString()), this::onNoteSave, false, v -> saveNote());
    }


    private void onNoteSave(boolean b) {
        Toast.makeText(getContext(), b ? "Запись сохранена" : "Возникла ошибка, запись не сохранена", Toast.LENGTH_SHORT).show();
    }

    private void addCountItem(String title, Pair<String, String> data) {
        CountItem countItem = new CountItem(getContext());
        countItem.setTitle(title);
        countItem.setDesc(data.second);
        countItem.setOnClickListener(view1 -> IntentHandler.handle(data.first));
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
        contactItem.setOnClickListener(view1 -> IntentHandler.handle(data));
        contactList.addView(contactItem);
    }

    private void addDeviceItem(String text, String link) {
        DeviceItem deviceItem = new DeviceItem(getContext());
        deviceItem.setText(text);
        deviceItem.setOnClickListener(view1 -> IntentHandler.handle(link));
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

    private void onProfileLoad(ProfileModel profile) {
        if (profile.getNick() == null) return;
        long time = System.currentTimeMillis();
        ImageLoader.getInstance().loadImage(profile.getAvatar(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                //Нужен handler, иначе при повторном создании фрагмента неверно вычисляется высота вьюхи
                new Handler().post(() -> {
                    if (!isAdded())
                        return;
                    blur(loadedImage);
                    Palette.from(loadedImage).generate(palette -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Palette.Swatch swatch = palette.getDarkMutedSwatch();
                            Log.d("FORPDA_LOG", "COLOR 1");
                            if (swatch == null) {
                                Log.d("FORPDA_LOG", "COLOR 2");
                                swatch = palette.getMutedSwatch();
                            }
                            if (swatch == null) {
                                Log.d("FORPDA_LOG", "COLOR 3");
                                swatch = palette.getDarkVibrantSwatch();
                            }
                            statusBarColor = swatch == null ? standardColor : swatch.getRgb();
                            Log.d("FORPDA_LOG", "COLOR " + (swatch != null) + " : " + statusBarColor);
                            if (!isDetached() && isAdded() && isVisible() && !isHidden()) {
                                if (swatch == null) {
                                    window.setStatusBarColor(statusBarColor);
                                } else {
                                    statusBarValueAnimator = new ValueAnimator();
                                    statusBarValueAnimator.setIntValues(0, 255);
                                    statusBarValueAnimator.setDuration(500);
                                    statusBarValueAnimator.setInterpolator(new DecelerateInterpolator());
                                    statusBarValueAnimator.addUpdateListener(animation -> {
                                        window.setStatusBarColor(Color.argb(((Integer) animation.getAnimatedValue()), Color.red(statusBarColor), Color.green(statusBarColor), Color.blue(statusBarColor)));
                                    });
                                    statusBarValueAnimator.start();
                                }
                                toolbarLayout.setContentScrimColor(statusBarColor);
                            }
                        }
                    });
                    Bitmap overlay = Bitmap.createBitmap(loadedImage.getWidth(), loadedImage.getHeight(), Bitmap.Config.RGB_565);
                    overlay.eraseColor(Color.WHITE);
                    Canvas canvas = new Canvas(overlay);
                    canvas.drawBitmap(loadedImage, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
                    AlphaAnimation animation = new AlphaAnimation(0, 1);
                    animation.setDuration(500);
                    animation.setFillAfter(true);
                    avatar.setImageBitmap(overlay);
                    avatar.startAnimation(animation);

                    AlphaAnimation animation1 = new AlphaAnimation(1, 0);
                    animation1.setDuration(500);
                    animation1.setFillAfter(true);
                    progressView.startAnimation(animation1);
                    new Handler().postDelayed(() -> {
                        progressView.stopAnimation();
                        progressView.setVisibility(View.GONE);
                    }, 500);
                });

            }
        });


        setTabTitle("Профиль ".concat(profile.getNick()));
        setTitle(profile.getNick());
        nick.setText(profile.getNick());
        group.setText(profile.getGroup());
        if (profile.getSign() != null) {
            Log.d("FORPDA_LOG", "view sign set");
            sign.setText(profile.getSign());
            sign.setVisibility(View.VISIBLE);
            Log.d("FORPDA_LOG", "view sign setted");
            sign.setMovementMethod(LinkMovementMethod.getInstance());
        }
        Log.d("FORPDA_LOG", "check 1 " + (System.currentTimeMillis() - time));
        if (profile.getPosts() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_posts), profile.getPosts());
        if (profile.getTopics() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_themes), profile.getTopics());
        if (profile.getReputation() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_rep), profile.getReputation());
        if (profile.getKarma() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_karma), profile.getKarma());
        if (profile.getSitePosts() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_site_posts), profile.getSitePosts());
        if (profile.getComments() != null)
            addCountItem(getContext().getString(R.string.profile_item_text_comments), profile.getComments());
        Log.d("FORPDA_LOG", "check 2 " + (System.currentTimeMillis() - time));
        if (profile.getGender() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_gender), profile.getGender());
        if (profile.getBirthDay() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_birthday), profile.getBirthDay());
        if (profile.getCity() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_city), profile.getCity());
        if (profile.getUserTime() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_user_time), profile.getUserTime());
        inflater.inflate(R.layout.profile_divider, infoBlock);
        if (profile.getRegDate() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_reg), profile.getRegDate());
        if (profile.getOnlineDate() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_last_online), profile.getOnlineDate());
        if (profile.getAlerts() != null)
            addInfoItem(getContext().getString(R.string.profile_item_text_alerts), profile.getAlerts());
        Log.d("FORPDA_LOG", "check 3 " + (System.currentTimeMillis() - time));
        if (profile.getContacts().size() > 0) {
            if (!Pattern.compile("showuser=".concat(Integer.toString(ClientHelper.getUserId()))).matcher(tab_url).find()) {
                toolbar.getMenu().add("Написать").setIcon(App.getAppDrawable(R.drawable.ic_create_white_24dp)).setOnMenuItemClickListener(item -> {
                    IntentHandler.handle(profile.getContacts().get(0).first);
                    return false;
                }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
        if (profile.getContacts().size() > 1) {
            for (int i = 1; i < profile.getContacts().size(); i++)
                addContactItem(getIconRes(profile.getContacts().get(i).second), profile.getContacts().get(i).first);
            findViewById(R.id.profile_block_contacts).setVisibility(View.VISIBLE);
        }
        Log.d("FORPDA_LOG", "check 4 " + (System.currentTimeMillis() - time));
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
            about.setMovementMethod(LinkMovementMethod.getInstance());
            findViewById(R.id.profile_block_about).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.profile_block_counts).setVisibility(View.VISIBLE);
        findViewById(R.id.profile_block_information).setVisibility(View.VISIBLE);
        Log.d("FORPDA_LOG", "full time " + (System.currentTimeMillis() - time));
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
            ((ImageView) findViewById(R.id.icon)).setImageDrawable(App.getAppDrawable(iconRes));
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void blur(Bitmap bkg) {
        float scaleFactor = 3;
        int radius = 4;
        Observable<Bitmap> observable = Observable.fromCallable(() -> {
            Bitmap overlay = centerCrop(bkg, toolbarBackground.getWidth(), toolbarBackground.getHeight(), scaleFactor);
            BlurUtil.fastBlur(overlay, radius, true);
            return overlay;
        });
        blurAvatarSubscriber.subscribe(observable, bitmap -> {
            AlphaAnimation animation1 = new AlphaAnimation(0, 1);
            animation1.setDuration(500);
            animation1.setFillAfter(true);
            toolbarBackground.setBackground(new BitmapDrawable(getResources(), bitmap));
            toolbarBackground.startAnimation(animation1);
        }, bkg);
    }

    public static Bitmap centerCrop(final Bitmap src, int w, int h, float scaleFactor) {
        final int srcWidth = (int) (src.getWidth() / scaleFactor);
        final int srcHeight = (int) (src.getHeight() / scaleFactor);
        w = (int) (w / scaleFactor);
        h = (int) (h / scaleFactor);
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
        srcX = (int) (srcWidth * 0.5f - srcCroppedW / 2);
        srcY = (int) (srcHeight * 0.5f - srcCroppedH / 2);
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);

        Bitmap overlay = Bitmap.createBitmap(srcCroppedW, srcCroppedH, Bitmap.Config.ARGB_8888);
        overlay.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-srcX / scaleFactor, -srcY / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        canvas.drawBitmap(src, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return overlay;
    }
}
