package io.agritrack.rfid;

import static io.agritrack.common.Filters.RFID_BIN;
import static io.agritrack.common.Filters.RFID_LOGGER;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.data.model.EncodingSchemeEntity;
import io.agritrack.data.service.EncodingSchemeService;

public class SingleShotScanner implements Runnable {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();

    private ICAEN_API uhfReader;
    private Handler mScanHandler;
    private String RFID_FILTER = null;
    private Boolean trimEPCFlag = Boolean.TRUE;
    private int encodingIdx = schemeSvc.encodingIndex();
    private int encodingWth = schemeSvc.encodingWidth();

    public SingleShotScanner(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        if (uhfReader!=null) {
            uhfReader.clearEPCFilter();
        }
        mScanHandler = handler;
    }

    public void LowEnergy() {
        uhfReader.LowPowerLevel();
    }

    public void HighEnergy() {
        uhfReader.HighPowerLevel();
    }

    public boolean startReading() {
        if(uhfReader == null) {
            uhfReader = RFIDModuleFactory.getInstance();
        }
        return true;// uhfReader.startReading();
    }

    public void stopReading() {
        uhfReader.StopReading();
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        this.trimEPCFlag = (RFID_BIN.equalsIgnoreCase(rfidFilter) || RFID_LOGGER.equalsIgnoreCase(rfidFilter)) ? Boolean.FALSE : Boolean.TRUE;
        EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(rfidFilter);
        if (schemeEntry!=null && schemeEntry.encoding_index!=null){
            this.encodingIdx = schemeEntry.encoding_index;
            this.encodingWth = schemeEntry.code.length();
        }
    }

    @Override
    public void run() {
        int idx = 0;
        while (true) {
            if (uhfReader != null) {
                final List<RFIDTag> tagList = uhfReader.inventoryByTimer(); //inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (f.getEpc().indexOf(this.RFID_FILTER) == encodingIdx && encodingIdx >-1)   || (f.getEpc().indexOf(this.RFID_FILTER) > -1));
                    //Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || f.getEpc().indexOf(this.RFID_FILTER) == 11);
                    Optional<RFIDTag> tag = filteredStream.sorted((y, x) -> Integer.compare(x.getRssi(), y.getRssi())).findFirst();

                    if (tag.isPresent()) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();

                        String tagStr = tag.get().getEpc();
                        b.putString("epc", tagStr);

                        msg.setData(b);
                        mScanHandler.sendMessage(msg);
                        mScanHandler.removeCallbacks(this);
                        break;
                    }
                }
            }
            // to avoid possible endless loop.
            if (++idx > 10) {
                Message msg = new Message();
                msg.what = 999;
                Bundle b = new Bundle();
                b.putString("err", "No_TAG_Found");
                msg.setData(b);
                mScanHandler.sendMessage(msg);
                mScanHandler.removeCallbacksAndMessages(null);
                break;
            }
        }
    }
}
