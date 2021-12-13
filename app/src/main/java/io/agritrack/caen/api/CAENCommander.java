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
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_ACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_FOUR;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ZERO;
import static io.agritrack.caen.api.EncodingUtils.ToShort;
import static io.agritrack.caen.api.EncodingUtils.parseTemperature;
import static io.agritrack.caen.api.EncodingUtils.parseTimestamp;

import com.android.hdhe.uhf.reader.UhfReader;

import java.util.LinkedList;
import java.util.List;

import cn.pda.serialport.Tools;
import io.agritrack.caen.common.CAENRegistersIO;

public class CAENCommander {
    public static final Short DefaultInterval = (short) (30);
    private static final Short SampleBatchSize = 30;
    private final byte[] accessPassword = Tools.HexString2Bytes("00000000");
    private final UhfReader uhfReader;
    private final Response success;


    public CAENCommander(UhfReader _uhfReader, String epc) {
        this.uhfReader = _uhfReader;
        this.uhfReader.selectEPC(Tools.HexString2Bytes(epc));
        this.success = new Response();
    }

    public short INIT() throws Exception {
        return INIT(DefaultInterval);
    }

    public short INIT(short interval) throws Exception {
        WriteTimeBin_ONE();
        WriteInterval(interval);
        WriteCurrentDatetime();
        EnableLogging();
        return READ_LAST_SAMPLE();
    }

