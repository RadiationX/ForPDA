package forpdateam.ru.forpda.views.drawers;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.Objects;

import forpdateam.ru.forpda.BuildConfig;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.auth.AuthFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandsFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.fragments.news.main.NewsMainParentFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.fragments.search.SearchFragment;

/**
 * Created by radiationx on 02.05.17.
 */

public class MenuItems {
    public final static int ACTION_APP_SETTINGS = 0;
    private ArrayList<MenuItem> createdMenuItems = new ArrayList<>();

    public MenuItems() {
        createdMenuItems.add(new MenuItem("Авторизация", R.drawable.ic_person_add, AuthFragment.class));
        if (Objects.equals(BuildConfig.FLAVOR, "dev"))
            createdMenuItems.add(new MenuItem("Новости", R.drawable.ic_newspaper, NewsMainParentFragment.class));
        createdMenuItems.add(new MenuItem("Избранное", R.drawable.ic_star, FavoritesFragment.class));
        createdMenuItems.add(new MenuItem("Контакты", R.drawable.ic_contacts, QmsContactsFragment.class));
        createdMenuItems.add(new MenuItem("Ответы", R.drawable.ic_notifications, MentionsFragment.class));
        createdMenuItems.add(new MenuItem("DevDB", R.drawable.ic_devices_other, BrandsFragment.class));
        //createdMenuItems.add(new MenuItem("DevDB dev", R.drawable.ic_devices_other, DeviceFragment.class));
        createdMenuItems.add(new MenuItem("Форум", R.drawable.ic_forum, ForumFragment.class));
        createdMenuItems.add(new MenuItem("Поиск", R.drawable.ic_search, SearchFragment.class));
        createdMenuItems.add(new MenuItem("Настройки", R.drawable.ic_settings, ACTION_APP_SETTINGS));
    }

    public ArrayList<MenuItem> getCreatedMenuItems() {
        return createdMenuItems;
    }

    public class MenuItem {
        private String title;
        private int iconRes;
        private int notifyCount = 0;
        private String attachedTabTag = "";
        private Class<? extends TabFragment> tabClass;
        private int action;
        private boolean active = false;

        public MenuItem(String title, @DrawableRes int iconRes, Class<? extends TabFragment> tabClass) {
            this.title = title;
            this.iconRes = iconRes;
            this.tabClass = tabClass;
        }

        public MenuItem(String title, @DrawableRes int iconRes, int action) {
            this.title = title;
            this.iconRes = iconRes;
            this.action = action;
        }

        public String getTitle() {
            return title;
        }

        public int getIconRes() {
            return iconRes;
        }

        public int getNotifyCount() {
            return notifyCount;
        }

        public String getAttachedTabTag() {
            return attachedTabTag;
        }

        public int getAction() {
            return action;
        }

        public Class<? extends TabFragment> getTabClass() {
            return tabClass;
        }

        public void setAttachedTabTag(String attachedTabTag) {
            this.attachedTabTag = attachedTabTag;
        }

        public void setNotifyCount(int notifyCount) {
            this.notifyCount = notifyCount;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
