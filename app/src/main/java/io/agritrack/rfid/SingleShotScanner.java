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

public class SingleShotScanner implements Runnable {
    private final ICAEN_API uhfReader;
    private final Handler mScanHandler;
    private String RFID_FILTER = null;
    private Boolean trimEPCFlag = Boolean.TRUE;

    public SingleShotScanner(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        mScanHandler = handler;
    }

    public boolean startReading() {
        return uhfReader.startReading();
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
        this.trimEPCFlag = (RFID_BIN.equalsIgnoreCase(rfidFilter) || RFID_LOGGER.equalsIgnoreCase(rfidFilter)) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public void run() {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<RFIDTag> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || f.getEpc().indexOf(this.RFID_FILTER) == 11);
                    Optional<RFIDTag> tag = filteredStream.sorted((y, x) -> Integer.compare(x.getRssi(), y.getRssi())).findFirst();

                    if (tag.isPresent()) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();

                        String tagStr = tag.get().getEpc();
                        if (!trimEPCFlag) {
                            b.putString("epc", tagStr);
                        } else {
                            b.putString("epc", (tagStr.length() > 12) ? tagStr.substring(11) : "N/A");
                        }

                        msg.setData(b);
                        mScanHandler.sendMessage(msg);
                        uhfReader.StopReading();
                        mScanHandler.removeCallbacks(this);
                        break;
                    } else {
                        mScanHandler.sendEmptyMessage(1980);
                    }
                    mScanHandler.postDelayed(this, 0);
                }
            }
            // to avoid possible endless loop.
            if (idx > 10) {
                mScanHandler.sendEmptyMessage(1980);
                uhfReader.StopReading();
                mScanHandler.removeCallbacks(this);
                break;
            }
        }
    }
}
