package forpdateam.ru.forpda.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import forpdateam.ru.forpda.App;

/**
 * Created by RadiationX on 13.08.2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";
    private final IntentFilter mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    private final Context context;
    private ConnectivityManagerDelegate managerDelegate;
    private boolean mRegistered;
    private NetworkInfo.State mConnectionType;

    public NetworkStateReceiver(){
        context = App.getContext();
        managerDelegate = new ConnectivityManagerDelegate(context);
        mConnectionType = getCurrentConnectionType();
    }

    public NetworkStateReceiver(Context context) {
        this.context = context.getApplicationContext();
        managerDelegate = new ConnectivityManagerDelegate(this.context);
        mConnectionType = getCurrentConnectionType();
    }

    public void registerReceiver() {
        if (!mRegistered) {
            mRegistered = true;
            context.registerReceiver(this, mIntentFilter);
        }
    }

    public void unregisterReceiver() {
        if (mRegistered) {
            mRegistered = false;
            context.unregisterReceiver(this);
        }
    }

    public NetworkInfo.State getCurrentConnectionType() {
        if (!managerDelegate.activeNetworkExists() ||
                !managerDelegate.isConnected()) {
            return NetworkInfo.State.DISCONNECTED;
        }

        switch (managerDelegate.getNetworkType()) {
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_MOBILE:
                return NetworkInfo.State.CONNECTED;
            default:
                return NetworkInfo.State.UNKNOWN;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        NetworkInfo.State newConnectionType = getCurrentConnectionType();
        if (newConnectionType == mConnectionType) return;

        mConnectionType = newConnectionType;
        Log.d(TAG, "Network connectivity changed, type is: " + mConnectionType);

        if (mConnectionType == NetworkInfo.State.CONNECTED) {
            Client.getInstance().notifyNetworkObservers(true);
        } else {
            Client.getInstance().notifyNetworkObservers(false);
        }
    }

    private static class ConnectivityManagerDelegate {
        private final ConnectivityManager manager;

        ConnectivityManagerDelegate(Context context) {
            manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        boolean activeNetworkExists() {
            return manager.getActiveNetworkInfo() != null;
        }

        boolean isConnected() {
            return manager.getActiveNetworkInfo().isConnected();
        }

        int getNetworkType() {
            return manager.getActiveNetworkInfo().getType();
        }
    }
}
