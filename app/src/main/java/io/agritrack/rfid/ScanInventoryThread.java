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
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.data.model.EncodingSchemeEntity;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.sound.SoundUtil;

public class ScanInventoryThread implements Runnable {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();

    private final Handler mScanHandler;
    private final ICAEN_API uhfReader;
    private boolean scanInProgress = false;
    private String RFID_FILTER;
    private int encodingIdx = schemeSvc.encodingIndex();
    private int encodingWth = schemeSvc.encodingWidth();


    public ScanInventoryThread(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        if (uhfReader!=null) {
            uhfReader.clearEPCFilter();
        }
        mScanHandler = handler;
        // use by default high energy. Explicitly set to Low Energy (after initialization) where required.
        this.HighEnergy();
    }

    public void LowEnergy() {
        if (uhfReader!=null)
        uhfReader.LowPowerLevel();
    }

    public void HighEnergy() {
        if (uhfReader!=null)
        uhfReader.HighPowerLevel();
    }

    public boolean startReading() {
        if (uhfReader!=null) {
            this.scanInProgress = true;
            return uhfReader.startReading();
        }
        return false;
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(rfidFilter);
        if (schemeEntry!=null && schemeEntry.encoding_index!=null){
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
        if (uhfReader != null && this.scanInProgress) {
            try {
                SoundUtil.play(Beep, 1, 1f);
                final List<RFIDTag> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (f.getEpc().indexOf(this.RFID_FILTER) == encodingIdx && encodingIdx >-1)   || (f.getEpc().indexOf(this.RFID_FILTER) > -1));
                    List<RFIDTag> filteredList = filteredStream.collect(Collectors.toList());
                    for (RFIDTag tag : filteredList) {
                        if (tag != null) {
                            final String epcStr = tag.getEpc();
                            if (epcStr.length() <= (encodingWth + encodingIdx)) {
                                continue;
                            }
//                            if (encodingIdx<0){
                                epcValues.add(epcStr);
                           /* } else {
                                epcValues.add(epcStr.substring(encodingIdx));
                            }*/
                        }
                    }

                    Message msg = new Message();
                    msg.what = 100;
                    Bundle b = new Bundle();
                    b.putCharSequenceArrayList("epc", epcValues);
                    msg.setData(b);
                    mScanHandler.sendMessage(msg);
                }
            } catch (NullPointerException ignored) {
                ignored.printStackTrace();
            }
            mScanHandler.postDelayed(this, 0l);
        }
        if (!scanInProgress) {
            mScanHandler.sendEmptyMessage(1980);
            uhfReader.StopReading();
            mScanHandler.removeCallbacks(this);
        }
    }
}
