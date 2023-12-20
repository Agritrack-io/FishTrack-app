package io.agritrack.caen.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.CollectionUtils;
import com.uhf.api.cls.Reader;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.FILTER_ACTION;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATE_AWARE_ACTION;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TARGET;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagDataArray;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.common.async.AsyncTaskExecutorService;

public class ZebraTC26Commander extends AbstractCAENCommander implements Readers.RFIDReaderEventHandler {
    final static String TAG = "Zebra_TC26";
    // available RFID readers
    private static Readers readers;
    // RFID Reader
    private static RFIDReader currentReader;
    // the actual RFID reader
    private static ReaderDevice readerDevice;
    // list of available RFID readers...
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    // Power levels
    private int MIN_POWER = 0;
    private int MAX_POWER = 270; //TODO: test actual values!
    // Activity where this class was invoked from.
    private Context context;
    private String filterEPC;

    //private final Handler mScanHandler;
    // listener handling RFID events
    private RfidEventsListener rfidEventsListener;

    public ZebraTC26Commander(Context ctx) {
        this.context = ctx;
        // SDK
        InitSDK();
    }


    //.........................................................
    @Override
    public void setFilterEPC(String epc) {
        this.filterEPC = epc;
//        AccessFilter accessFilter = new AccessFilter();
//        byte[] tagMask = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
//
//        // Tag Pattern A
//        accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
//        accessFilter.TagPatternA.setTagPattern(epc.getBytes());
//        accessFilter.TagPatternA.setTagPatternBitCount(8 * 8);
//        accessFilter.TagPatternA.setBitOffset(0);
//        accessFilter.TagPatternA.setTagMask(tagMask);
//        accessFilter.TagPatternA.setTagMaskBitCount(tagMask.length * 8);
//        accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);
    }

