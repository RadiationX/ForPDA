package forpdateam.ru.forpda.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by radiationx on 27.10.16.
 */

public class AlertDialogMenu<T, E> {
    private List<MenuItem> items = new ArrayList<>();

    public void addItem(CharSequence title, OnClickListener<T, E> listener) {
        items.add(new MenuItem(title, listener));
    }

    public void addItem(int index, CharSequence title, OnClickListener<T, E> listener) {
        if (index < 0) index = 0;
        if (index > items.size()) index = items.size() - 1;

        items.add(index, new MenuItem(title, listener));
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public void addItems(Collection<MenuItem> items) {
        this.items.addAll(items);
    }

    public List<MenuItem> getItems(){
        return items;
    }

    public MenuItem get(int index) {
        return items.get(index);
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

    public void clear() {
        items.clear();
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

    public void onClick(int i, T context, E data) {
        items.get(i).onClick(context, data);
    }

    public class MenuItem implements OnClickListener<T, E> {
        private OnClickListener<T, E> listener;
        private CharSequence title;

        public MenuItem(CharSequence title, OnClickListener<T, E> listener) {
            this.title = title;
            this.listener = listener;
        }

        public void setTitle(CharSequence title) {
            this.title = title;
        }

        @Override
        public void onClick(T context, E data) {
            if (listener != null)
                listener.onClick(context, data);
        }
    }

    public interface OnClickListener<T, E> {
        void onClick(T context, E data);
    }
}
