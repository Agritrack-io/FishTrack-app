package io.agritrack.kefalonia.caen.api;

import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_BIN_ENABLE_COUNTER;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_BIN_ENA_SAMPLE_STORE;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_BIN_ENA_TIME_STORE;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_BIN_HLIMIT_0;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_FW_REVISION;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_HW_REVISION;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_INIT_DATE_L;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_INTERVAL;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_LAST_SAMPLE;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_LOGS;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_SAMPLES_CNT;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_SHIPPING_DATE_L;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_STATUS;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_STOP_DATE_L;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_TIMESTAMP;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.ADDR_TIME_BIN;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_FOUR;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_SEVENTY;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_SIX;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_THREE;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.kefalonia.caen.api.CAEN_CONSTANTS.SHORT_ZERO;
import static io.agritrack.kefalonia.caen.api.EncodingUtils.ToShort;
import static io.agritrack.kefalonia.caen.api.EncodingUtils.createTimestamp;
import static io.agritrack.kefalonia.caen.api.EncodingUtils.parseTemperatureNumeric;
import static io.agritrack.kefalonia.caen.api.EncodingUtils.parseTimestamp;
import static io.agritrack.kefalonia.rfid.RFIDUtils.WaitFor;

import com.android.hdhe.uhf.readerInterface.TagModel;
import com.uhf.api.cls.Reader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import cn.pda.serialport.Tools;
import io.agritrack.kefalonia.caen.pojo.RFIDTag;
import io.agritrack.kefalonia.data.model.TempSample;

public abstract class AbstractCAENCommander implements ICAEN_API {
    private static final short WORDS_PER_MEASUREMENT = (short) 1; //(short)3;
    private static boolean LoggerIsOpen = Boolean.FALSE;
    protected final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    protected final Function<Reader.TAGINFO, String> TagInfoToString = t -> Tools.Bytes2HexString(t.EpcId, t.Epclen);

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // ##########################
    // ###  abstract methods  ###
    // ##########################
    abstract public void setFilterEPC(String epc);

    abstract public boolean clearEPCFilter();

    abstract protected byte[] ReadRegisters(short address, short length) throws Exception;

    abstract protected Reader.READER_ERR WriteRegisters(short address, Object data) throws Exception;

    abstract public byte CheckReply();

    abstract public void CloseReader();

    abstract public void StopReading();

    abstract public List<RFIDTag> inventoryRealTime();

    abstract public List<RFIDTag> inventoryByTimer();

    abstract public List<RFIDTag> searchInventory();

    abstract public boolean startReading();

    abstract public boolean startSearching();

    abstract public List<RFIDTag> search();

    abstract public boolean stopSearching();

    abstract public Reader.READER_ERR HighPowerLevel();

    abstract public Reader.READER_ERR LowPowerLevel();

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

