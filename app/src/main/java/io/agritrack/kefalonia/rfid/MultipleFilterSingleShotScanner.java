package io.agritrack.kefalonia.rfid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.agritrack.kefalonia.caen.api.ICAEN_API;
import io.agritrack.kefalonia.caen.api.RFIDModuleFactory;
import io.agritrack.kefalonia.caen.pojo.RFIDTag;
import io.agritrack.kefalonia.data.model.EncodingSchemeEntity;
import io.agritrack.kefalonia.data.service.EncodingSchemeService;

public class MultipleFilterSingleShotScanner implements Runnable {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final Handler mScanHandler;
    private ICAEN_API uhfReader;
    private String[] RFID_FILTERS = null;
    private int encodingIdx = schemeSvc.encodingIndex();
    private int encodingWth = schemeSvc.encodingWidth();


    public MultipleFilterSingleShotScanner(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        if (uhfReader != null) {
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
        if (uhfReader == null) {
            uhfReader = RFIDModuleFactory.getInstance();
        }
        return true;// uhfReader.startReading();
    }

    public void stopReading() {
        uhfReader.StopReading();
    }

    //set filters by encoding index
    public void setFilter(String[] rfidFilters) {
        for (String rfidFilter : rfidFilters) {
            EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(rfidFilter);
            if (schemeEntry != null && schemeEntry.encoding_index != null) {
                this.encodingIdx = schemeEntry.encoding_index;
                this.encodingWth = schemeEntry.code.length();
            }
            this.RFID_FILTERS = rfidFilters;
        }
    }

    public void setFilters(String... rfidFilters) {
        this.RFID_FILTERS = rfidFilters;
    }

    @Override
    public void run() {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<RFIDTag> tagList = uhfReader.inventoryByTimer(); //inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle b = new Bundle();

                    ArrayList<CharSequence> result = new ArrayList<>();
                    List<Optional<RFIDTag>> optionalTags = filterTags(tagList);

                    if (!optionalTags.isEmpty()) {
                        for (Optional<RFIDTag> optionalTag : optionalTags) {
                            if (optionalTag.isPresent())
                                result.add(optionalTag.get().getEpc());
                        }
                        TextUtils.join(",", result);

                       /* Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();*/

                        b.putCharSequenceArrayList("epc", result);

                        msg.setData(b);
                        mScanHandler.sendMessage(msg);
                        uhfReader.StopReading();
                        mScanHandler.removeCallbacks(this);
                        break;
                    } else {
                        mScanHandler.sendEmptyMessage(1980);
                        mScanHandler.removeCallbacks(this);
                    }
                } else {
                    mScanHandler.sendEmptyMessage(1980);
                    mScanHandler.removeCallbacks(this);
                }
            }
            // to avoid possible endless loop.
            if (idx > 10) {
                uhfReader.StopReading();
                mScanHandler.removeCallbacks(this);
                break;
            }
        }
    }

    private List<Optional<RFIDTag>> filterTags(List<RFIDTag> tagList) {
        Set<Optional<RFIDTag>> filteredTags = new HashSet<>();
        for (String filter : this.RFID_FILTERS) {
            Optional<RFIDTag> aTag = tagList.stream()
                    .filter(i -> filter == null || i.getEpc().indexOf(filter) == encodingIdx && encodingIdx > -1
                            || (i.getEpc().indexOf(filter) > -1)).min(Comparator.comparing(RFIDTag::getRssi));
            filteredTags.add(aTag);
        }
        return new ArrayList<>(filteredTags);
    }
}
