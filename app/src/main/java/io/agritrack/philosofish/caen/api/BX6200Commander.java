package io.agritrack.philosofish.caen.api;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CMD_READ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CMD_WRITE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.MAXBYTESIZEDATA;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDREADBASE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDWRITE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_WRITEPAGE;

import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;
import com.uhf.api.cls.Reader;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cn.pda.serialport.Tools;
import io.agritrack.philosofish.caen.common.INTERFACEMEM;
import io.agritrack.philosofish.caen.pojo.RFIDTag;

public class BX6200Commander extends AbstractCAENCommander {
    private final UhfReader uhfReader;
    private byte[] epcBytes;


    public BX6200Commander() {
        uhfReader = UhfReader.getInstance();

        if (uhfReader != null) {
            uhfReader.setWorkArea(3);
            uhfReader.setOutputPower(24);
        } else {
            Toast.makeText(getAppContext(), "Failed to initialize UHF Reader", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void setFilterEPC(String epc) {
        this.epcBytes = Tools.HexString2Bytes(epc);
        this.uhfReader.selectEPC(epcBytes);
    }

    @Override
    public boolean clearEPCFilter() {
        return (this.uhfReader.unSelectEPC() > 0);
    }

    // #########################
    // ###  Private Methods  ###
    // #########################
    @Override
    protected byte[] ReadRegisters(short address, short length) throws Exception {
        short command;
        byte msgID = 0x00;
        short numBytes = (short) (length * 1);
        byte reply = REPLY_NACK;

        if (numBytes > MAXBYTESIZEDATA) {
            throw new Exception("Requested Read length exceeds max limit (200 words)!");
        }

        // check current msgID value written in reply word and adjust msgID of next command accordingly
        msgID = adjustReplyId(msgID);

        command = (short) (msgID << 8 | CMD_READ);

        //load command parameters in user memory
        boolean b = INTERFACEMEM.SetReadCommand(this.uhfReader, command, address, numBytes, accessPassword);

        // wait for tag to set command
        Thread.sleep(TIME_WAITTAG_CMDREADBASE);

        //trigger tag command reception+execution
        INTERFACEMEM.Trigger(this.uhfReader, accessPassword);

        // wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDREADBASE + TIME_WAITTAG_WRITEPAGE * (numBytes));

        //check if tag replied
        reply = adjustReplyId(msgID);

        //check reply
        if (reply == REPLY_NACK) {
            throw new Exception("Tag replied NACK");
        }

        //tag replied ACK, now we can read the data
        byte[] data = INTERFACEMEM.ReadData(this.uhfReader, (short) 0, numBytes, accessPassword);
        return data;
    }

    @Override
    protected Reader.READER_ERR WriteRegisters(short address, Object data) throws Exception {
        byte msgID = 0x00;
        short command;
        //short size = (short) (length * 2);
        short size = data instanceof Long ? SHORT_TWO : SHORT_ONE;
        byte reply = REPLY_NACK;

        if (size > MAXBYTESIZEDATA) {
            throw new Exception("Requested Read length exceeds max limit (200 words)!");
        }

        command = (short) (msgID << 8 | CMD_WRITE);

        // Fill the 5 Registers with the required command parameters.
        String cmdValue = INTERFACEMEM.SetWriteCommand(this.uhfReader, command, address, (short) size, msgID, data, accessPassword);

        //wait for tag to write command
        Thread.sleep(TIME_WAITTAG_CMDWRITE);

        //trigger tag command reception+execution
        INTERFACEMEM.Trigger(this.uhfReader, accessPassword);

        //wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDWRITE);

        //check if tag replied
        reply = adjustReplyId(msgID);

        //check reply
        if (reply != msgID + 1) {
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }

        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public byte CheckReply() {
        int retries = 0;

        // check current idmsg value written in reply word and adjust idmsg of next command accordingly
        byte[] replyVal = INTERFACEMEM.ReadReply(uhfReader, accessPassword);

        try {
            while ((replyVal.length == 1 || REPLY_NACK == replyVal[1]) && retries < 10) {
                replyVal = INTERFACEMEM.ReadReply(uhfReader, accessPassword);
                // wait for tag to parse command, execute it, and reply
                Thread.sleep(TIME_WAITTAG_CMDREADBASE + TIME_WAITTAG_WRITEPAGE);
                retries++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return replyVal[0];
    }

    // ########################
    // ###  public methods  ###
    // ########################
    @Override
    public Reader.READER_ERR Reset() {
        try {
            Thread.sleep(200);
            Reader.READER_ERR rs = WriteRegisters(ADDR_CONTROL, SHORT_ONE);
            return rs;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Reader.READER_ERR.MT_CMD_FAILED_ERR;
    }

    @Override
    public byte[] Trigger() {
        INTERFACEMEM.Trigger(this.uhfReader, accessPassword);
        return null;
    }

    @Override
    public void CloseReader() {
        this.uhfReader.close();
        this.Status(Boolean.FALSE);
        RFIDModuleFactory.Reset();
    }

    @Override
    public void StopReading() {
        this.uhfReader.unSelectEPC();
        //this.uhfReader.close();
    }

    @Override
    public List<RFIDTag> inventoryRealTime() {
        List<TagModel> inventory = this.uhfReader.inventoryRealTime();
        if (inventory == null) {
            return Collections.emptyList();
        }
        return inventory.stream().map(x -> new RFIDTag(TagToString.apply(x), x.getmRssi())).collect(Collectors.toList());
    }

    @Override
    public List<RFIDTag> inventoryByTimer() {
        return inventoryRealTime();
    }

    @Override
    public List<RFIDTag> searchInventory() {
        return inventoryRealTime();
    }

    @Override
    public boolean startReading() {
        //Reader.READER_ERR res = this.uhfReader.asyncStartReading();
        return false; //Reader.READER_ERR.MT_OK_ERR.equals(res);
    }

    @Override
    public boolean startSearching() {
        return true;
    }

    @Override
    public List<RFIDTag> search() {
        return inventoryRealTime();
    }

    @Override
    public boolean stopSearching() {
        return true;
    }


    @Override
    public Reader.READER_ERR HighPowerLevel() {
        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public Reader.READER_ERR LowPowerLevel() {
        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public Reader.READER_ERR MedPowerLevel() {
        return Reader.READER_ERR.MT_OK_ERR;
    }

    public Reader.READER_ERR MidPowerLevel() {
        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public int[] getPowerLevel() {
        return new int[2];
    }
}
