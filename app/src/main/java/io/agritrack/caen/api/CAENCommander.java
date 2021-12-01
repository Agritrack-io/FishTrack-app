package io.agritrack.caen.api;

import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_INTERVAL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_SAMPLES_CNT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIME_BIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_FOUR;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_TWO;

import com.android.hdhe.uhf.reader.UhfReader;

import cn.pda.serialport.Tools;
import io.agritrack.caen.common.CAENRegistersIO;

public class CAENCommander {
    private static final Short numOfSamples = Short.valueOf("0");
    private final byte[] accessPassword = Tools.HexString2Bytes("00000000");
    private final UhfReader _uhfReader;


    public CAENCommander() {
        _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);
        _uhfReader.setOutputPower(24);
    }

    public void RESET() throws Exception {
        byte reply = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_CONTROL, SHORT_ONE, SHORT_ONE, accessPassword);
        if (reply != REPLY_NACK)
            throw new Exception("Failed to reset data logger.");
    }

    public void WRITE_CURRENT_DATETIME() throws Exception {
        long unixTime = System.currentTimeMillis() / 1000L;
        byte reply = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIMESTAMP, SHORT_TWO, unixTime, accessPassword);
        if (reply != REPLY_NACK)
            throw new Exception("Failed to set timestamp.");
    }

    public void WRITE_CURRENT_TIME_BIN() throws Exception {
        byte reply = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_TIME_BIN, SHORT_ONE, SHORT_ONE, accessPassword);
        if (reply != REPLY_NACK)
            throw new Exception("Failed to set time bin.");
    }

    public void WRITE_INTERVAL(Short interval) throws Exception {
        short _interval = interval == null ? 30 : interval;
        byte reply = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_INTERVAL, SHORT_ONE, _interval, accessPassword);
        if (reply != REPLY_NACK)
            throw new Exception("Failed to set interval.");
    }

    public void ENABLE_LOGGING() throws Exception {
        byte reply = CAENRegistersIO.WriteRegisters(_uhfReader, ADDR_CONTROL, SHORT_ONE, SHORT_FOUR, accessPassword);
        if (reply != REPLY_NACK)
            throw new Exception("Failed to start logger.");
    }


    public short READ_SAMPLES_COUNT() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_SAMPLES_CNT, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to start logger.");

        return EncodingUtils.ToShort(reply);
    }

    public short READ_REVISION() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_CONTROL, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to start logger.");

        return EncodingUtils.ToShort(reply);
    }

    public long READ_CURRENT_DATETIME() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_TIMESTAMP, SHORT_TWO, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read timestamp.");

        return EncodingUtils.ToLong(reply);
    }

    public short READ_SAMPLES(int samplesCnt) throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(_uhfReader, ADDR_TIMESTAMP, SHORT_TWO, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read sample data.");

        return EncodingUtils.ToShort(reply);
    }
}
