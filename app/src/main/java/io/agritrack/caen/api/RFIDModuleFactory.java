package io.agritrack.caen.api;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;
import com.handheld.uhfr.UHFRManager;

import io.agritrack.ui.service.LocalPreferences;

public class RFIDModuleFactory {
    private static ICAEN_API instance = null;

    private RFIDModuleFactory() {
    }

    private static String getLocalInfo() {
        return LocalPreferences.getDeviceModel();
    }

    private static String detectModel() {
        try {
            String model = getLocalInfo();
            if(!Strings.isEmptyOrWhitespace(model)) {
                return model;
            } else {
                if (UHFRManager.getInstance() != null) {
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6100");
                    return "BX6100";
                } else if (UhfReader.getInstance() != null) {
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6200");
                    return "BX6200";
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ICAEN_API getInstance() {
        if (instance == null || !instance.IsOpen()) {
            String model = detectModel();
            if ("BX6100".equalsIgnoreCase(model)) {
                instance = new BX6100Commander();
                instance.Status(Boolean.TRUE);
            } else if ("BX6200".equalsIgnoreCase(model)) {
                instance = new BX6200Commander();
                instance.Status(Boolean.TRUE);
            }
        }
        return instance;
    }
}
