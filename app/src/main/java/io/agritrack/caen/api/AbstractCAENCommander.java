package io.agritrack.caen.api;

import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_FW_REVISION;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_HW_REVISION;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_INIT_DATE_L;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_INTERVAL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_LAST_SAMPLE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_LOGS;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_SAMPLES_CNT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_STATUS;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TIME_BIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_FOUR;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ZERO;
import static io.agritrack.caen.api.EncodingUtils.ToShort;
import static io.agritrack.caen.api.EncodingUtils.parseTemperatureNumeric;
import static io.agritrack.caen.api.EncodingUtils.parseTimestamp;

import com.android.hdhe.uhf.readerInterface.TagModel;
import com.uhf.api.cls.Reader;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import cn.pda.serialport.Tools;
import io.agritrack.caen.pojo.RFIDTag;

public abstract class AbstractCAENCommander implements ICAEN_API {
    protected final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    protected final Function<Reader.TAGINFO, String> TagInfoToString = t -> Tools.Bytes2HexString(t.EpcId, t.Epclen);
    private static boolean LoggerIsOpen = Boolean.FALSE;

    // ##########################
    // ###  abstract methods  ###
    // ##########################
    abstract public void setFilterEPC(String epc);

    abstract protected byte[] ReadRegisters(short address, short length) throws Exception;

    abstract protected Reader.READER_ERR WriteRegisters(short address, Object data) throws Exception;

    abstract public byte CheckReply();

    abstract public void CloseReader();

    abstract public void StopReading();

    abstract public List<RFIDTag> inventoryRealTime();

    abstract public boolean startReading();

    //########################################################
    //###  Protected Methods called by several subclasses ####
    //########################################################
    protected byte adjustReplyId(byte msgId) throws InterruptedException {
        // check current idmsg value written in reply word and adjust idmsg of next command accordingly
        byte reply = CheckReply();

        if (reply == msgId) {
            msgId++;
            return msgId;
        }
        return reply;
    }

    /* This function loads the command, address, and size parameters in the corresponding registers of tag memory interface. */
    protected byte[] ComposeReadCommand(short command, short address, short size, short reply) {
        byte[] data = new byte[8];

        data[0] = (byte) (command >> 8);
        data[1] = (byte) (command & 0xFF);
        data[2] = (byte) (address >> 8);
        data[3] = (byte) (address & 0xFF);
        data[4] = (byte) (size >> 8);
        data[5] = (byte) (size & 0xFF);
        data[6] = (byte) (reply >> 8);
        data[7] = (byte) (reply & 0xFF);

        return data;
    }

    protected byte[] ComposeWriteCommand(short command, short address, short size, short reply, Object value) {
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
        return data;
    }