    public Response RESET() {
        Response res = WriteRegisters(ADDR_CONTROL, SHORT_ONE, SHORT_ONE);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to reset data logger.");
        }
        return success;
    }

    public Response SETUP(short interval) {
        //WriteTimeBin_ONE();
        return WriteInterval(interval);
        //return WriteCurrentDatetime();
    }

    public short START_LOGGING() throws Exception {
        EnableLogging();
        return READ_LAST_SAMPLE();
    }

    public Response HighSensitivity() {
        Short bits = Short.valueOf("0014", 16);
        Response res = WriteRegisters(ADDR_CONTROL, SHORT_ONE, bits);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to set High Sensitivity.");
        }
        return success;
    }

    public Response LowSensitivity() {
        Short bits = Short.valueOf("0004", 16);
        Response res = WriteRegisters(ADDR_CONTROL, SHORT_ONE, bits);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to set Low Sensitivity.");
        }
        return success;
    }


    public Response WriteCurrentDatetime() {
        long unixTime = System.currentTimeMillis() / 1000L;
        Response res = WriteRegisters(ADDR_TIMESTAMP, SHORT_TWO, unixTime);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to set timestamp.");
        }
        return success;
    }

    public Response WriteTimeBin_ONE() {
        Response res = WriteRegisters(ADDR_TIME_BIN, SHORT_ONE, SHORT_ONE);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to set time bin.");
        }
        return success;
    }

    public Response WriteInterval(Short interval) {
        short _interval = interval == null ? 900 : interval;
        Response res = WriteRegisters(ADDR_INTERVAL, SHORT_ONE, _interval);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to set interval.");
        }
        return success;
    }

    public Response EnableLogging() {
        Response res = WriteRegisters(ADDR_CONTROL, SHORT_ONE, SHORT_FOUR);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to start logging.");
        }
        return success;
    }

    public Response DisableLogging() {
        Response res = WriteRegisters(ADDR_CONTROL, SHORT_ONE, SHORT_ZERO);
        if (!res.succeeded()) {
            return new Response(REPLY_NACK, "Failed to stop logging.");
        }
        return success;
    }


    public String READ_CONTROL_REGISTER() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_CONTROL, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read CONTROL register.");
        else if (reply.length < 2)
            return "N/A";

        return Integer.toBinaryString(ToShort(reply));
    }

    public String READ_STATUS_REGISTER() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_STATUS, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read STATUS register.");
        else if (reply.length < 2)
            return "N/A";

        return String.format("%8s", Integer.toBinaryString(ToShort(reply))).replace(' ', '0');
    }

    public String READ_FW_REVISION() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_FW_REVISION, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read FW revision.");
        else if (reply.length < 2)
            return "N/A";

        return String.format("%s.%s", reply[0], reply[1]);
    }

    public String READ_HW_REVISION() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_HW_REVISION, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read HW revision.");
        else if (reply.length < 2)
            return "N/A";

        return String.format("%s.%s", reply[0], reply[1]);
    }

    public String READ_INIT_DATETIME() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_INIT_DATE_L, SHORT_TWO, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read init timestamp.");
        else if (reply.length < 4)
            return "N/A";

        return parseTimestamp(new byte[]{reply[2], reply[3], reply[0], reply[1]});
    }

    public short READ_SAMPLES_COUNT() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_SAMPLES_CNT, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read Samples Count value.");

        return ToShort(reply);
    }

    public short READ_LAST_SAMPLE() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_LAST_SAMPLE, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 1 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read last sample value.");
        else if(reply.length == 1) {
            return Short.valueOf("-99");
        }

        return ToShort(reply);
    }

    public List<String[]> READ_SAMPLES(int samplesCnt) throws Exception {
        if (samplesCnt <= SampleBatchSize) {
            return READ_SAMPLES_BATCH(SHORT_ZERO, samplesCnt);
        } else {
            List<String[]> result = new LinkedList<>();
            for (short batchStart = 0; batchStart < samplesCnt; batchStart += SampleBatchSize) {
                short batchSize = (samplesCnt - batchStart) >= SampleBatchSize ? SampleBatchSize : (short) (samplesCnt % SampleBatchSize);
                List<String[]> batch = READ_SAMPLES_BATCH((short) (batchStart * 3), batchSize);
                result.addAll(batch);
                Thread.sleep(200l);
            }
            return result;
        }
    }

    private List<String[]> READ_SAMPLES_BATCH(short start, int samplesCnt) throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, (short) (ADDR_LOGS + start), (short) (samplesCnt * 3), accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read sample data.");
        else if (reply.length < 6)
            return new LinkedList<>();

        return parseData(reply);
    }

    public short READ_TIME_BIN() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_TIME_BIN, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read time bin.");

        return ToShort(reply);
    }

    public short READ_INTERVAL() throws Exception {
        byte[] reply = CAENRegistersIO.ReadRegisters(uhfReader, ADDR_INTERVAL, SHORT_ONE, accessPassword);
        if (reply != null && reply.length > 0 && reply[0] == REPLY_NACK)
            throw new Exception("Failed to read interval.");

        return ToShort(reply);
    }

    // #########################
    // ###  Private Methods  ###
    // #########################
    private Response WriteRegisters(short address, short length, Object data) {
        try {
            byte reply = CAENRegistersIO.WriteRegisters(uhfReader, address, length, data, accessPassword);
            if (reply == REPLY_NACK) {
                return new Response(REPLY_NACK, "Failed to reset data logger.");
            }
        } catch (Exception e) {
            return new Response(REPLY_NACK, e.getMessage());
        }
        return success;
    }

    private List<String[]> parseData(byte[] data) {
        List<String[]> measurements = new LinkedList<>();

        for (int i = 0; i < data.length - 5; i += 6) {
            short t = ToShort(new byte[]{data[i], data[i + 1]});
            byte[] bytes = new byte[]{data[i + 4], data[i + 5], data[i + 2], data[i + 3]};
            measurements.add(new String[]{parseTimestamp(bytes), parseTemperature(t) + "\u2103"});
        }

        return measurements;
    }


    public class Response {
        public byte code;
        public String message;

        public Response() {
            this.code = REPLY_ACK;
            this.message = "OK";
        }

        public Response(byte code, String msg) {
            this.code = code;
            this.message = msg;
        }

        public boolean succeeded() {
            return this.code == REPLY_ACK;
        }
    }
}