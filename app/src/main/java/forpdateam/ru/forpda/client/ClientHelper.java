package forpdateam.ru.forpda.client;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by radiationx on 26.03.17.
 */

public class ClientHelper {
    //Зачем? - Возможно в потом будет изменено на другой тип данных
    public final static boolean AUTH_STATE_LOGIN = true;
    public final static boolean AUTH_STATE_LOGOUT = false;
    private static ClientHelper clientHelper = null;
    private MyObservable loginObservable = new MyObservable();
    private MyObservable observable = new MyObservable();
    private static boolean authState = false;
    private static int userId = 0;
    private static int qmsCount = 0, mentionsCount = 0, favoritesCount = 0;

    public static ClientHelper getInstance() {
        if (clientHelper == null) clientHelper = new ClientHelper();
        return clientHelper;
    }

    public void addLoginObserver(Observer observer) {
        loginObservable.addObserver(observer);
    }

    public void notifyAuthChanged(boolean authState) {

        ClientHelper.setAuthState(authState);
        loginObservable.notifyObservers(authState);
    }

    public void addCountsObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void notifyCountsChanged() {
        observable.notifyObservers();
    }

    public static boolean getAuthState() {
        Log.e("FORPDA_LOG", "getAuthState "+authState);
        return authState;
    }

    public static void setAuthState(boolean state) {
        Log.e("FORPDA_LOG", "NEW AUTH STATE "+authState);
        authState = state;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(String newUserId) {
        Log.d("FORPDA_LOG", "newUserId: " + newUserId);
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

    private class MyObservable extends Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }
}
