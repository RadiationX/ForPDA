package forpdateam.ru.forpda.client;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by radiationx on 26.03.17.
 */

public class ClientHelper {
    public final static boolean AUTH_STATE_LOGIN = true;
    public final static boolean AUTH_STATE_LOGOUT = false;
    private static ClientHelper clientHelper = null;
    private MyObservable loginObservable = new MyObservable();
    private MyObservable observable = new MyObservable();
    private static boolean authState = false;
    private static String userId = "0";
    private static int userIdInt = 0;
    private static int qmsCount = 0, mentionsCount = 0, favoritesCount = 0;

    public static ClientHelper getInstance() {
        if (clientHelper == null) clientHelper = new ClientHelper();
        return clientHelper;
    }

    public void addLoginObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void addCountsObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void notifyAuthChanged(boolean authState) {
        ClientHelper.setAuthState(authState);
        loginObservable.notifyObservers(authState);
    }

    public void notifyCountsChanged() {
        observable.notifyObservers();
    }

    public static boolean getAuthState() {
        return authState;
    }

    public static void setAuthState(boolean b) {
        authState = b;
    }

    public static String getUserId() {
        return userId;
    }

    public static int getUserIdInt() {
        return userIdInt;
    }

    public static void setUserId(String userId1) {
        userId = userId1;
        Log.d("FORPDA_LOG", "userid 1: " + userId1);
        try {
            if (userId1 != null)
                userIdInt = Integer.parseInt(userId1);
        } catch (NumberFormatException ignore) {
        }
    }

    public static int getAllCounts() {
        return qmsCount + favoritesCount + mentionsCount;
    }

    public static int getQmsCount() {
        return qmsCount;
    }

    public static int getFavoritesCount() {
        return favoritesCount;
    }

    public static int getMentionsCount() {
        return mentionsCount;
    }

    public static void setQmsCount(int qmsCount) {
        ClientHelper.qmsCount = qmsCount;
    }

    public static void setFavoritesCount(int favoritesCount) {
        ClientHelper.favoritesCount = favoritesCount;
    }

    public static void setMentionsCount(int mentionsCount) {
        ClientHelper.mentionsCount = mentionsCount;
    }

    private class MyObservable extends Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }
}
