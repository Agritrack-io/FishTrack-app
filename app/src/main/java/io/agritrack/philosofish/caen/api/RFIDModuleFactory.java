package io.agritrack.philosofish.caen.api;

import android.util.Log;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;
import com.handheld.uhfr.UHFRManager;

import io.agritrack.philosofish.ui.service.LocalPreferences;

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

            Log.e("RFID_FACTORY", "Stored model = " + model);

            if (!Strings.isEmptyOrWhitespace(model)) {
                Log.e("RFID_FACTORY", "Using stored model → " + model);
                return model;
            }

            // TRY BX6100
            try {
                Log.e("RFID_FACTORY", "Trying UHFRManager (BX6100)");
                UHFRManager manager = UHFRManager.getInstance();

                if (manager != null) {
                    Log.e("RFID_FACTORY", "Detected BX6100");
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6100");
                    return "BX6100";
                } else {
                    Log.e("RFID_FACTORY", "UHFRManager = NULL");
                }
            } catch (Throwable ex) {
                Log.e("RFID_FACTORY", "UHFRManager ERROR: " + ex.getMessage());
            }

            // TRY BX6200
            try {
                Log.e("RFID_FACTORY", "Trying UhfReader (BX6200)");
                UhfReader reader = UhfReader.getInstance();

                if (reader != null) {
                    Log.e("RFID_FACTORY", "Detected BX6200");
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6200");
                    return "BX6200";
                } else {
                    Log.e("RFID_FACTORY", "UhfReader = NULL");
                }
            } catch (Throwable ex) {
                Log.e("RFID_FACTORY", "UhfReader ERROR: " + ex.getMessage());
            }

        } catch (Throwable ex) {
            Log.e("RFID_FACTORY", "detectModel ERROR: " + ex.getMessage());
        }

        Log.e("RFID_FACTORY", "Detection FAILED → returning null");
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

    public static void Reset() {
        instance = null;
    }
}
