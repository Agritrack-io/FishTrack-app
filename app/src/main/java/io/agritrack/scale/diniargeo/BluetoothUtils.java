package io.agritrack.scale.diniargeo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothUtils {
    private static byte[] Read_Buffer;

    @SuppressLint("MissingPermission")
    public static void Switch(boolean z) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        if (z) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        } else if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothSocket CreateSocket(BluetoothDevice bluetoothDevice) {
        try {
            return (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(bluetoothDevice, new Object[]{1});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    public static BluetoothSocket CreateSocketUnsecure(BluetoothDevice bluetoothDevice) {
        try {
            return bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean Send(OutputStream outputStream, byte[] bArr) {
        return Send(outputStream, bArr, CommunicationParameters.getSendTimeout());
    }

    public static boolean Send(final OutputStream outputStream, final byte[] bArr, int i) {
        final boolean[] zArr = {false};
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    outputStream.write(bArr);
                    zArr[0] = true;
                } catch (Exception unused) {
                }
            }
        });
        thread.start();
        try {
            thread.join((long) i);
            if (thread.isAlive()) {
                Log.wtf("BLUETOOTH SOCKET SEND", "TIMEOUT");
            }
        } catch (InterruptedException unused) {
        }
        return zArr[0];
    }

    public static byte[] Read(InputStream inputStream) {
        return Read(inputStream, CommunicationParameters.getReadTimeout());
    }

    public static byte[] Read(final InputStream inputStream, int i) {
        Read_Buffer = new byte[0];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] bArr = new byte[2048];
                    byte[] unused = BluetoothUtils.Read_Buffer = Arrays.copyOf(bArr, inputStream.read(bArr) + 1);
                } catch (Exception unused2) {
                }
            }
        });
        thread.start();
        try {
            thread.join((long) i);
        } catch (InterruptedException unused) {
        }
        return Read_Buffer;
    }

    @SuppressLint("MissingPermission")
    public static boolean isPaired(Context context, BluetoothDevice bluetoothDevice) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            return defaultAdapter.getBondedDevices().contains(bluetoothDevice);
        }
        return false;
    }

    public static void Pair(Context context, BluetoothDevice bluetoothDevice) {
        PairDevice_Procedura(context, bluetoothDevice);
    }

    public static void PairDevice_Procedura(Context context, final BluetoothDevice bluetoothDevice) {
        if (!isPaired(context, bluetoothDevice)) {
            new Handler().post(new Runnable() {
                public void run() {
                    try {
                        bluetoothDevice.getClass().getMethod("createBond", (Class[]) null).invoke(bluetoothDevice, (Object[]) null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
