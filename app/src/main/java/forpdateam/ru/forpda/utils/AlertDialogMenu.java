package forpdateam.ru.forpda.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 27.10.16.
 */

public class AlertDialogMenu<E> {
    private List<MenuItem> items = new ArrayList<>();

    public void addItem(CharSequence title, OnClickListener<E> listener) {
        items.add(new MenuItem(title, listener));
    }

    public void addItem(int index, CharSequence title, OnClickListener<E> listener) {
        if (index < 0) index = 0;
        if (index > items.size()) index = items.size() - 1;

        items.add(index, new MenuItem(title, listener));
    }

    public void addItem(MenuItem item){
        items.add(item);
    }

    public boolean contains(CharSequence title) {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).title.equals(title))
                return true;
        return false;
    }

    public void remove(int i) {
        items.remove(i);
    }

    public int containsIndex(CharSequence title) {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).title.equals(title))
                return i;
        return -1;
    }

    public void changeTitle(int i, CharSequence title) {
        items.get(i).setTitle(title);
    }

    public CharSequence[] getTitles() {
        CharSequence[] result = new CharSequence[items.size()];
        for (int i = 0; i < items.size(); i++)
            result[i] = items.get(i).title;
        return result;
    }

    public void onClick(int i, E data) {
        items.get(i).onClick(data);
    }

    public class MenuItem implements OnClickListener<E> {
        private OnClickListener<E> listener;
        private CharSequence title;

        public MenuItem(CharSequence title, OnClickListener<E> listener) {
            this.title = title;
            this.listener = listener;
        }

        public void setTitle(CharSequence title) {
            this.title = title;
        }

        @Override
        public void onClick(E data) {
            if (listener != null)
                listener.onClick(data);
        }
    }

    public interface OnClickListener<E> {
        void onClick(E data);
    }
}