    //#############################################
    //###  Read / Write to Registers commands  ####
    //#############################################
    /* This function enables TimeBin-One */
    @Override
    public Reader.READER_ERR WriteTimeBinONE() {
        try {
            return WriteRegisters(ADDR_TIME_BIN, SHORT_ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function sets the current epoch timestamp */
    @Override
    public Reader.READER_ERR WriteCurrentDatetime() {
        try {
            long unixTime = System.currentTimeMillis() / 1000L;
            Reader.READER_ERR rs = WriteRegisters(ADDR_TIMESTAMP, unixTime);
            return rs;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function sets the sampling interval. */
    @Override
    public Reader.READER_ERR WriteInterval(Short interval) {
        try {
            short _interval = interval == null ? DefaultInterval : interval;
            return WriteRegisters(ADDR_INTERVAL, _interval);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function starts Logging. */
    @Override
    public Reader.READER_ERR EnableLogging() {
        try {
            return WriteRegisters(ADDR_CONTROL, SHORT_FOUR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function starts Logging. */
    @Override
    public Reader.READER_ERR DisableLogging() {
        try {
            return WriteRegisters(ADDR_CONTROL, SHORT_ZERO);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function sets Logger to HIGH sensitivity mode. */
    @Override
    public Reader.READER_ERR HighSensitivity() {
        try {
            Short bits = Short.valueOf("0014", 16);
            return WriteRegisters(ADDR_CONTROL, bits);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function sets Logger to LOW sensitivity mode. */
    @Override
    public Reader.READER_ERR LowSensitivity() {
        try {
            Short bits = Short.valueOf("0004", 16);
            return WriteRegisters(ADDR_CONTROL, bits);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //##################################################
    //###  Public methods for Read / Write commands  ###
    //##################################################
    /* This function returns in one step, the LastSampleMeasurement and the Samples Count */
    @Override
    public Object[] ReadSamplesCntAndLastValue() {
        Double lastSampleValue = 0.0d;
        Short samplesCnt = 0;
        try {
            byte[] sampleRS = ReadRegisters(ADDR_LAST_SAMPLE, SHORT_TWO);
            if (sampleRS != null && sampleRS.length == 4) {
                lastSampleValue = parseTemperatureNumeric(ToShort(new byte[]{sampleRS[0], sampleRS[1]}));
                samplesCnt = ToShort(new byte[]{sampleRS[2], sampleRS[3]});
            }
            return new Object[]{lastSampleValue, samplesCnt};
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the CONTROL register bits value */
    @Override
    public String ReadControlRegister() {
        try {
            byte[] ctrlRS = ReadRegisters(ADDR_CONTROL, SHORT_ONE);
            return String.format("%5s", Integer.toBinaryString(ToShort(ctrlRS))).replace(' ', '0');
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /* This function returns the STATUS register bits value */
    @Override
    public String ReadStatusRegister() {
        try {
            byte[] statusRS = ReadRegisters(ADDR_STATUS, SHORT_ONE);
            return String.format("%8s", Integer.toBinaryString(ToShort(statusRS))).replace(' ', '0');
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /* This function returns the READ_FW_REVISION value */
    @Override
    public String ReadFWRevision() {
        try {
            byte[] fwRevRS = ReadRegisters(ADDR_FW_REVISION, SHORT_ONE);
            if (fwRevRS.length == 2) {
                return String.format("%s.%s", fwRevRS[0], fwRevRS[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_HW_REVISION value */
    @Override
    public String ReadHWRevision() {
        try {
            byte[] hwRevRS = ReadRegisters(ADDR_HW_REVISION, SHORT_ONE);
            if (hwRevRS.length == 2) {
                return String.format("%s.%s", hwRevRS[0], hwRevRS[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_INIT_DATETIME value */
    @Override
    public String ReadInitDatetime() {
        try {
            byte[] aa = ReadRegisters(ADDR_INIT_DATE_L, SHORT_TWO);
            return parseTimestamp(new byte[]{aa[2], aa[3], aa[0], aa[1]});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_TIME_BIN value */
    @Override
    public Short ReadTimeBIN() {
        try {
            byte[] rs = ReadRegisters(ADDR_TIME_BIN, SHORT_ONE);
            return ToShort(rs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_INTERVAL value */
    @Override
    public Short ReadInterval() {
        try {
            byte[] rs = ReadRegisters(ADDR_INTERVAL, SHORT_ONE);
            return ToShort(rs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_SAMPLES_COUNT value */
    @Override
    public Double ReadLastSample() {
        try {
            byte[] rs = ReadRegisters(ADDR_LAST_SAMPLE, SHORT_ONE);
            if(rs==null || rs.length==1) {
                return null;
            } else {
                return parseTemperatureNumeric(ToShort(rs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the READ_SAMPLES_COUNT value */
    @Override
    public Short ReadSamplesCount() {
        try {
            byte[] aa = ReadRegisters(ADDR_SAMPLES_CNT, SHORT_ONE);
            return ToShort(aa);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String[]> ReadSamples(int samplesCnt) throws Exception {
        if (samplesCnt <= SampleBatchSize) {
            return ReadSamplesBatch(SHORT_ZERO, samplesCnt);
        } else {
            List<String[]> result = new LinkedList<>();
            for (short batchStart = 0; batchStart < samplesCnt; batchStart += SampleBatchSize) {
                short batchSize = (samplesCnt - batchStart) >= SampleBatchSize ? SampleBatchSize : (short) (samplesCnt % SampleBatchSize);
                List<String[]> batch = ReadSamplesBatch((short) (batchStart * 3), batchSize);
                result.addAll(batch);
                Thread.sleep(200l);
            }
            return result;
        }
    }

    @Override
    public Reader.READER_ERR Setup(short interval) {
        WriteTimeBinONE();
        return WriteInterval(interval);
    }

    @Override
    public Double StartLogging() throws Exception {
        EnableLogging();
        return ReadLastSample();
    }

    @Override
    public void Status(boolean open){
        LoggerIsOpen = open;
    }

    @Override
    public boolean IsOpen() {
        return LoggerIsOpen;
    }

    //###################################################
    //###  Private methods for Read / Write commands  ###
    //###################################################
    private List<String[]> ReadSamplesBatch(short start, int samplesCnt) throws Exception {
        byte[] reply = ReadRegisters((short) (ADDR_LOGS + start), (short) (samplesCnt * 3));
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read sample data.");
        else if (reply == null || reply.length < 6)
            return new LinkedList<>();
        return parseData(reply);
    }

    private List<String[]> parseData(byte[] data) {
        List<String[]> measurements = new LinkedList<>();

        for (int i = 0; i < data.length - 5; i += 6) {
            short t = ToShort(new byte[]{data[i], data[i + 1]});
            byte[] bytes = new byte[]{data[i + 4], data[i + 5], data[i + 2], data[i + 3]};
            measurements.add(new String[]{parseTimestamp(bytes), parseTemperatureNumeric(t) + "\u2103"});
        }
        return measurements;
    }
}
