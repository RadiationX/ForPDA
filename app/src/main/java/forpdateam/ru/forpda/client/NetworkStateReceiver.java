package forpdateam.ru.forpda.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by RadiationX on 13.08.2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("app", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("app", "Network " + ni.getTypeName() + " connected");
                Client.getInstance().notifyNetworkObservers(true);
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("app", "There's no network connectivity");
                Client.getInstance().notifyNetworkObservers(false);
            }
        }
    }
}
