package io.agritrack.crypto;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoLicense {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    private static final String IV = "IV_VALUE_16_BYTE";
    private static final String PASSWORD = "ETL2IdRzPcaA";
    private static final String SALT = "CTFh_pHjND6H";

    /**
     * static method used to decrypt the given text.
     *
     * @param encrypted, the encrypted text that will be decrypted.
     * @return the decrypted text.
     */
    public static String decodeAndDecrypt(String encrypted) {
        try {
            byte[] decodedValue = Base64.decode(getBytes(encrypted), Base64.DEFAULT);
            Cipher c = getCipher(Cipher.DECRYPT_MODE, PASSWORD);
            byte[] decValue = c.doFinal(decodedValue);
            return new String(decValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static Cipher getCipher(int mode, String str) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = getBytes(IV);
        c.init(mode, generateKey(str), new IvParameterSpec(iv));
        return c;
    }

    private static Key generateKey(String str) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        char[] password = str.toCharArray();
        byte[] salt = getBytes(SALT);

        KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] encoded = tmp.getEncoded();
        return new SecretKeySpec(encoded, "AES");
    }

    /**
     * Checks if the key provided, corresponds to the terminalId and has not expired.
     *
     * @param encryptedKey, the encrypted license key,
     * @param terminalId,   the deviceId
     * @return True if key is valid, False otherwise or if some of the input parameters are null or empty..
     */
    public static boolean isLicenseKeyValid(String encryptedKey, String terminalId) {
        try {
            String raw = decodeAndDecrypt(encryptedKey);
            if (!TextUtils.isEmpty(raw) && raw.length() > 13 && !TextUtils.isEmpty(terminalId)) {
                String[] parts = raw.substring(4).split("\\.");
                if (parts.length == 2) {
                    Date expirationDate = format.parse(parts[0]);
                    if (expirationDate.after(new Date()) && terminalId.equalsIgnoreCase(parts[1])) {
                        return true;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
