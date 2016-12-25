package forpdateam.ru.forpda.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxFragment;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ScrollAwareFABBehavior;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.utils.ErrorHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 07.08.16.
 */
public class TabFragment extends RxFragment implements ITabFragment {
    public final static String TITLE_ARG = "TAB_TITLE";
    public final static String SUBTITLE_ARG = "TAB_SUBTITLE";
    public final static String URL_ARG = "TAB_URL";
    private final static String prefix = "tab_fragment_";
    protected String tabUrl = "";
    protected View view;
    protected Toolbar toolbar;
    protected ImageView toolbarBackground;
    protected CoordinatorLayout coordinatorLayout;
    protected FloatingActionButton fab;
    private int UID = 0;
    private String title = getDefaultTitle();
    private String subtitle;
    private String parentTag;
    private ImageView icNoNetwork;
    protected TextView toolbarTitleView;
    protected TextView toolbarSubitleView;
    protected ImageView toolbarImageView;

    public TabFragment() {
        parentTag = TabManager.getActiveTag();
        setUID();
    }

    //Титл по умолчанию, отображается в тулбаре, когда ничего не задано
    @Override
    public String getDefaultTitle() {
        return this.getClass().getSimpleName();
    }

    /* For TabManager etc */
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getUID() {
        return UID;
    }

    @Override
    public void setUID() {
        UID = (getArguments() + getTabUrl() + getClass().getSimpleName()).hashCode();
        Log.d("UID", "" + UID);
    }

    //Одинокий фрагмент не будет дублироваться в списке вкладок
    @Override
    public boolean isAlone() {
        return false;
    }

    //Для фрагментов с использование кеша, чтобы не отображалась иконка отсутствия интернета
    @Override
    public boolean isUseCache() {
        return false;
    }

    //Сомнительная штука, возможно даже выпилить надо будет
    @Override
    public String getTabUrl() {
        return tabUrl;
    }

    public void setTabUrl(String tabUrl) {
        this.tabUrl = tabUrl;
    }

    @Override
    public String getParentTag() {
        return parentTag;
    }

    @Override
    public boolean onBackPressed() {
        Log.d("kek", "onbackpressed tab");
        return false;
    }

    @Override
    public void hidePopupWindows() {
        getMainActivity().hidePopupWindows();
    }