    // Add state aware pre-filter for given EPC or Tag ID
    private void addfilters(String tag) {
        // Add state aware pre-filter
        PreFilters filters = new PreFilters();
        PreFilters.PreFilter filter = filters.new PreFilter();
        filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
        filter.setTagPattern(tag);// Tags which starts with passed pattern
        filter.setTagPatternBitCount(tag.length() * 4);
        filter.setBitOffset(32); // skip PC bits (always it should be in bit length)
        filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
        filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
        filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S1); // inventoried flag of session S1 of matching tags to B
        filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B);
        // not to select tags that match the criteria
        try {
            currentReader.Actions.PreFilters.add(filter);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    // Set the singulation control matching with prefilter
    private void setSingulationForFilter() {
        try {
            Antennas.SingulationControl s1_singulationControl = currentReader.Config.Antennas.getSingulationControl(1);
            s1_singulationControl.setSession(SESSION.SESSION_S1);
            s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);
            s1_singulationControl.Action.setPerformStateAwareSingulationAction(true);
            currentReader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }
    //.........................................................

    @Override
    public boolean clearEPCFilter() {
        // delete any prefilters
        try {
            if (currentReader != null && currentReader.isConnected()) {
                this.filterEPC = null;
                if (currentReader.Actions.PreFilters.length() > 0) {
                    currentReader.Actions.PreFilters.deleteAll();
                }
            } else {
                return false;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            return false;
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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
        return Reader.READER_ERR.MT_OP_NOT_SUPPORTED;
    }

    @Override
    public byte CheckReply() {
        return 0;
    }

    @Override
    public void CloseReader() {
//        this.Status(Boolean.FALSE);
//        RFIDModuleFactory.Reset();
        try {
            if (currentReader != null) {
                currentReader.Events.removeEventsListener(rfidEventsListener);
                currentReader.disconnect();
                Toast.makeText((AppCompatActivity) context, "Disconnecting reader", Toast.LENGTH_LONG).show();
                currentReader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void StopReading() {
        new AsyncTaskExecutorService<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                try {
                    currentReader.Actions.Inventory.stop();
//                    Status(Boolean.FALSE);
//                    RFIDModuleFactory.Reset();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                int k = 0;
            }

        }.execute();
    }

    @Override
    public List<RFIDTag> inventoryRealTime() {
        new AsyncTaskExecutorService<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                try {
                    currentReader.Actions.Inventory.perform();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                int k = 0;
            }

        }.execute();


        return null;
    }

    @Override
    public List<RFIDTag> inventoryByTimer() {
        new AsyncTaskExecutorService<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                try {
                    currentReader.Actions.Inventory.perform();

                    // Sleep or wait
                    try {
                        Thread.sleep(50, 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // stop the inventory
                    currentReader.Actions.Inventory.stop();

                } catch (InvalidUsageException e) {

                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                int k = 0;
            }

        }.execute();


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
        try {
            currentReader.Actions.TagLocationing.Perform(this.filterEPC, null, null);
            return true;
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<RFIDTag> search() {
        return null;
    }

    @Override
    public boolean stopSearching() {
        try {
            currentReader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Reader.READER_ERR HighPowerLevel() {
        try {
            // set antenna configurations
            Antennas.AntennaRfConfig config = currentReader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(MAX_POWER);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            currentReader.Config.Antennas.setAntennaRfConfig(1, config);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }

        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public Reader.READER_ERR LowPowerLevel() {
        try {
            // set antenna configurations
            Antennas.AntennaRfConfig config = currentReader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(MIN_POWER);
            int aa = config.getTransmitPowerIndex();
            config.setrfModeTableIndex(0);
            config.setTari(0);
            currentReader.Config.Antennas.setAntennaRfConfig(1, config);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return Reader.READER_ERR.MT_CMD_FAILED_ERR;
        }

        return Reader.READER_ERR.MT_OK_ERR;
    }

    @Override
    public int[] getPowerLevel() {
        try {
            // get antenna configuration and read the current TransmitPowerIndex
            Antennas.AntennaRfConfig config = currentReader.Config.Antennas.getAntennaRfConfig(1);
            return new int[config.getTransmitPowerIndex()];
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }

        return new int[0];
    }

    // ########################
    // ### Private Methods ####
    // ########################

    /**
     * checks whether currentReader is instantiated and connected
     *
     * @return: true if currentReader is connected, false otherwise.
     */
    private boolean isReaderConnected() {
        if (currentReader != null && currentReader.isConnected())
            return true;
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    private synchronized void connectReader() {
        if (!isReaderConnected()) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (currentReader != null)
                connect();
        }
    }

    // should be called upon Pause() ::TODO:: make public and call it from parent actiivity?
    private synchronized void disconnect() {
        Log.d(TAG, "disconnect " + currentReader);
        try {
            if (currentReader != null) {
                currentReader.Events.removeEventsListener(rfidEventsListener);
                currentReader.disconnect();
                //TODO: check this type casting in conjuction with: (Context) context
                ((AppCompatActivity) context).runOnUiThread(() -> {
                    //textView.setText("Disconnected");
                });
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ########################################################################
    // ### ZEBRA Methods:: handler for receiving reader appearance events #####
    // ########################################################################
    //
    // RFID SDK
    //
    private void InitSDK() {
        Log.d(TAG, "InitZebraSDK");
        if (readers == null) {
            Log.d(TAG, "DetectAvailableZebraReadersTask");
            try {
                // Based on support available on host device choose the reader type
                if (readers == null) {
                    readers = new Readers(context, ENUM_TRANSPORT.ALL);
                }

                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
            } catch (InvalidUsageException e) {
                // log the Exception
                e.printStackTrace();

                // discard and re-instantiate the readers object again.
                readers.Dispose();
                readers = null;
                if (readers == null) {
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                }
            }
        }
        connectReader();
    }


    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        connectReader();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (readerDevice.getName().equals(currentReader.getHostName()))
            disconnect();
    }

    /**
     * Retrieves the list of available readers and picks the first for 'Current Reader'
     */
    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        if (readers != null) {
            readers.attach(this);

            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                if (availableRFIDReaderList != null) {
                    // TODO: original code has different login, We get ALWAYS the first reader
                    if (availableRFIDReaderList.size() > 0) {
                        readerDevice = availableRFIDReaderList.get(0);
                        currentReader = readerDevice.getRFIDReader();
                    }
                }
            } catch (InvalidUsageException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * attempts to connect to currentReader.
     * if successful, assigns the default configuration values.
     *
     * @return: a string message informing on the outcome of the connection attempt.
     */
    private synchronized String connect() {
        if (currentReader != null) {
            Log.d(TAG, "connect " + currentReader.getHostName());
            try {
                if (!currentReader.isConnected()) {
                    // Establish connection to the RFID Reader
                    currentReader.connect();
                    ConfigureReader();
                    if (currentReader.isConnected()) {
                        return "Connected: " + currentReader.getHostName();
                    }
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();
                return "Connection failed" + e.getVendorMessage() + " " + des;
            }
        }
        return "Previously connected to " + currentReader.getHostName();
    }

    private void ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + currentReader.getHostName());
        if (currentReader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (rfidEventsListener == null) {
                    rfidEventsListener = new ZebraRFIDEventsListener();
                }
                currentReader.Events.addEventsListener(rfidEventsListener);

                // HH event
                currentReader.Events.setHandheldEvent(true);

                // tag event with tag data
                currentReader.Events.setTagReadEvent(true);

                // application will collect tag using getReadTags API
                currentReader.Events.setAttachTagDataWithReadEvent(false);

                // set trigger mode as rfid so scanner beam will not come
                currentReader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);

                // set start and stop triggers
                currentReader.Config.setStartTrigger(triggerInfo.StartTrigger);
                currentReader.Config.setStopTrigger(triggerInfo.StopTrigger);

                // power levels are index based so maximum power supported get the last one
                MIN_POWER = 1;
                MAX_POWER = currentReader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;

                // set antenna configurations
                Antennas.AntennaRfConfig config = currentReader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(MAX_POWER);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                currentReader.Config.Antennas.setAntennaRfConfig(1, config);

                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = currentReader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                currentReader.Config.Antennas.setSingulationControl(1, s1_singulationControl);

//                // Get tag storage settings from the reader
//                TagStorageSettings tagStorageSettings = currentReader.Config.getTagStorageSettings();
//                // set tag storage settings on the reader with all fields
//                tagStorageSettings.setTagFields(TAG_FIELD.ALL_TAG_FIELDS);
//                currentReader.Config.setTagStorageSettings(tagStorageSettings);

                // delete any prefilters
                currentReader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    //########################################################
    //----  Zebra related asynchronous tasks -----------------


    /**
     * defines the methods that should be implemented by every activity that uses ZebraT26Commander.
     */
    public interface ResponseHandlerInterface {
        void handleTagsdata(TagData[] tagData);

        void handleTagdata(TagData tagData);

        void handleTriggerPress(boolean pressed);
        //void handleStatusEvents(Events.StatusEventData eventData);
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class ZebraRFIDEventsListener implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {

//            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
//            TagData[] tagsRead = currentReader.Actions.getReadTags(100);
//
//            // if >0 tags were read, proceed...
//            if (tagsRead != null) {
//                // stop the inventory
//
//                List<TagData> tagsList = Arrays.asList(tagsRead);
//                List<TagData> tagsFound = tagsList.stream().filter(f -> f.isContainsLocationInfo()).collect(Collectors.toList());
//
//                int tagsCnt = tagsRead.length;
//                for (int index = 0; index < tagsCnt; index++) {
//                    Log.d(TAG, "Tag ID " + tagsRead[index].getTagID());
//
//                    if (tagsRead[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
//                            tagsRead[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
//                        if (tagsRead[index].getMemoryBankData().length() > 0) {
//                            Log.d(TAG, " Mem Bank Data " + tagsRead[index].getMemoryBankData());
//                        }
//                    }
//                    if (tagsRead[index].isContainsLocationInfo()) {
//                        short dist = tagsRead[index].LocationInfo.getRelativeDistance();
//                        Log.d(TAG, "Tag relative distance " + dist + " EPC = " + tagsRead[index].getTagID());
//                        new AsyncDataSearch().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tagsRead[index]);
//                    }
//                }
//
//                // possibly if operation was invoked from async task and still busy
//                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
//                if (CollectionUtils.isEmpty(tagsFound) && !CollectionUtils.isEmpty(tagsList)) {
//                    new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tagsRead);
//                }
//            }

            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagDataArray tagsRead = currentReader.Actions.getReadTagsEx(100);

            // if >0 tags were read, proceed...
            if (tagsRead != null) {

                List<TagData> tagsList = Arrays.asList(tagsRead.getTags());
                List<TagData> tagsFound = tagsList.stream().filter(f -> f.isContainsLocationInfo()).collect(Collectors.toList());

                TagData[] tags = tagsRead.getTags();
                int tagsCnt = tagsRead.getLength();
                for (int index = 0; index < tagsCnt; index++) {
                    Log.d(TAG, "Tag ID " + tags[index].getTagID());

                    if (tags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            tags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (tags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + tags[index].getMemoryBankData());
                        }
                    }
                    if (tags[index].isContainsLocationInfo()) {
                        short dist = tags[index].LocationInfo.getRelativeDistance();
                        Log.d(TAG, "Tag relative distance " + dist + " EPC = " + tags[index].getTagID());
                        new AsyncDataSearch().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tags[index]);
                    }
                }

                // possibly if operation was invoked from async task and still busy
                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
                if (CollectionUtils.isEmpty(tagsFound) && !CollectionUtils.isEmpty(tagsList)) {
                    new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tags);
                }
            }
        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
//                    new AsyncTaskExecutorService<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            context.handleTriggerPress(true);
//                            return null;
//                        }
//                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
//                    new AsyncTaskExecutorService<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            context.handleTriggerPress(false);
//                            return null;
//                        }
//                    }.execute();
                }
            }
        }
    }

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            ((ResponseHandlerInterface) context).handleTagsdata(params[0]);
            return null;
        }
    }

    private class AsyncDataSearch extends AsyncTask<TagData, Void, Void> {
        @Override
        protected Void doInBackground(TagData... param) {
            ((ResponseHandlerInterface) context).handleTagdata(param[0]);
            return null;
        }
    }
}
