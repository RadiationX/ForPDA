package forpdateam.ru.forpda.ui.fragments.devdb.device;

import android.support.v4.app.Fragment;

import forpdateam.ru.forpda.api.devdb.models.Device;

/**
 * Created by radiationx on 09.08.17.
 */

public class SubDeviceFragment extends Fragment {
    protected Device device;

    public Device getDevice() {
        return device;
    }

    public SubDeviceFragment setDevice(Device device) {
        this.device = device;
        return this;
    }
}
