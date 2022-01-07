package io.agritrack.caen.api;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_COMMAND;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_DATA;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_REPLY;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ADDR_TRIGGER;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CMD_READ;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CMD_WRITE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.EPCBANK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.MAXBYTESIZEDATA;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_ACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDREADBASE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDWRITE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_WRITEPAGE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.USERBANK;

import android.widget.Toast;

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import java.util.List;
import java.util.stream.Collectors;

import cn.pda.serialport.Tools;
import io.agritrack.caen.pojo.RFIDTag;

public class BX6100Commander extends AbstractCAENCommander {
    private final short timeout = 10000;
    private final int filterStartAddress = 2;
    private final UHFRManager mUhfRManager;
    private byte[] epcBytes;

    public BX6100Commander() {
        mUhfRManager = UHFRManager.getInstance();// Init Uhf module
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(33, 33);//set uhf module power

            if (err == Reader.READER_ERR.MT_OK_ERR) {
                mUhfRManager.setRegion(Reader.Region_Conf.RG_EU3);
                //Toast.makeText(getAppContext(), "FreRegion:" + Reader.Region_Conf.RG_EU3 + "\n" + "Read Power:" + 33 + "\n" + "Write Power:" + 33, Toast.LENGTH_LONG).show();
            } else {
                Reader.READER_ERR err1 = mUhfRManager.setPower(30, 30);//set uhf module power
                if (err1 == Reader.READER_ERR.MT_OK_ERR) {
                    mUhfRManager.setRegion(Reader.Region_Conf.RG_EU3);
                    //Toast.makeText(getAppContext(), "FreRegion:" + Reader.Region_Conf.RG_EU3 + "\n" + "Read Power:" + 30 + "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getAppContext(), "Failed to initialize UHFR manager", Toast.LENGTH_LONG);
                }
            }
        } else {
            Toast.makeText(getAppContext(), "Failed to initialize UHFR manager", Toast.LENGTH_LONG);
        }
    }


    @Override
    public void setFilterEPC(String epc) {
        this.epcBytes = Tools.HexString2Bytes(epc);
        this.mUhfRManager.setInventoryFilter(this.epcBytes, EPCBANK, filterStartAddress, true);
    }

    private byte[] readTagDataByFilter(int memBank, short startAddress, short len) {
        return mUhfRManager.getTagDataByFilter(memBank, startAddress, len, accessPassword, timeout, epcBytes, EPCBANK, filterStartAddress, true);
    }

    private Reader.READER_ERR writeTagDataByFilter(int memBank, short startAddress, short value) {
        return writeTagDataByFilter(memBank, startAddress, EncodingUtils.ToBytes(value));
    }

    private Reader.READER_ERR writeTagDataByFilter(int memBank, short startAddress, byte[] writeDataBytes) {
        //write data
        return mUhfRManager.writeTagDataByFilter((char) memBank, startAddress, writeDataBytes, writeDataBytes.length, accessPassword, timeout, epcBytes, EPCBANK, filterStartAddress, true);
    }

    @Override
    protected byte[] ReadRegisters(short address, short length) throws Exception {
        short command;
        byte msgID = 0x01;
        short numBytes = (short) (length * 2);
        byte reply = REPLY_NACK;

        if (numBytes > MAXBYTESIZEDATA) {
            throw new Exception("Requested Read length exceeds max limit (200 words)!");
        }

        // check current msgID value written in reply word and adjust msgID of next command accordingly
        command = (short) (msgID << 8 | CMD_READ);

        //load command parameters in user memory
        byte[] cmd = ComposeReadCommand(command, address, length, msgID);
        Reader.READER_ERR a = writeTagDataByFilter(USERBANK, ADDR_COMMAND, cmd);

        //trigger tag command reception+execution
        byte[] triggRS = Trigger();

        // wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDREADBASE + TIME_WAITTAG_WRITEPAGE * (numBytes / 4 + 1));

        //check if tag replied
        reply = adjustReplyId(msgID);

        //check reply
        if (reply == REPLY_NACK) {
            throw new Exception("Tag replied NACK");
        }

        //tag replied ACK, now we can read the data
        return readTagDataByFilter(USERBANK, ADDR_DATA, length);
//        return Reader.READER_ERR.MT_IO_ERR;
    }

    @Override
    protected Reader.READER_ERR WriteRegisters(short address, Object data) throws Exception {
        byte msgID = 0x00;
        short command;
        byte reply = REPLY_NACK;
        short size = data instanceof Long ? SHORT_TWO : SHORT_ONE;

        // check current msgID value written in reply word and adjust msgID of next command accordingly
        command = (short) (msgID << 8 | CMD_WRITE);

        // Fill the 3 Registers with the required command parameters.
        byte[] cmdBytes = ComposeWriteCommand(command, address, size, msgID, data);
        String cmdd = Tools.Bytes2HexString(cmdBytes, cmdBytes.length);
        Reader.READER_ERR outcome = writeTagDataByFilter(USERBANK, ADDR_COMMAND, cmdBytes);

        //wait for tag to process Write command
        Thread.sleep(TIME_WAITTAG_CMDREADBASE);

        //trigger tag command reception+execution
        byte[] triggRS = readTagDataByFilter(EPCBANK, ADDR_TRIGGER, SHORT_ONE);

        //wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDWRITE);

        //check if tag replied
        reply = (byte) adjustReplyId(msgID);

        //check reply
        if (reply != msgID + 1) {
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }

        return Reader.READER_ERR.MT_OK_ERR;
    }

    /* This function checks the reply value, recommended to call it after a trigger command */
    @Override
    public byte CheckReply() {
        try {
            byte[] reply = readTagDataByFilter(USERBANK, ADDR_REPLY, SHORT_ONE);
            return REPLY_ACK == reply[1] ? reply[0] : REPLY_NACK == reply[1] ? (byte) -1 : (byte) -2;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -2;
    }


    // ########################
    // ###  public methods  ###
    // ########################
    @Override
    public Double Init() throws Exception {
        return Init(DefaultInterval);
    }

    @Override
    public Double Init(short interval) throws Exception {
        Reader.READER_ERR rs = WriteTimeBinONE();
        rs = WriteInterval(interval);;
        rs = WriteCurrentDatetime();
        rs = EnableLogging();
        return ReadLastSample();
    }

    /* This function RESETS the logger */
    @Override
    public Reader.READER_ERR Reset() {
        try {
            return WriteRegisters(ADDR_CONTROL, SHORT_ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Reader.READER_ERR.MT_CMD_FAILED_ERR;
    }

    /* This function triggers the tag parsing and execution of a command */
    @Override
    public byte[] Trigger() {
        try {
            return readTagDataByFilter(EPCBANK, ADDR_TRIGGER, SHORT_ONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void CloseReader() {
        this.mUhfRManager.close();
        this.Status(Boolean.FALSE);
        RFIDModuleFactory.Reset();
    }

    @Override
    public void StopReading() {
        this.mUhfRManager.setCancleInventoryFilter();
        this.mUhfRManager.asyncStopReading();
        this.mUhfRManager.stopTagInventory();
        this.mUhfRManager.setGen2session(false);
    }

    @Override
    public List<RFIDTag> inventoryRealTime() {
        //this.mUhfRManager.setCancleInventoryFilter();
        //this.mUhfRManager.setGen2session(false);
        List<Reader.TAGINFO> inventory = this.mUhfRManager.tagInventoryRealTime();
        //List<Reader.TAGINFO> inventory = this.mUhfRManager.tagEpcTidInventoryByTimer((short) 200);//  tagInventoryRealTime();
        return inventory.stream().map(x->new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public List<RFIDTag> inventoryWithFilter() {
        List<Reader.TAGINFO> inventory = this.mUhfRManager.tagInventoryRealTime();
        this.mUhfRManager.setCancleInventoryFilter();
        return inventory.stream().map(x->new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public boolean startReading() {
        //this.mUhfRManager.setGen2session(true);
        Reader.READER_ERR result = this.mUhfRManager.asyncStartReading();
        return false;// Reader.READER_ERR.MT_OK_ERR.equals(res);
    }
}