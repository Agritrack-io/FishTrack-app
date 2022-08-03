package io.agritrack.scale.diniargeo;

import android.content.Context;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import java.util.Arrays;

public class ScaleUtils {
    public static int indexOf(byte[] bArr, byte[] bArr2) {
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= (bArr.length - bArr2.length) + 1) {
                return -1;
            }
            int i2 = 0;
            while (true) {
                if (i2 >= bArr2.length) {
                    break;
                } else if (bArr[i + i2] != bArr2[i2]) {
                    z = false;
                    break;
                } else {
                    i2++;
                }
            }
            if (z) {
                return i;
            }
            i++;
        }
    }

    public static boolean endsWith(byte[] bArr, byte[] bArr2) {
        return indexOf(bArr, bArr2) != -1;
    }

    public static byte[] append(byte[] bArr, byte b) {
        int length = bArr.length;
        byte[] copyOf = Arrays.copyOf(bArr, length + 1);
        copyOf[length] = b;
        return copyOf;
    }

    public static byte[] append(byte[] bArr, byte[] bArr2) {
        for (byte append : bArr2) {
            bArr = append(bArr, append);
        }
        return bArr;
    }

    public static int getDecimalsFromString(String str) {
        String replace = str.replace(',', '.');
        if (replace.contains(".")) {
            return replace.substring(replace.indexOf(".") + 1).trim().length();
        }
        return 0;
    }

    public static void Wait(int i) {
        try {
            Thread.sleep((long) i);
        } catch (Exception unused) {
        }
    }

    public static void ShowVirtualKeyboard(Context context) {
        ((InputMethodManager) context.getSystemService("input_method")).toggleSoftInput(2, 0);
    }

    public static void HideVirtualKeyboard(Window window) {
        window.setSoftInputMode(3);
    }
}