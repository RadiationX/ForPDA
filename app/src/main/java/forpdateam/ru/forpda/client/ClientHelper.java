package forpdateam.ru.forpda.client;

import android.util.Log;

import java.util.Observer;

import forpdateam.ru.forpda.utils.SimpleObservable;

/**
 * Created by radiationx on 26.03.17.
 */

public class ClientHelper {
    private final static String LOG_TAG = ClientHelper.class.getSimpleName();
    public final static boolean AUTH_STATE_LOGIN = true;
    public final static boolean AUTH_STATE_LOGOUT = false;
    private static ClientHelper clientHelper = null;
    private SimpleObservable loginObservables = new SimpleObservable();
    private SimpleObservable countsObservables = new SimpleObservable();
    private static boolean authState = false;
    private static int userId = 0;
    private static int qmsCount = 0, mentionsCount = 0, favoritesCount = 0;

    public static ClientHelper get() {
        if (clientHelper == null) clientHelper = new ClientHelper();
        return clientHelper;
    }

    public void addLoginObserver(Observer observer) {
        loginObservables.addObserver(observer);
    }

    public void removeLoginObserver(Observer observer) {
        loginObservables.deleteObserver(observer);
    }

    public void notifyAuthChanged(boolean authState) {
        ClientHelper.setAuthState(authState);
        loginObservables.notifyObservers(authState);
    }

    public void addCountsObserver(Observer observer) {
        countsObservables.addObserver(observer);
    }

    public void removeCountsObserver(Observer observer) {
        countsObservables.deleteObserver(observer);
    }

    public void notifyCountsChanged() {
        countsObservables.notifyObservers();
    }

    public static boolean getAuthState() {
        Log.d(LOG_TAG, "getAuthState " + authState);
        return authState;
    }

    public static void setAuthState(boolean state) {
        Log.d(LOG_TAG, "setAuthState " + authState);
        authState = state;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(String newUserId) {
        Log.d(LOG_TAG, "setUserId " + newUserId);
        try {
            userId = Integer.parseInt(newUserId);
        } catch (NumberFormatException e) {
            userId = 0;
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
}
