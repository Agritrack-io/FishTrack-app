package io.agritrack.kefalonia.rfid;

import static io.agritrack.kefalonia.common.Filters.RFID_BIN;
import static io.agritrack.kefalonia.common.Filters.RFID_LOGGER;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.caen.api.ICAEN_API;
import io.agritrack.kefalonia.caen.api.RFIDModuleFactory;
import io.agritrack.kefalonia.caen.pojo.RFIDTag;
import io.agritrack.kefalonia.data.model.EncodingSchemeEntity;
import io.agritrack.kefalonia.data.service.EncodingSchemeService;

public class SingleShotScanner implements Runnable {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final Handler mScanHandler;
    private ICAEN_API uhfReader;
    private String RFID_FILTER = null;
    private String EXCL_RFID_FILTER = null;
    private Boolean trimEPCFlag = Boolean.TRUE;
    private Integer maxLength = null;
    private boolean readEpcList = false;
    private int encodingIdx = schemeSvc.encodingIndex();
    private int encodingWth = schemeSvc.encodingWidth();

    public SingleShotScanner(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        if (uhfReader != null) {
            uhfReader.clearEPCFilter();
        }
        mScanHandler = handler;
        // use by default high energy. Explicitly set to Low Energy (after initialization) where required.
        this.HighEnergy();
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
        if (uhfReader == null) {
            uhfReader = RFIDModuleFactory.getInstance();
        }
        return true;
    }

    public void stopReading() {
        if (uhfReader != null) {
            uhfReader.StopReading();
        }
    }

    public void readEpcList() {
        readEpcList = true;
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        this.trimEPCFlag = (RFID_BIN.equalsIgnoreCase(rfidFilter) || RFID_LOGGER.equalsIgnoreCase(rfidFilter)) ? Boolean.FALSE : Boolean.TRUE;
        EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(rfidFilter);
        if (schemeEntry != null && schemeEntry.encoding_index != null) {
            this.encodingIdx = schemeEntry.encoding_index;
            this.encodingWth = schemeEntry.code.length();
        }
    }

    public void setExcludedFilter(String exclFilter) {
        this.EXCL_RFID_FILTER = exclFilter;
        EncodingSchemeEntity schemeEntry = schemeSvc.schemeForFilter(exclFilter);
        if (schemeEntry != null && schemeEntry.encoding_index != null) {
            this.encodingIdx = schemeEntry.encoding_index;
            this.encodingWth = schemeEntry.code.length();
        }
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void run() {
        int idx = 0;
        while (true) {
            if (uhfReader != null) {
                final List<RFIDTag> tagList = uhfReader.inventoryByTimer(); //inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle b = new Bundle();

                    List<RFIDTag> filteredList = null;
                    if (this.maxLength != null) {
                        filteredList = tagList.stream().filter(f -> f.getEpc().length() > maxLength).collect(Collectors.toList());
                        filteredList = this.EXCL_RFID_FILTER != null ? tagList.stream().filter(x -> x.getEpc().length() == maxLength && !x.getEpc().startsWith(this.EXCL_RFID_FILTER)).collect(Collectors.toList()) : filteredList;
                        b.putLong("cnt", filteredList.size());
                    } else {
                        filteredList = tagList.stream().filter(f -> this.RFID_FILTER == null || (f.getEpc().indexOf(this.RFID_FILTER) == encodingIdx && encodingIdx > -1) || (f.getEpc().indexOf(this.RFID_FILTER) > -1)).collect(Collectors.toList());
                    }

                    if (!readEpcList) {
                        Optional<RFIDTag> tag = filteredList.stream().sorted((y, x) -> Integer.compare(x.getRssi(), y.getRssi())).findFirst();

                        if (tag.isPresent()) {
                            String tagStr = tag.get().getEpc();
                            b.putString("epc", tagStr);
                        }
                    } else {
                        ArrayList<String> epcList = new ArrayList<>();
                        for (RFIDTag tag : filteredList) {
                            epcList.add(tag.getEpc());
                        }
                        b.putStringArrayList("epcList", epcList);
                    }
                    msg.setData(b);
                    mScanHandler.sendMessage(msg);
                    mScanHandler.removeCallbacks(this);
                    break;
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
