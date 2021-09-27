package io.agritrack.fishtrack.caen.common;

import com.android.hdhe.uhf.reader.UhfReader;

import io.agritrack.fishtrack.caen.api.EncodingUtils;

import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.ADDR_COMMAND;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.ADDR_DATA;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.ADDR_REPLY;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.ADDR_TRIGGER;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.CMDBANK;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.TRIGBANK;

public class INTERFACEMEM {

    /* This function loads the command, address, and size parameters in the corresponding registers of tag memory interface. */
    public static boolean SetReadCommand(UhfReader reader, short command, short wordAddress, short words, byte[] accessPassword) {
        byte[] data = new byte[6];

        data[0] = (byte) (command >> 8);
        data[1] = (byte) (command & 0xFF);
        data[2] = (byte) (wordAddress >> 8);
        data[3] = (byte) (wordAddress & 0xFF);
        data[4] = (byte) (words >> 8);
        data[5] = (byte) (words & 0xFF);

        try {
            return reader.writeTo6C(accessPassword, CMDBANK, ADDR_COMMAND, (short) data.length, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /* This function loads the command, address, and size parameters in the corresponding registers of tag memory interface. */
    public static String SetWriteCommand(UhfReader reader, short command, short address, short size, short reply, Object value, byte[] accessPassword) {
        byte[] data;
        if (value instanceof Short) {
            short _val = (short) value;

            data = new byte[10];
            data[0] = (byte) (command >> 8);
            data[1] = (byte) (command & 0xFF);
            data[2] = (byte) (address >> 8);
            data[3] = (byte) (address & 0xFF);
            data[4] = (byte) (size >> 8);
            data[5] = (byte) (size & 0xFF);
            data[6] = (byte) (reply >> 8);
            data[7] = (byte) (reply & 0xFF);
            data[8] = (byte) (_val >> 8);
            data[9] = (byte) (_val & 0xFF);
        } else {
            long _val = (long) value;
            short highVal = (short) (_val >> 16);
            short lowVal = (short) (_val & 0xFFFF);

            data = new byte[12];
            data[0] = (byte) (command >> 8);
            data[1] = (byte) (command & 0xFF);
            data[2] = (byte) (address >> 8);
            data[3] = (byte) (address & 0xFF);
            data[4] = (byte) (size >> 8);
            data[5] = (byte) (size & 0xFF);
            data[6] = (byte) (reply >> 8);
            data[7] = (byte) (reply & 0xFF);
            data[8] = (byte) (lowVal >> 8);
            data[9] = (byte) (lowVal & 0xFF);
            data[10] = (byte) (highVal >> 8);
            data[11] = (byte) (highVal & 0xFF);
        }

        String cmsTring = EncodingUtils.BytesToHex(data);

        try {
            return cmsTring + ":" + reader.writeTo6C(accessPassword, CMDBANK, ADDR_COMMAND, (short) data.length, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cmsTring + ":" + false;
    }


    /* This function triggers the tag parsing and execution of a command */
    public static void Trigger(UhfReader reader, byte[] accessPassword) {
        try {
            reader.readFrom6C(TRIGBANK, ADDR_TRIGGER, (short) 0x0001, accessPassword);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /* This function reads the DATA area of the tag memory interface. */
    public static byte[] ReadData(UhfReader reader, short address, short bytes, byte[] accessPassword) {
        byte[] data = null;
        try {
            data = reader.readFrom6C(CMDBANK, (short) (ADDR_DATA + address), bytes, accessPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    /* This function reads the REPLY register of the tag memory interface */
    public static byte[] ReadReply(UhfReader reader, byte[] accessPassword) {
        byte[] data = null;
        try {
            data = reader.readFrom6C(CMDBANK, ADDR_REPLY, (short) 1, accessPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
