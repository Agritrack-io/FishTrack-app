package io.agritrack.caen.api;

import androidx.appcompat.app.AppCompatActivity;

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
        UHFRManager instBX6100 = null;
        UhfReader instBX6200 = null;
        UhfReader instZEBRA_TC26 = null;
        try {
            String model = getLocalInfo();
            if (!Strings.isEmptyOrWhitespace(model)) {
                return model;
            } else {
                try {
                    instBX6100 = UHFRManager.getInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    instBX6200 = UhfReader.getInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (instBX6100 != null) {
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6100");
                    return "BX6100";
                } else if (instBX6200 != null) {
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "BX6200");
                    return "BX6200";
                } else { //TODO: we assume that it is Zebra terminal!!
                    LocalPreferences.writeValue(LocalPreferences.Device_Key, "RFID_Zebra_TC26");
                    return "RFID_Zebra_TC26";
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

    public static ICAEN_API getInstance(ZebraTC26Commander.ResponseHandlerInterface ctx) {
        if (instance == null || !instance.IsOpen()) {
            String model = detectModel();
            if ("RFID_Zebra_TC26".equalsIgnoreCase(model)) {
                instance = new ZebraTC26Commander((AppCompatActivity) ctx);
                instance.Status(Boolean.TRUE);
            }
        }
        return instance;
    }

    public static void Reset() {
        instance = null;
    }
}
