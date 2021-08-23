package io.agritrack.fishtrack.crypto;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static String IV = "IV_VALUE_16_BYTE";
    private static final String PASSWORD = "AgriF!$h-pwd";
    private static final String SALT = "AgriF!$h-s@lt";

    public static String encryptAndEncode(String raw) {
        try {
            Cipher c = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedVal = c.doFinal(getBytes(raw));
            return Base64.encodeToString(encryptedVal, Base64.NO_WRAP);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static String decodeAndDecrypt(String encrypted) throws Exception {
        byte[] decodedValue = Base64.decode(getBytes(encrypted), Base64.NO_WRAP);
        Cipher c = getCipher(Cipher.DECRYPT_MODE);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }

    private String getString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] getBytes(String str) throws UnsupportedEncodingException {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static Cipher getCipher(int mode) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = getBytes(IV);
        c.init(mode, generateKey(), new IvParameterSpec(iv));
        return c;
    }

    private static Key generateKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        char[] password = PASSWORD.toCharArray();
        byte[] salt = getBytes(SALT);

        KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] encoded = tmp.getEncoded();
        return new SecretKeySpec(encoded, "AES");
    }

}
