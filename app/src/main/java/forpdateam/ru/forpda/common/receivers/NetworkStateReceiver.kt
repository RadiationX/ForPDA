package forpdateam.ru.forpda.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

import forpdateam.ru.forpda.App

/**
 * Created by RadiationX on 13.08.2016.
 */
class NetworkStateReceiver : BroadcastReceiver() {
    private var isConnected: Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        if (!(intent.action != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION)) {
            return
        }
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val newIsConnected: Boolean = cm.activeNetworkInfo?.isConnected ?: false

        if (isConnected != newIsConnected) {
            isConnected = newIsConnected
            App.get().Di().networkState.setState(isConnected)
        }
    }
}
