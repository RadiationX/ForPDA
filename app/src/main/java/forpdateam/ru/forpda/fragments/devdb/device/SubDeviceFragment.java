package forpdateam.ru.forpda.fragments.devdb.device;

import android.support.v4.app.Fragment;

import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.fragments.TabFragment;

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
