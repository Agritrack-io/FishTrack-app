package io.agritrack.caen.api;

public class CAEN_CONSTANTS {
    public final static short CMDBANK = 3;
    public final static short TRIGBANK = 1;

    public final static short ADDR_TRIGGER = 0x001F; /* byte address */
    public final static short ADDR_COMMAND = 0x0000;
    public final static short ADDR_ADDRESS = (short) (ADDR_COMMAND + 1);
    public final static short ADDR_SIZE = (short) (ADDR_ADDRESS + 1);
    public final static short ADDR_REPLY = (short) (ADDR_SIZE + 1);
    public final static short ADDR_DATA = (short) (ADDR_REPLY + 1);

    public final static short ADDR_CONTROL = (short) 0x000A;
    public final static short ADDR_TIMESTAMP = (short) 0x000C;
    public final static short ADDR_INIT_DATE_L = (short) 0x000C;
    public final static short ADDR_INIT_DATE_H = (short) 0x000D;
    public final static short ADDR_TIME_BIN = (short) 0x0012;
    public final static short ADDR_INTERVAL = (short) 0x0023;
    public final static short ADDR_LOGS = (short) 0x008A;
    public final static short ADDR_LAST_SAMPLE = (short) 0x0066;
    public final static short ADDR_SAMPLES_CNT = (short) 0x0067;

    public final static short ADDR_FW_REVISION = (short) 0x0008;
    public final static short ADDR_HW_REVISION = (short) 0x0009;
    public final static short ADDR_STATUS = (short) 0x0051;

    public final static short MAXBYTESIZEDATA = 200 * 2;
    public final static byte CMD_READ = 0x12;
    public final static byte CMD_WRITE = 0x13;
    public final static byte REPLY_ACK = (byte) 0xAC;
    public final static byte REPLY_NACK = (byte) 0xFC;

    public final static int TIME_WAITTAG_CMDWRITE = 700;
    public final static int TIME_WAITTAG_CMDREADBASE = 100;
    public final static int TIME_WAITTAG_WRITEPAGE = 10;

    public final static short SHORT_ZERO = (short) 0x0000;
    public final static short SHORT_ONE = (short) 0x0001;
    public final static short SHORT_TWO = (short) 0x0002;
    public final static short SHORT_THREE = (short) 0x0003;
    public final static short SHORT_FOUR = (short) 0x0004;
    public final static short SHORT_SIX = (short) 0x0006;
}
