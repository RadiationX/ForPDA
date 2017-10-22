package forpdateam.ru.forpda.fragments.qms;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.qms.QmsContactBd;
import forpdateam.ru.forpda.data.realm.qms.QmsThemeBd;
import forpdateam.ru.forpda.data.realm.qms.QmsThemesBd;
import forpdateam.ru.forpda.utils.SimpleObservable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 22.10.17.
 */

public class QmsHelper {
    private static QmsHelper instance = null;
    private SimpleObservable qmsEvents = new SimpleObservable();

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("Already init");
        }
        instance = new QmsHelper();
    }

    public static QmsHelper get() {
        if (instance == null) {
            throw new IllegalStateException("Not init");
        }
        return instance;
    }

    public QmsHelper() {
        Observer notification = (observable, o) -> {
            if (o == null) return;
            TabNotification event = (TabNotification) o;
            handleEvent(event);
        };
        App.get().subscribeQms(notification);
    }


    private void handleEvent(TabNotification event) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<QmsThemesBd> themes = realm
                .where(QmsThemesBd.class)
                .findAll();
        QmsThemeBd targetTheme = null;
        QmsThemesBd targetDialog = null;
        for (QmsThemesBd dialog : themes) {
            for (QmsThemeBd theme : dialog.getThemes()) {
                if (theme.getId() == event.getEvent().getSourceId()) {
                    targetDialog = dialog;
                    targetTheme = theme;
                    break;
                }
            }
            if (targetTheme != null) {
                break;
            }
        }

        if (targetTheme != null) {
            QmsThemeBd finalTargetTheme = targetTheme;
            QmsThemesBd finalTargetDialog = targetDialog;
            realm.executeTransaction(realm1 -> {
                if (event.isWebSocket()) {
                    if (NotificationEvent.isRead(event.getType())) {
                        finalTargetTheme.setCountNew(0);
                    }
                } else {
                    if (NotificationEvent.isNew(event.getType())) {
                        finalTargetTheme.setCountNew(event.getEvent().getMsgCount());
                    }
                }

                QmsContactBd contact = realm1
                        .where(QmsContactBd.class)
                        .equalTo("id", finalTargetDialog.getUserId())
                        .findFirst();

                if (contact != null) {
                    int count = 0;
                    for (QmsThemeBd theme : finalTargetDialog.getThemes()) {
                        count += theme.getCountNew();
                    }
                    contact.setCount(count);
                }

            });
        }

        int globalCount = 0;
        for (NotificationEvent ev : event.getLoadedEvents()) {
            globalCount += ev.getMsgCount();
        }

        ClientHelper.setQmsCount(globalCount);
        ClientHelper.get().notifyCountsChanged();

        realm.close();
        notifyQms(event);
    }

    public void subscribeQms(Observer observer) {
        qmsEvents.addObserver(observer);
    }

    public void unSubscribeQms(Observer observer) {
        qmsEvents.deleteObserver(observer);
    }

    public void notifyQms(TabNotification event) {
        qmsEvents.notifyObservers(event);
    }
}
