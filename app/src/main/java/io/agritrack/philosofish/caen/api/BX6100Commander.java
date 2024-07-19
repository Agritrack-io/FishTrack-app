package io.agritrack.philosofish.caen.api;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_COMMAND;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_CONTROL;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_DATA;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_REPLY;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ADDR_TRIGGER;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CMD_READ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CMD_WRITE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.EPCBANK;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.MAXBYTESIZEDATA;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.REPLY_ACK;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.SHORT_ONE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.SHORT_TWO;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDREADBASE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDWRITE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_WRITEPAGE;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.USERBANK;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.pda.serialport.Tools;
import io.agritrack.philosofish.caen.pojo.RFIDTag;

public class BX6100Commander extends AbstractCAENCommander {
    private final short timeout = 10000;
    private final int filterStartAddress = 2;
    private UHFRManager mUhfRManager;
    private byte[] epcBytes;
    private String tagToSearch;


    public BX6100Commander() {
        mUhfRManager = UHFRManager.getInstance();// Init Uhf module
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(24, 24);//set uhf module power

            if (err == Reader.READER_ERR.MT_OK_ERR) {
                mUhfRManager.setRegion(Reader.Region_Conf.RG_EU3);
                //Toast.makeText(getAppContext(), "FreRegion:" + Reader.Region_Conf.RG_EU3 + "\n" + "Read Power:" + 33 + "\n" + "Write Power:" + 33, Toast.LENGTH_LONG).show();
            } else {
                Reader.READER_ERR err1 = mUhfRManager.setPower(24, 24);//set uhf module power
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

    public Reader.READER_ERR HighPowerLevel() {
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(24, 24);//set uhf module power
            if (err != Reader.READER_ERR.MT_OK_ERR) {
                Reader.READER_ERR err1 = mUhfRManager.setPower(24, 24);//set uhf module power
                if (err1 != Reader.READER_ERR.MT_OK_ERR) {
                    Toast.makeText(getAppContext(), "Failed to switch to HIGH Energy mode!!", Toast.LENGTH_LONG);
                    return Reader.READER_ERR.MT_CMD_FAILED_ERR;
                }
            }
        } else {
            Toast.makeText(getAppContext(), "No UHFR manager found!!", Toast.LENGTH_LONG);
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }
        return Reader.READER_ERR.MT_OK_ERR;
    }

    public Reader.READER_ERR LowPowerLevel() {
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(24, 24);//set uhf module power
            if (err != Reader.READER_ERR.MT_OK_ERR) {
                Reader.READER_ERR err1 = mUhfRManager.setPower(24, 24);//set uhf module power
                if (err1 != Reader.READER_ERR.MT_OK_ERR) {
                    Toast.makeText(getAppContext(), "Failed to switch to LOW Energy mode!!", Toast.LENGTH_LONG);
                    return Reader.READER_ERR.MT_CMD_FAILED_ERR;
                }
            }
        } else {
            Toast.makeText(getAppContext(), "No UHFR manager found!!", Toast.LENGTH_LONG);
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }
        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public int[] getPowerLevel() {
        return mUhfRManager.getPower();
    }

    @Override
    public void setFilterEPC(String epc) {
        this.tagToSearch = epc;
        this.epcBytes = Tools.HexString2Bytes(epc);
        this.mUhfRManager.setInventoryFilter(this.epcBytes, EPCBANK, filterStartAddress, true);
    }

    @Override
    public boolean clearEPCFilter() {
        if (this.mUhfRManager == null) {
            return false;
        }
        return this.mUhfRManager.setCancleInventoryFilter();
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
        Thread.sleep(100);
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
        Thread.sleep(100);
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
        reply = adjustReplyId(msgID);

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
            return reply != null && REPLY_ACK == reply[1] ? reply[0] : REPLY_NACK;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return REPLY_NACK;
    }


    // ########################
    // ###  public methods  ###
    // ########################
    /* This function RESETS the logger */
    @Override
    public Reader.READER_ERR Reset() {
        try {
            Thread.sleep(200);
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
        if (this.mUhfRManager != null) {
            this.mUhfRManager.close();
        }
        this.Status(Boolean.FALSE);
        RFIDModuleFactory.Reset();
    }

    @Override
    public List<RFIDTag> inventoryByTimer() {
        List<Reader.TAGINFO> inventory = this.mUhfRManager.tagInventoryByTimer((short) 500);
        if (inventory == null) {
            return Collections.emptyList();
        }
        return inventory.stream().map(x -> new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public List<RFIDTag> inventoryRealTime() {
        List<Reader.TAGINFO> inventory = this.mUhfRManager.tagInventoryRealTime();
        //List<Reader.TAGINFO> inventory = this.mUhfRManager.tagInventoryByTimer((short) 250);//  tagInventoryRealTime();
        return inventory.stream().map(x -> new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public List<RFIDTag> searchInventory() {
        mUhfRManager.setGen2session(false);
        mUhfRManager.setInventoryFilter(this.epcBytes, 1, 2, true);
        List<Reader.TAGINFO> inventory = mUhfRManager.tagEpcTidInventoryByTimer((short) 100);
        Stream<Reader.TAGINFO> filteredStream = inventory.stream().filter(k -> Tools.Bytes2HexString(k.EpcId, k.Epclen).indexOf(tagToSearch.substring(tagToSearch.length() - 6)) > -1);
        return filteredStream.map(x -> new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public boolean startReading() {
        if (this.mUhfRManager == null) {
            this.mUhfRManager = UHFRManager.getInstance();
            if (this.mUhfRManager == null) {
                CToast(null, render("Couldn't find RFID module, please retry!!"), Toast.LENGTH_SHORT);
                return false;
            }
        }
        if (!Strings.isEmptyOrWhitespace(this.tagToSearch)) {
            this.mUhfRManager.setCancleInventoryFilter();
        }
        this.mUhfRManager.setGen2session(true);
        Reader.READER_ERR result = this.mUhfRManager.asyncStartReading();

        return Reader.READER_ERR.MT_OK_ERR.equals(result);  //false;
    }


    @Override
    public void StopReading() {
        if (this.mUhfRManager != null) {
            this.mUhfRManager.setCancleInventoryFilter();
            this.mUhfRManager.asyncStopReading();
            this.mUhfRManager.stopTagInventory();
            this.mUhfRManager.setGen2session(false);
            //this.mUhfRManager = null;
        }
    }

    @Override
    public boolean startSearching() {
        mUhfRManager.setGen2session(false);
        if (this.epcBytes != null) {
            return mUhfRManager.setInventoryFilter(this.epcBytes, 1, 2, true);
        } else {
            return false;
        }
    }

    @Override
    public List<RFIDTag> search() {
        List<Reader.TAGINFO> inventory = mUhfRManager.tagEpcTidInventoryByTimer((short) 100);
        Stream<Reader.TAGINFO> filteredStream = inventory.stream().filter(k -> Tools.Bytes2HexString(k.EpcId, k.Epclen).indexOf(tagToSearch.substring(tagToSearch.length() - 6)) > -1);
        return filteredStream.map(x -> new RFIDTag(TagInfoToString.apply(x), x.RSSI)).collect(Collectors.toList());
    }

    @Override
    public boolean stopSearching() {
        return this.mUhfRManager.setCancleInventoryFilter();
    }
}
