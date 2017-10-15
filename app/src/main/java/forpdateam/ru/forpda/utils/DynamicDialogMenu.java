package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 27.10.16.
 */

public class DynamicDialogMenu<T, E> {
    private List<MenuItem> allItems = new ArrayList<>();
    private List<MenuItem> allowedItems = new ArrayList<>();

    public MenuItem addItem(CharSequence title, OnClickListener<T, E> listener) {
        MenuItem item = new MenuItem(title, listener);
        allItems.add(item);
        return item;
    }

    public MenuItem addItem(CharSequence title) {
        MenuItem item = new MenuItem(title);
        allItems.add(item);
        return item;
    }

    public void allow(int index) {
        allow(get(index));
    }

    public void allow(MenuItem item) {
        allowedItems.add(item);
    }

    public void allowAll() {
        this.allowedItems.addAll(allItems);
    }

    public void disallowAll() {
        allowedItems.clear();
    }

    public List<MenuItem> getAllItems() {
        return allItems;
    }

    public MenuItem get(int index) {
        return allItems.get(index);
    }

    public int containsIndex(CharSequence title) {
        for (int i = 0; i < allItems.size(); i++)
            if (allItems.get(i).title.equals(title))
                return i;
        return -1;
    }

    public void changeTitle(int i, CharSequence title) {
        allItems.get(i).setTitle(title);
    }

    public CharSequence[] getTitles() {
        CharSequence[] result = new CharSequence[allowedItems.size()];
        for (int i = 0; i < allowedItems.size(); i++)
            result[i] = allowedItems.get(i).title;
        return result;
    }

    public void show(Context uiContext, T context, E data) {
        show(uiContext, null, context, data);
    }

    public void show(Context uiContext, String title, T context, E data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(uiContext);
        if (title != null) {
            builder.setTitle(title);
        }
        builder.setItems(getTitles(), (dialog, which) -> onClick(which, context, data));
        builder.show();
    }

    public void onClick(int i, T context, E data) {
        allowedItems.get(i).onClick(context, data);
    }

    public class MenuItem implements OnClickListener<T, E> {
        private OnClickListener<T, E> listener;
        private CharSequence title;

        public MenuItem(CharSequence title, OnClickListener<T, E> listener) {
            this.title = title;
            this.listener = listener;
        }

        public MenuItem(CharSequence title) {
            this.title = title;
        }

        public MenuItem setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public MenuItem setListener(OnClickListener<T, E> listener) {
            this.listener = listener;
            return this;
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
