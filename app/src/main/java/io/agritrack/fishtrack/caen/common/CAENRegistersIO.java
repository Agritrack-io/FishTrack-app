package io.agritrack.fishtrack.caen.common;

import com.android.hdhe.uhf.reader.UhfReader;

import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.CMD_READ;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.CMD_WRITE;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.MAXBYTESIZEDATA;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.REPLY_NACK;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDREADBASE;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_CMDWRITE;
import static io.agritrack.fishtrack.caen.api.CAEN_CONSTANTS.TIME_WAITTAG_WRITEPAGE;


public class CAENRegistersIO {

    public static byte[] ReadRegisters(UhfReader reader, short address, short length, byte[] accessPassword) throws Exception {
        short command;
        byte msgID = 0x00;
        short numBytes = length;
        byte reply = REPLY_NACK;

        if (numBytes > MAXBYTESIZEDATA) {
            throw new Exception("Requested Read length exceeds max limit (200 words)!");
        }

        // check current msgID value written in reply word and adjust msgID of next command accordingly
        msgID = adjustReplyId(msgID, reader, accessPassword);

        command = (short) (msgID << 8 | CMD_READ);

        //load command parameters in user memory
        boolean b = INTERFACEMEM.SetReadCommand(reader, command, address, numBytes, accessPassword);

        //trigger tag command reception+execution
        INTERFACEMEM.Trigger(reader, accessPassword);

        // wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDREADBASE + TIME_WAITTAG_WRITEPAGE * (numBytes / 4 + 1));

        //check if tag replied
        reply = adjustReplyId(msgID, reader, accessPassword);

        //check reply
        if (reply == REPLY_NACK) {
            throw new Exception("Tag replied NACK");
        }

        //tag replied ACK, now we can read the data
        byte[] data = INTERFACEMEM.ReadData(reader, (short) 0, numBytes, accessPassword);
        return data;
    }

    public static byte WriteRegisters(UhfReader reader, short address, short length, Object data, byte[] pwd) throws Exception {
        byte msgID = 0x00;
        short command;
        short numBytes = (short) (length * 2);
        byte reply = REPLY_NACK;

        if (numBytes > MAXBYTESIZEDATA) {
            throw new Exception("Requested Read length exceeds max limit (200 words)!");
        }

        // check current msgID value written in reply word and adjust msgID of next command accordingly
        msgID = adjustReplyId(msgID, reader, pwd);

        command = (short) (msgID << 8 | CMD_WRITE);

        // Fill the 5 Registers with the required command parameters.
        String outcome = INTERFACEMEM.SetWriteCommand(reader, command, address, length, msgID, data, pwd);

        //trigger tag command reception+execution
        INTERFACEMEM.Trigger(reader, pwd);

        //wait for tag to parse command, execute it, and reply
        Thread.sleep(TIME_WAITTAG_CMDWRITE);

        //check if tag replied
        reply = adjustReplyId(msgID, reader, pwd);

        //check reply
        if (reply == REPLY_NACK) {
            throw new Exception("Tag replied NACK");
        }

        return reply;
    }

    private static byte adjustReplyId(byte msgId, UhfReader reader, byte[] accessPassword) throws InterruptedException {
        int retries = 0;

        // check current idmsg value written in reply word and adjust idmsg of next command accordingly
        byte[] replyVal = INTERFACEMEM.ReadReply(reader, accessPassword);

        //String _id = BytesToHex(replyVal);

        while (replyVal.length == 1 && retries < 10) {
            // wait for tag to parse command, execute it, and reply
            Thread.sleep(TIME_WAITTAG_CMDREADBASE + TIME_WAITTAG_WRITEPAGE);
            replyVal = INTERFACEMEM.ReadReply(reader, accessPassword);
            retries++;
        }

        if (replyVal[0] == msgId) {
            msgId++;
            return msgId;
        }

        //String _idd = BytesToHex(replyVal);

        return replyVal[0];
    }
}
