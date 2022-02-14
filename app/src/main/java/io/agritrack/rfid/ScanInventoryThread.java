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
import io.agritrack.sound.SoundUtil;

public class ScanInventoryThread implements Runnable {
    private final Handler mScanHandler;
    private final ICAEN_API uhfReader;
    private boolean scanInProgress = false;
    private String RFID_FILTER;


    public ScanInventoryThread(Handler handler) {
        uhfReader = RFIDModuleFactory.getInstance();
        mScanHandler = handler;
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
                    Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (f.getEpc().indexOf(this.RFID_FILTER) == 11));
                    List<RFIDTag> filteredList = filteredStream.collect(Collectors.toList());
                    for (RFIDTag tag : filteredList) {
                        if (tag != null) {
                            final String epcStr = tag.getEpc();
                            if (epcStr.length() <= 12) {
                                continue;
                            }
                            epcValues.add(epcStr.substring(11));
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
