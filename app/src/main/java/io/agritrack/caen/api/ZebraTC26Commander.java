package io.agritrack.caen.api;

import android.util.Log;

import com.uhf.api.cls.Reader;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import java.util.List;

import io.agritrack.caen.pojo.RFIDTag;

public class ZebraTC26Commander extends AbstractCAENCommander implements Readers.RFIDReaderEventHandler {
    final static String TAG = "RFID_Zebra_TC26";

    private static RFIDReader reader;
    //private final Handler mScanHandler;
    //private MainActivity context;


    @Override
    public void setFilterEPC(String epc) {

    }

    @Override
    public boolean clearEPCFilter() {
        return false;
    }

    @Override
    public Reader.READER_ERR Reset() {
        return null;
    }

    @Override
    public byte[] Trigger() {
        return new byte[0];
    }

    @Override
    protected byte[] ReadRegisters(short address, short length) throws Exception {
        return new byte[0];
    }

    @Override
    protected Reader.READER_ERR WriteRegisters(short address, Object data) throws Exception {
        return null;
    }

    @Override
    public byte CheckReply() {
        return 0;
    }

    @Override
    public void CloseReader() {

    }

    @Override
    public void StopReading() {

    }

    @Override
    public List<RFIDTag> inventoryRealTime() {
        return null;
    }

    @Override
    public List<RFIDTag> inventoryByTimer() {
        return null;
    }

    @Override
    public List<RFIDTag> searchInventory() {
        return null;
    }

    @Override
    public boolean startReading() {
        return false;
    }

    @Override
    public boolean startSearching() {
        return false;
    }

    @Override
    public List<RFIDTag> search() {
        return null;
    }

    @Override
    public boolean stopSearching() {
        return false;
    }

    @Override
    public Reader.READER_ERR HighPowerLevel() {
        return null;
    }

    @Override
    public Reader.READER_ERR LowPowerLevel() {
        return null;
    }

    @Override
    public int[] getPowerLevel() {
        return new int[0];
    }


    // ########################################################################
    // ### ZEBRA Methods:: handler for receiving reader appearance events #####
    // ########################################################################
    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        connectReader();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (readerDevice.getName().equals(reader.getHostName()))
            disconnect();
    }


    // ########################
    // ### Private Methods ####
    // ########################

    private boolean isReaderConnected() {
        if (reader != null && reader.isConnected())
            return true;
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    private synchronized void connectReader() {
        if (!isReaderConnected()) {
            //new ConnectionTask().execute();
        }
    }

    // should be called upon Pause() ::TODO:: make public and call it from parent actiivity?
    private synchronized void disconnect() {
        Log.d(TAG, "disconnect " + reader);
//        try {
//            if (reader != null) {
//                reader.Events.removeEventsListener(eventHandler);
//                reader.disconnect();
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.setText("Disconnected");
//                    }
//                });
//            }
//        } catch (InvalidUsageException e) {
//            e.printStackTrace();
//        } catch (OperationFailureException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
