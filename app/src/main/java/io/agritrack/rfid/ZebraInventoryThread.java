package io.agritrack.rfid;

import static io.agritrack.sound.SoundUtil.Beep;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.api.ZebraTC26Commander;
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.data.model.EncodingSchemeEntity;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.sound.SoundUtil;

public class ZebraInventoryThread implements Runnable {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();

    private final Handler mScanHandler;
    private final ICAEN_API uhfReader;
    private boolean scanInProgress = false;
    private String RFID_FILTER;
    private int encodingIdx = schemeSvc.encodingIndex();
    private int encodingWth = schemeSvc.encodingWidth();

    public ZebraInventoryThread(Handler handler, ZebraTC26Commander.ResponseHandlerInterface activity) {
        super();
        uhfReader = RFIDModuleFactory.getInstance(activity);
        if (uhfReader != null) {
            uhfReader.clearEPCFilter();
        }
        mScanHandler = handler;
        // use by default high energy. Explicitly set to Low Energy (after initialization) where required.
//        this.HighEnergy();
    }

    public void LowEnergy() {
        if (uhfReader != null) {
            uhfReader.LowPowerLevel();
        }
    }

    public void HighEnergy() {
        if (uhfReader != null) {
            uhfReader.HighPowerLevel();
        }
    }

    public boolean startReading() {
        if (uhfReader != null) {
            this.scanInProgress = true;
            uhfReader.inventoryRealTime();
            return true;
        }
        return false;
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(rfidFilter);
        if (schemeEntry != null && schemeEntry.encoding_index != null) {
            this.encodingIdx = schemeEntry.encoding_index;
            this.encodingWth = schemeEntry.code.length();
        }
    }

    public void stopReading() {
        uhfReader.StopReading();
        this.scanInProgress = false;
        this.RFID_FILTER = null;
    }

    public boolean isReading() {
        return this.scanInProgress;
    }

    @Override
    public void run() {
        ArrayList<CharSequence> epcValues = new ArrayList<>();
    }
}
