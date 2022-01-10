package io.agritrack.rfid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.common.Filters;

public class MultipleFilterSingleShotScanner implements Runnable {

    private final ICAEN_API uhfReader;
    private final Handler mScanHandler;
    private String[] RFID_FILTERS = null;


    public MultipleFilterSingleShotScanner(Handler handler) {
        super();
        uhfReader = RFIDModuleFactory.getInstance();
        mScanHandler = handler;
    }

    public boolean startReading() {
        return uhfReader.startReading();
    }

    public void setFilter(String[] rfidFilters) {
        this.RFID_FILTERS = rfidFilters;
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
                final List<RFIDTag> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    ArrayList<CharSequence> result = new ArrayList<>();
                    List<Optional<RFIDTag>> optionalTags = filterTags(tagList);

                    if(!optionalTags.isEmpty()) {
                        for (Optional<RFIDTag> optionalTag : optionalTags) {
                            if (optionalTag.isPresent())
                                if (optionalTag.get().getEpc().indexOf(Filters.RFID_LOGGER) > -1)
                                    result.add(optionalTag.get().getEpc());
                                else
                                    result.add(optionalTag.get().getEpc().substring(11));
                        }
                        TextUtils.join(",", result);

                        Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();

                        b.putCharSequenceArrayList("epc", result);

                        msg.setData(b);
                        mScanHandler.sendMessage(msg);
                        uhfReader.StopReading();
                        mScanHandler.removeCallbacks(this);
                        break;
                    } else {
                        mScanHandler.sendEmptyMessage(1980);
                    }
                } else {
                    mScanHandler.sendEmptyMessage(1980);
                }
                mScanHandler.postDelayed(this, 0);
            }
            // to avoid possible endless loop.
            if (idx > 10) {
                mScanHandler.sendEmptyMessage(1980);
                //mScanHandler.postDelayed(this, 0);
                uhfReader.StopReading();
                mScanHandler.removeCallbacks(this);
                break;
            }
        }
    }

    private List<Optional<RFIDTag>> filterTags(List<RFIDTag> tagList) {
        Set<Optional<RFIDTag>> filteredTags = new HashSet<>();
        for (String filter : this.RFID_FILTERS) {
            Optional<RFIDTag> aTag = tagList.stream().sorted((y, x) -> Integer.compare(x.getRssi(), y.getRssi())).filter(i -> i.getEpc().indexOf(filter) == 11).findFirst();
            filteredTags.add(aTag);
        }
        return new ArrayList<>(filteredTags);
    }
}
