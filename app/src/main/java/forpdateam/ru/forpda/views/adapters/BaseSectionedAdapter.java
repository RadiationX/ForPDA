package forpdateam.ru.forpda.views.adapters;

import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseSectionedAdapter<E, VH extends BaseSectionedViewHolder> extends SectionedRecyclerViewAdapter<VH> {
    protected List<Pair<String, List<E>>> sections = new ArrayList<>();

    public void addSection(Pair<String, List<E>> item) {
        sections.add(item);
    }

    public void clear() {
        for (Pair<String, List<E>> section : sections)
            section.second.clear();
        sections.clear();
    }

    protected int[] getItemPosition(int layPos) {
        int result[] = new int[]{-1, -1};
        int sumPrevSections = 0;
        for (int i = 0; i < getSectionCount(); i++) {
            result[0] = i;
            result[1] = layPos - i - sumPrevSections - 1;
            sumPrevSections += getItemCount(i);
            if (sumPrevSections + i >= layPos) break;
        }
        if (result[1] < 0) {
            result[0] = -1;
            result[1] = -1;
        }
        return result;
    }

    public E getItem(int layPos) {
        int position[] = getItemPosition(layPos);
        if (position[0] == -1) {
            return null;
        }
        return sections.get(position[0]).second.get(position[1]);
    }

    public E getItem(int section, int relativePosition) {
        return sections.get(section).second.get(relativePosition);
    }

    @Override
    public int getSectionCount() {
        return sections.size();
    }

    @Override
    public int getItemCount(int section) {
        return sections.get(section).second.size();
    }

    @Override
    public void onBindHeaderViewHolder(VH vh, int i, boolean b) {

    }

    @Override
    public void onBindFooterViewHolder(VH vh, int i) {

    }

    @Override
    public void onBindViewHolder(VH vh, int i, int i1, int i2) {

    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    protected View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);

        boolean onItemLongClick(T item);
    }
}
