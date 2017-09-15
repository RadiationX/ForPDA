package forpdateam.ru.forpda.fragments.profile;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
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
import forpdateam.ru.forpda.utils.BitmapUtils;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.LinkMovementMethod;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ScrimHelper;
import io.reactivex.Observable;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileFragment extends TabFragment implements ProfileAdapter.ClickListener {
    //private LayoutInflater inflater;
    private TextView nick, group, sign;
    private ImageView avatar;
    //private LinearLayout countList, infoBlock, contactList, devicesList;
    //private EditText noteText;
    private CircularProgressView progressView;

    private Subscriber<ProfileModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<Boolean> saveNoteSubscriber = new Subscriber<>(this);
    private Subscriber<Bitmap> blurAvatarSubscriber = new Subscriber<>(this);

    private String tab_url = "";
    private ProfileModel currentProfile;
    private MenuItem copyLinkMenuItem;
    private MenuItem writeMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tab_url = getArguments().getString(ARG_TAB);
        }
        if (tab_url == null || tab_url.isEmpty())
            tab_url = "https://4pda.ru/forum/index.php?showuser=".concat(Integer.toString(ClientHelper.getUserId() == 0 ? 2556269 : ClientHelper.getUserId()));
    }

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_profile);
        ViewStub viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_profile);
        viewStub.inflate();
        nick = (TextView) findViewById(R.id.profile_nick);
        group = (TextView) findViewById(R.id.profile_group);
        sign = (TextView) findViewById(R.id.profile_sign);
        avatar = (ImageView) findViewById(R.id.profile_avatar);
        recyclerView = (RecyclerView) findViewById(R.id.profile_list);
        progressView = (CircularProgressView) findViewById(R.id.profile_progress);
        viewsReady();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        adapter = new ProfileAdapter();
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        toolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        toolbarLayout.setCollapsedTitleTextColor(Color.TRANSPARENT);
        toolbarLayout.setTitleEnabled(true);
        toolbarTitleView.setVisibility(View.GONE);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
        toolbarLayout.setLayoutParams(params);


        ScrimHelper scrimHelper = new ScrimHelper(appBarLayout, toolbarLayout);
        scrimHelper.setScrimListener(scrim1 -> {
            if (scrim1) {
                toolbar.getNavigationIcon().clearColorFilter();
                toolbar.getOverflowIcon().clearColorFilter();
            } else {
                toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        });

        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        return view;
    }


    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        copyLinkMenuItem = getMenu().add(R.string.copy_link)
                .setOnMenuItemClickListener(menuItem -> {
                    Utils.copyToClipBoard(tab_url);
                    return false;
                });
        writeMenuItem = getMenu().add(R.string.write)
                .setIcon(App.getVecDrawable(getContext(), R.drawable.ic_profile_toolbar_create))
                .setOnMenuItemClickListener(item -> {
                    IntentHandler.handle(currentProfile.getContacts().get(0).getUrl());
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            copyLinkMenuItem.setEnabled(true);
        } else {
            copyLinkMenuItem.setEnabled(false);
            writeMenuItem.setVisible(false);
        }
    }

    @Override
    public void loadData() {
        super.loadData();
        refreshToolbarMenuItems(false);
        mainSubscriber.subscribe(RxApi.Profile().getProfile(tab_url), this::onProfileLoad, new ProfileModel(), v -> loadData());
    }

    @Override
    public void onSaveClick(String text) {
        saveNoteSubscriber.subscribe(RxApi.Profile().saveNote(text), this::onNoteSave, false, v -> onSaveClick(text));
    }

    private void onNoteSave(boolean b) {
        Toast.makeText(getContext(), getString(b ? R.string.profile_note_saved : R.string.error_occurred), Toast.LENGTH_SHORT).show();
    }

    private void onProfileLoad(ProfileModel profile) {
        currentProfile = profile;
        if (currentProfile.getNick() == null) return;
        adapter.setProfile(currentProfile);
        adapter.notifyDataSetChanged();
        refreshToolbarMenuItems(true);
        ImageLoader.getInstance().loadImage(currentProfile.getAvatar(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                //Нужен handler, иначе при повторном создании фрагмента неверно вычисляется высота вьюхи
                new Handler().post(() -> {
                    if (!isAdded())
                        return;
                    blur(loadedImage);
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


        setTabTitle(String.format(getString(R.string.profile_with_Nick), currentProfile.getNick()));
        setTitle(currentProfile.getNick());
        nick.setText(currentProfile.getNick());
        group.setText(currentProfile.getGroup());
        if (currentProfile.getSign() != null) {
            sign.setText(currentProfile.getSign());
            sign.setVisibility(View.VISIBLE);
            sign.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (currentProfile.getContacts().size() > 0) {
            if (!Pattern.compile("showuser=".concat(Integer.toString(ClientHelper.getUserId()))).matcher(tab_url).find()) {
                writeMenuItem.setVisible(true);
            } else {
                writeMenuItem.setVisible(false);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void blur(Bitmap bkg) {
        float scaleFactor = 3;
        int radius = 4;
        Observable<Bitmap> observable = Observable.fromCallable(() -> {
            Log.d("SUKA", "BKG " + bkg.getWidth() + " : " + bkg.getHeight() + " : " + toolbarBackground.getWidth() + " : " + toolbarBackground.getHeight() + " : " + scaleFactor);
            Bitmap overlay = BitmapUtils.centerCrop(bkg, toolbarBackground.getWidth(), toolbarBackground.getHeight(), scaleFactor);
            BitmapUtils.fastBlur(overlay, radius, true);
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
}