    //Загрузка каких-то данных, выполняется только при наличии сети
    @Override
    public void loadData() {

    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            parentTag = savedInstanceState.getString(prefix + "parent_tag");
            title = savedInstanceState.getString(prefix + "title");
            subtitle = savedInstanceState.getString(prefix + "subtitle");
        }
        if (getArguments() != null)
            setTabUrl(getArguments().getString(URL_ARG));
        setHasOptionsMenu(true);
        Log.d("kek", "oncreate " + getArguments() + " : " + savedInstanceState + " : " + title);
    }

    //Загрузка основной вьюхи со всеми нужными элементами вроде тулбара, фаба и фич в тулбаре
    protected void initBaseView(LayoutInflater inflater, @Nullable ViewGroup container) {
        Log.d("kek", "view " + view);
        view = inflater.inflate(R.layout.fragment_base, container, false);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitleView = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarSubitleView = (TextView) toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarImageView = (ImageView) toolbar.findViewById(R.id.toolbar_image_icon);
        toolbarBackground = (ImageView) findViewById(R.id.toolbar_image_background);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        icNoNetwork = (ImageView) view.findViewById(R.id.ic_no_network);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        int iconRes;
        if (isAlone()) {
            iconRes = R.drawable.ic_menu_white_24dp;
            toolbar.setNavigationOnClickListener(getMainActivity().getToggleListener());
        } else {
            iconRes = R.drawable.ic_arrow_back_white_24dp;
            toolbar.setNavigationOnClickListener(getMainActivity().getRemoveTabListener());
        }
        toolbar.setNavigationIcon(App.getAppDrawable(iconRes));


        if (!Client.getInstance().getNetworkState()) {
            if (!isUseCache())
                icNoNetwork.setVisibility(View.VISIBLE);
            if (!getTag().equals(TabManager.getActiveTag())) return;
            Snackbar.make(getCoordinatorLayout(), "No network connection", Snackbar.LENGTH_LONG).show();
        }

        if (getArguments() != null) {
            setTitle(getArguments().getString(TITLE_ARG, title));
            setSubtitle(getArguments().getString(SUBTITLE_ARG, subtitle));
        } else {
            if (title != null)
                setTitle(title);
        }

        Client.getInstance().addNetworkObserver((observable, o) -> {
            if ((isUseCache() || icNoNetwork.getVisibility() == View.VISIBLE) && (boolean) o) {
                loadData();
                icNoNetwork.setVisibility(View.GONE);
            }
        });
    }

    protected void baseInflateFragment(LayoutInflater inflater, @LayoutRes int res) {
        inflater.inflate(res, (ViewGroup) view.findViewById(R.id.fragment_content), true);
    }


    protected void viewsReady() {
        if (Client.getInstance().getNetworkState()) {
            loadData();
        }
        toolbar.getMenu().add("logout").setOnMenuItemClickListener(menuItem -> {
            new Task().execute();
            return false;
        });
    }

    class Task extends AsyncTask {
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Api.Auth().tryLogout();
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (exception != null) {
                new AlertDialog.Builder(getMainActivity())
                        .setMessage(exception.getMessage())
                        .create()
                        .show();
            } else {
                Toast.makeText(getContext(), "logout complete", Toast.LENGTH_LONG).show();
                Api.Auth().doOnLogout();
            }
        }
    }

    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        // TODO: 20.12.16 not work in 25.1.0
        //params.setBehavior(new ScrollAwareFABBehavior(fab.getContext(), null));
        fab.requestLayout();
    }

    protected void setWhiteBackground() {
        view.findViewById(R.id.fragment_content).setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("kek", "onactivitycreated " + getArguments() + " : " + savedInstanceState + " : " + title);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(prefix + "parent_tag", parentTag);
        outState.putString(prefix + "title", title);
        outState.putString(prefix + "subtitle", subtitle);
    }

    /* For UI in class */
    protected final String getSubtitle() {
        return subtitle;
    }

    protected final void setTitle(String title) {
        this.title = title;
        getMainActivity().updateTabList();
        //getTitleBar().setTitle(title);
        toolbarTitleView.setText(title);
    }

    protected final void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        if (subtitle == null) {
            toolbarSubitleView.setVisibility(View.GONE);
        } else {
            toolbarSubitleView.setText(subtitle);
            toolbarSubitleView.setVisibility(View.VISIBLE);
        }
        //getTitleBar().setSubtitle(subtitle);
    }


    protected final View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    protected final Toolbar getTitleBar() {
        return toolbar;
    }

    protected final MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("kek", this + " : onresume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("kek", this + " : onpause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Client.getInstance().cancelCallByUrl(getTabUrl());
    }

    protected void handleErrorRx(Throwable throwable) {
        handleErrorRx(throwable, null);
    }

    protected void handleErrorRx(Throwable throwable, View.OnClickListener listener) {
        ErrorHandler.handle(this, throwable, listener);
    }

    public class Subscriber<T> {
        public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn) {
            return subscribe(observable, onNext, onErrorReturn, null);
        }

        public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn, View.OnClickListener onErrorAction) {
            return observable.onErrorReturn(throwable -> {
                handleErrorRx(throwable, onErrorAction);
                return onErrorReturn;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext, throwable -> handleErrorRx(throwable, onErrorAction));
        }
    }

    /* Experiment */
    public static class Builder<T extends TabFragment> {
        private T tClass;

        public Builder(Class<T> tClass) {
            try {
                this.tClass = tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Builder setArgs(Bundle args) {
            tClass.setArguments(args);
            return this;
        }

        /*public Builder setTitle(String title) {
            tClass.setTitle(title);
            return this;
        }*/

        public T build() {
            tClass.setUID();
            return tClass;
        }
    }
}
