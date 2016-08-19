package forpdateam.ru.forpda.utils;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.MainActivity;
import forpdateam.ru.forpda.client.OkHttpResponseException;
import forpdateam.ru.forpda.fragments.TabFragment;

/**
 * Created by RadiationX on 14.08.2016.
 */
public class ErrorHandler {


    public static void handle(TabFragment fragment, Throwable throwable, View.OnClickListener listener) {
        throwable.printStackTrace();
        String text = "";
        boolean isNetworkEx = false;

        Class c = throwable.getClass();

        if (c == SocketTimeoutException.class) {
            text = "TimeOut";
            isNetworkEx = true;
        } else if (c == UnknownHostException.class) {
            text = "Server not available";
            isNetworkEx = true;
        } else if (c == OkHttpResponseException.class) {
            OkHttpResponseException exception = (OkHttpResponseException) throwable;
            text = exception.getName() + " : " + exception.getCode();
            isNetworkEx = true;
        } else {
            text = throwable.getMessage();
        }

        if(isNetworkEx){
            Snackbar snackbar = Snackbar.make(fragment.getCoordinatorLayout(), text, listener != null ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
            if (listener != null)
                snackbar.setAction("Retry", listener);
            snackbar.show();
        }else {
            Toast.makeText(App.getContext(), "Not Network Ex\n"+text, Toast.LENGTH_SHORT).show();
        }

    }
}
