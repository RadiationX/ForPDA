package forpdateam.ru.forpda.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by radiationx on 05.10.17.
 */
/*
* Для управления и дополнительными вьюхами, когда нет данных и т.д.
* */
public class ContentController {
    public final static String TAG_NO_DATA = "NO_DATA";
    private View additionalRefresh;
    private ViewGroup additionalContent;
    private View mainRefresh;
    private ViewGroup mainContent;
    private boolean firstLoad = true;

    private HashMap<Object, View> contents = new HashMap<>();

    public ContentController(View additionalRefresh, ViewGroup additionalContent, ViewGroup mainContent) {
        this.additionalRefresh = additionalRefresh;
        this.additionalContent = additionalContent;
        this.mainContent = mainContent;
    }


    public void setMainRefresh(View mainRefresh) {
        this.mainRefresh = mainRefresh;
    }

    public boolean contains(Object tag) {
        return contents.get(tag) != null;
    }

    public View addContent(View content, Object tag) {
        View view = contents.get(tag);
        if (view == null) {
            view = content;
            view.setVisibility(View.GONE);
            contents.put(tag, view);
            additionalContent.addView(view, 0);
        }
        return view;
    }

    public View addContent(Context context, @LayoutRes int id, Object tag) {
        View view = contents.get(tag);
        if (view == null) {
            view = View.inflate(context, id, null);
            view.setVisibility(View.GONE);
            contents.put(tag, view);
            additionalContent.addView(view, 0);
        }
        return view;
    }

    public void showContent(Object tag) {
        View view = contents.get(tag);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            //mainContent.setVisibility(View.GONE);
        }
    }

    public void hideContent(Object tag) {
        View view = contents.get(tag);
        if (view != null) {
            view.setVisibility(View.GONE);
            //mainContent.setVisibility(View.VISIBLE);
        }
    }

    public void startRefreshing() {
        if (firstLoad) {
            mainContent.setVisibility(View.INVISIBLE);
            additionalRefresh.setVisibility(View.VISIBLE);
        } else if (mainRefresh != null) {
            if (mainRefresh instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) mainRefresh).setRefreshing(true);
            }
        }
    }

    public void stopRefreshing() {
        if (firstLoad) {
            mainContent.setVisibility(View.VISIBLE);
            additionalRefresh.setVisibility(View.GONE);
            firstLoad = false;
        } else if (mainRefresh != null) {
            if (mainRefresh instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) mainRefresh).setRefreshing(false);
            }
        }
    }

    public void setFirstLoad(boolean b) {
        firstLoad = b;
    }

    public void destroy() {
        additionalRefresh = null;
        mainContent = null;
        mainRefresh = null;
        contents.clear();
    }
}
