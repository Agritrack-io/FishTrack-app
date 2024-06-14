package io.agritrack.philosofish.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class DeviceUtils {

    public static String getIMEIDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    // Reflection method
    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals("") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                serialNumber = Build.getSerial();
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;
        } catch (Exception e) {
            serialNumber = "";
        }

        return serialNumber;
    }

    // BUILD method
    public static String getSerialNumber(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getSerialNumber();
        }

        try{
            String serialNumber;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serialNumber = Build.getSerial();
            } else{
                serialNumber = Build.SERIAL;
            }

            if("UNKNOWN".equalsIgnoreCase(serialNumber)){
                return "";
            } else {
                return serialNumber;
            }
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
