package forpdateam.ru.forpda.ui.fragments.news.main.timeline;

import android.support.v7.util.DiffUtil;

import java.util.List;

import forpdateam.ru.forpda.data.news.entity.News;

/**
 * Created by isanechek on 8/10/17.
 */

public class NewsDiffCallback extends DiffUtil.Callback {
    private final List<News> oList;
    private final List<News> nList;

    public NewsDiffCallback(List<News> oList, List<News> nList) {
        this.oList = oList;
        this.nList = nList;
    }

    @Override
    public int getOldListSize() {
        return oList.size();
    }

    @Override
    public int getNewListSize() {
        return nList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oList.get(oldItemPosition).url == nList.get(newItemPosition).url;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        News nItem = nList.get(newItemPosition);
        News oItem = oList.get(oldItemPosition);
        return oItem.url.equals(nItem.url);
    }
}
