package forpdateam.ru.forpda.fragments.theme;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.Date;

import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.fragments.theme.adapters.ThemeAdapter;

/**
 * Created by radiationx on 05.08.16.
 */
public class ThemeFragmentNative extends ThemeFragment {
    private RecyclerView recyclerView;
    private ThemeAdapter adapter;

    @Override
    protected void addShowingView() {
        recyclerView = new RecyclerView(getContext());
        refreshLayout.addView(recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void findNext(boolean next) {

    }

    @Override
    protected void findText(String text) {

    }

    @Override
    protected void saveToHistory(ThemePage themePage) {

    }

    @Override
    protected void updateHistoryLast(ThemePage themePage) {

    }

    @Override
    public void scrollToAnchor(String anchor) {

    }

    @Override
    protected void updateHistoryLastHtml() {

    }

    @Override
    protected void updateView() {
        super.updateView();
        Date date = new Date();
        adapter = new ThemeAdapter(currentPage.getPosts(), getContext());
        recyclerView.setAdapter(adapter);
        Log.d("FORPDA_LOG", "theme UI CREATE time " + (new Date().getTime() - date.getTime()));
    }
}
