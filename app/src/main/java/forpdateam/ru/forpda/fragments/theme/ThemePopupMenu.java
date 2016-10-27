package forpdateam.ru.forpda.fragments.theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 27.10.16.
 */

class ThemePopupMenu<E> {
    private List<MenuItem> items = new ArrayList<>();
    private List<CharSequence> titlesList = new ArrayList<>();

    void addItem(CharSequence title, OnClickListener listener) {
        items.add(new MenuItem(listener));
        titlesList.add(title);
    }

    void addItem(int index, CharSequence title, OnClickListener listener) {
        if (index < 0) index = 0;
        if (index > items.size()) index = items.size() - 1;

        items.add(index, new MenuItem(listener));
        titlesList.add(index, title);
    }

    public boolean contains(CharSequence title) {
        return titlesList.contains(title);
    }

    public void remove(int i) {
        items.remove(i);
        titlesList.remove(i);
    }

    public int containsIndex(CharSequence title) {
        for (int i = 0; i < titlesList.size(); i++)
            if (titlesList.get(i).equals(title))
                return i;
        return -1;
    }

    public CharSequence[] getTitles() {
        return titlesList.toArray(new CharSequence[titlesList.size()]);
    }

    public void onClick(int i, E data) {
        items.get(i).onClick(data);
    }

    class MenuItem implements OnClickListener<E> {
        private OnClickListener listener;

        public MenuItem(OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(E data) {
            if (listener != null)
                listener.onClick(data);
        }
    }

    interface OnClickListener<E> {
        void onClick(E data);
    }
}
