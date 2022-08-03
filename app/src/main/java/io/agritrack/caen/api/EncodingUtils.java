package io.agritrack.caen.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class EncodingUtils {
    private static final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);


    /**
     * returns Hex representation of a byte
     * @return
     */
    public static String BytesToHex(final byte b) {
        return BytesToHex(new byte[] {b});
    }

    /**
     * accepts a byte array and converts it to a Hex string
     * @param bytes
     * @return Hex encoded String
     */
    public static String BytesToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02X", (0xFF & b)));
        }

        return sb.toString();
    }

    /**
     * accepts a short number and converts it to a byte array.
     * @param x : short number to be converted to bytes array
     * @return: bytes array
     */
    public static byte[] ToBytes(short x) {
        return new byte[]{(byte) (x >>> 8), (byte) (x & 0xFF)};
    }

    public static short ToShort(byte[] bytes) {
        if(bytes.length>2) {
            bytes = Arrays.copyOf(bytes,2);
            return ByteBuffer.wrap(bytes).getShort();
        } else if (bytes.length==1) {
            return bytes[0];
        } else {
            return ByteBuffer.wrap(bytes).getShort();
        }
    }

    /**
     * converts a Long to a bytes array.
     * used in SetTimestamp, the bytes should by given in REVERSE order!!
     * @param x: long to be converted
     * @return: a bytes array.
     */
    public static byte[] ToBytes(long x) {
        return new byte[]{
                (byte) ((x >> 56) & 0xff),
                (byte) ((x >> 48) & 0xff),
                (byte) ((x >> 40) & 0xff),
                (byte) ((x >> 32) & 0xff),
                (byte) ((x >> 24) & 0xff),
                (byte) ((x >> 16) & 0xff),
                (byte) ((x >> 8) & 0xff),
                (byte) ((x >> 0) & 0xff),
        };
    }


    public static int ToInt(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    public static long ToLong(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getLong();
    }

    public static String parseData(byte[] data) {
        StringBuffer sb = new StringBuffer();
        if (data!=null) {
            for (int i = 0; i < data.length; i += 6) {
                short t = ToShort(new byte[]{data[i], data[i + 1]});

                byte[] bytes = new byte[]{data[i + 4], data[i + 5], data[i + 2], data[i + 3]};
                String ts = BytesToHex(bytes);
                sb.append(parseTemperatureText(t) + "\u2103, " + "\t" + parseTimestamp(bytes) + "\n");
            }
        }
        return sb.toString();
    }

    public static String parseTemperatureText(short t) {
        if (t > 2240) {
            return String.format("%.2f",(double)(t - 8192) / 32d);
        } else {
            return String.format("%.2f",(double)(t / 32d));
        }
    }

    public static Double parseTemperatureNumeric(short t) {
        if (t > 2240) {
            return ((double)(t - 8192) / 32d);
        } else {
            return ((double)(t / 32d));
        }
    }

    public static String parseTimestamp(byte[] b) {
        int l = ToInt(b);
        Date dt = new Date(l*1000l);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(dt);
    }

    public static String createTimestamp(long beginTS) {
        Date dt = new Date(beginTS);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(dt);
    }
}