    @Override
    public Reader.READER_ERR WriteTimeBinZERO() {
        try {
            return WriteRegisters(ADDR_TIME_BIN, SHORT_ZERO);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Reader.READER_ERR writeHLimitZERO() {
        try {
            return WriteRegisters(ADDR_BIN_HLIMIT_0, SHORT_SEVENTY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Reader.READER_ERR writeBinEnableCounter() {
        try {
            return WriteRegisters(ADDR_BIN_ENABLE_COUNTER, SHORT_ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Reader.READER_ERR writeBinEnaSampleStore() {
        try {
            return WriteRegisters(ADDR_BIN_ENA_SAMPLE_STORE, SHORT_ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Override
    public Reader.READER_ERR writeBinEnaTimeStore() {
        try {
            return WriteRegisters(ADDR_BIN_ENA_TIME_STORE, SHORT_ZERO);
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
            //Short bits = 0x10; //Short bits = 0x14; //Short.valueOf("0010", 16); // High sensitivity and KEEP LOGGING!!!!
            byte[] address = ReadRegisters(ADDR_CONTROL, SHORT_ONE);
            Short currState = EncodingUtils.ToShort(address);
            currState = (short) (currState | (1 << 4));
            Reader.READER_ERR rs = WriteRegisters(ADDR_CONTROL, currState);
            return rs;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Reader.READER_ERR LowSensitivity() {
        try {
            Short bits = 0x04; //Short.valueOf("0000", 16); // Low sensitivity and KEEP LOGGING!!!!
            Reader.READER_ERR rs = WriteRegisters(ADDR_CONTROL, bits);
            return rs;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //##################################################
    //###  Public methods for Read / Write commands  ###
    //##################################################
    @Override
    public void Wait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

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
            String binaryText = Integer.toBinaryString(ToShort(ctrlRS));
            binaryText = binaryText.length() > 5 ? binaryText.substring(0, 5) : binaryText;
            //if(!"0004".equalsIgnoreCase(hexRS))
            return String.format("%5s", binaryText).replace(' ', '0');
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
            if (statusRS == null || statusRS.length == 1) {
                return "N/A";
            }
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
        return "N/A";
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
        return "N/A";
    }

    /* This function returns the READ_FW_REVISION together with READ_HW_REVISION value */
    @Override
    public String[] ReadCTRLRevisions() {
        try {
            byte[] revRS = ReadRegisters(ADDR_FW_REVISION, SHORT_THREE);
            if (revRS.length == 6) {
                String binaryText = Integer.toBinaryString(ToShort(Arrays.copyOfRange(revRS, 4, 5)));
                binaryText = binaryText.length() > 5 ? binaryText.substring(0, 5) : binaryText;
                return new String[]{String.format("%s.%s", revRS[0], revRS[1]), String.format("%s.%s", revRS[2], revRS[3]), String.format("%5s", binaryText).replace(' ', '0')};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String[]{"N/A", "N/A", "N/A"};
    }

    /* This function returns the READ_INIT_DATETIME value */
    @Override
    public String ReadInitDatetime() {
        try {
            byte[] rs = ReadRegisters(ADDR_INIT_DATE_L, SHORT_TWO);
            if (rs.length == 4) {
                return parseTimestamp(new byte[]{rs[2], rs[3], rs[0], rs[1]});
            }
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

    /* This function returns the LAST_SAMPLE value */
    @Override
    public Double ReadLastSample() {
        try {
            byte[] rs = ReadRegisters(ADDR_LAST_SAMPLE, SHORT_ONE);
            if (rs == null || rs.length == 1) {
                return null;
            } else {
                return parseTemperatureNumeric(ToShort(rs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the ADDR_SAMPLES_CNT value */
    @Override
    public Short ReadSamplesCount() {
        try {
            byte[] aa = ReadRegisters(ADDR_SAMPLES_CNT, SHORT_ONE);
            if (aa == null || aa.length == 1) //sometimes returns {9}
                return -1;
            return ToShort(aa);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /* This function returns the ADDR_SHIPPING_DATE value */
    @Override
    public String ReadShippingDatetime() {
        try {
            byte[] rs = ReadRegisters(ADDR_SHIPPING_DATE_L, SHORT_TWO);
            if (rs.length == 4) {
                return parseTimestamp(new byte[]{rs[2], rs[3], rs[0], rs[1]});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns the ADDR_STOP_DATE value */
    @Override
    public String ReadStopDatetime() {
        try {
            byte[] rs = ReadRegisters(ADDR_STOP_DATE_L, SHORT_TWO);
            if (rs.length == 4) {
                return parseTimestamp(new byte[]{rs[2], rs[3], rs[0], rs[1]});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* This function returns: LAST_SAMPLE_VALUE, SAMPLES_NUM, SHIPPING_DATE, STOP_DATE */
    public String[] ReadSamplesInfo() {
        try {
            byte[] rs = ReadRegisters(ADDR_LAST_SAMPLE, SHORT_SIX);
            if (rs.length == 12) {

                Double lastSampleVal = parseTemperatureNumeric(ToShort(Arrays.copyOfRange(rs, 0, 1)));
                Short samplesCnt = ToShort(Arrays.copyOfRange(rs, 2, 3));
                String shippingDate = parseTimestamp(Arrays.copyOfRange(rs, 4, 7));
                String stopDate = parseTimestamp(Arrays.copyOfRange(rs, 8, 11));

                return new String[]{lastSampleVal.toString(), samplesCnt.toString(), shippingDate, stopDate};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String[]{"N/A", "N/A", "N/A", "N/A"};
    }

    @Override
    public List<TempSample> ReadSamplesWithInitTime(int samplesCnt, long initedAt) throws Exception {
        return ReadSamples(samplesCnt, DefaultInterval, initedAt);
    }

    @Override
    public List<TempSample> ReadSamples(int samplesCnt) throws Exception {
        return ReadSamples(samplesCnt, DefaultInterval);
    }

    @Override
    public List<TempSample> ReadSamples(int samplesCnt, int intervalSeconds) throws Exception {
        long startTSmSec = (long) (System.currentTimeMillis() - (samplesCnt * intervalSeconds * 1000L));
        return ReadSamples(samplesCnt, intervalSeconds, startTSmSec);
    }

    @Override
    public List<TempSample> ReadSamples(int samplesCnt, int intervalSeconds, long startTSmSec) throws Exception {
        if (samplesCnt <= SampleBatchSize) {
            return ReadSamplesBatch(startTSmSec, intervalSeconds, SHORT_ZERO, samplesCnt);
        } else {
            List<TempSample> result = new LinkedList<>();
            for (short batchStart = 0; batchStart < samplesCnt; batchStart += SampleBatchSize) {
                short batchSize = (samplesCnt - batchStart) >= SampleBatchSize ? SampleBatchSize : (short) (samplesCnt % SampleBatchSize);
                List<TempSample> batch = ReadSamplesBatch(startTSmSec + (batchStart * intervalSeconds * 1000), intervalSeconds, (short) (batchStart * WORDS_PER_MEASUREMENT), batchSize);
                result.addAll(batch);
            }
            return result;
        }
    }

    @Override
    public List<TempSample> ReadSamples(int samplesCnt, int intervalSeconds, long startTSmSec, Long pickedAt) throws Exception {
        if (samplesCnt <= SampleBatchSize) {
            return ReadSamplesBatch(pickedAt, startTSmSec, intervalSeconds, SHORT_ZERO, samplesCnt);
        } else {
            List<TempSample> result = new LinkedList<>();
            for (short batchStart = 0; batchStart < samplesCnt; batchStart += SampleBatchSize) {
                short batchSize = (samplesCnt - batchStart) >= SampleBatchSize ? SampleBatchSize : (short) (samplesCnt % SampleBatchSize);
                List<TempSample> batch = ReadSamplesBatch(pickedAt, startTSmSec + (batchStart * intervalSeconds * 1000), intervalSeconds, (short) (batchStart * WORDS_PER_MEASUREMENT), batchSize);
                result.addAll(batch);
            }
            return result;
        }
    }

    @Override
    public Reader.READER_ERR Setup(short interval) {
        WriteTimeBinONE();
        WriteInterval(interval);
        return WriteCurrentDatetime();
    }

    @Override
    public Double StartLogging() throws Exception {
        EnableLogging();
        WaitFor(500l);
        return ReadLastSample();
    }

    @Override
    public void Status(boolean open) {
        LoggerIsOpen = open;
    }

    @Override
    public boolean IsOpen() {
        return LoggerIsOpen;
    }

    //###################################################
    //###  Private methods for Read / Write commands  ###
    //###################################################
    private List<TempSample> ReadSamplesBatch(long beginTSmSec, int intervalSeconds, short start, int samplesCnt) throws Exception {
        //TODO: surround with try..catch to return null when ReadRegisters(..) fails....
        byte[] reply = ReadRegisters((short) (ADDR_LOGS + start), (short) (samplesCnt * WORDS_PER_MEASUREMENT));
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read sample data.");
        else if (reply == null || reply.length < WORDS_PER_MEASUREMENT * 2)
            return new LinkedList<>();

        return parseDataWithoutTimestamp(beginTSmSec, intervalSeconds, reply);
    }

    private List<TempSample> ReadSamplesBatch(Long pickedAt, long beginTSmSec, int intervalSeconds, short start, int samplesCnt) throws Exception {
        //TODO: surround with try..catch to return null when ReadRegisters(..) fails....
        byte[] reply = ReadRegisters((short) (ADDR_LOGS + start), (short) (samplesCnt * WORDS_PER_MEASUREMENT));
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read sample data.");
        else if (reply == null || reply.length < WORDS_PER_MEASUREMENT * 2)
            return new LinkedList<>();

        return parseDataWithoutTimestamp(pickedAt, beginTSmSec, intervalSeconds, reply);
    }

    private List<TempSample> parseDataWithTimestamp(byte[] data) {
        List<TempSample> measurements = new LinkedList<>();

        for (int i = 0; i < data.length - 5; i += 6) {
            short t = ToShort(new byte[]{data[i], data[i + 1]});
            byte[] bytes = new byte[]{data[i + 4], data[i + 5], data[i + 2], data[i + WORDS_PER_MEASUREMENT]};
            measurements.add(new TempSample(parseTimestamp(bytes), String.format("%.2f", parseTemperatureNumeric(t))));  //"%.2f\u2103"
        }
        return measurements;
    }

    private List<TempSample> parseDataWithoutTimestamp(long beginTSmSec, int intervalSeconds, byte[] data) {
        List<TempSample> measurements = new LinkedList<>();
        for (int sampleIdx = 0; sampleIdx < data.length / 2; sampleIdx++) {

            int byteIdx = sampleIdx * 2;
            short t = ToShort(new byte[]{data[byteIdx], data[byteIdx + 1]});
            Double temp = parseTemperatureNumeric(t);
            if (temp != null && temp >= -10 && temp < 40 && round(temp, 2) != 0.03 && round(temp, 2) != -0.03) {
                measurements.add(new TempSample(createTimestamp(beginTSmSec + (sampleIdx * intervalSeconds * 1000L)), String.format("%.2f", parseTemperatureNumeric(t))));
            } else {
                measurements.add(new TempSample(createTimestamp(beginTSmSec + (sampleIdx * intervalSeconds * 1000L)), "N/A"));
            }
        }
        return measurements;
    }

    private List<TempSample> parseDataWithoutTimestamp(Long fishingTS, long beginTSmSec, int intervalSeconds, byte[] data) {
        List<TempSample> measurements = new LinkedList<>();
        for (int sampleIdx = 0; sampleIdx < data.length / 2; sampleIdx++) {

            int byteIdx = sampleIdx * 2;
            short t = ToShort(new byte[]{data[byteIdx], data[byteIdx + 1]});
            Double temp = parseTemperatureNumeric(t);
            long currTemperatureTS = beginTSmSec + (sampleIdx * intervalSeconds * 1000L);

            // Note: on some extreme cases, there are no measurements after the pickedAt time.
            //       this caused the android app to show "invalid state"...
            if (fishingTS != null && (fishingTS - currTemperatureTS > 30 * 60 * 1000)) {
                // filter out measurements taken before fishing started.
                // currently specific to Kefalonia...
                continue;
            }
            boolean isAfterFishing;
            if (fishingTS > currTemperatureTS) {
                isAfterFishing = false;
            } else {
                isAfterFishing = true;
            }
            if (temp != null && temp >= -10 && temp < 40 && round(temp, 2) != 0.03 && round(temp, 2) != -0.03) {
                //measurements.add(new String[]{createTimestamp(currTemperatureTS), String.format("%.2f", parseTemperatureNumeric(t))})
                    measurements.add(new TempSample(createTimestamp(currTemperatureTS), String.format("%.2f", parseTemperatureNumeric(t)), isAfterFishing));
            } else {
                //measurements.add(new String[]{createTimestamp(currTemperatureTS), "N/A"});
                measurements.add(new TempSample(createTimestamp(currTemperatureTS), "N/A", isAfterFishing));
            }
        }

        return measurements;
    }
}