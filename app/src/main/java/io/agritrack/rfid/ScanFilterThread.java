package io.agritrack.rfid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.List;

import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.pojo.RFIDTag;

public class ScanFilterThread implements Runnable {
    public static final String RFID_PREFIX = "BE0019A0000";

    private final Handler mScanHandler;
    private final ICAEN_API uhfReader;
    private boolean scanInProgress = false;
    private String filterEPC;

    public ScanFilterThread(Handler handler) {
        uhfReader = RFIDModuleFactory.getInstance();
        mScanHandler = handler;
    }

    public void setFilterEPC(String filterTag) {
        this.filterEPC = filterTag;
        uhfReader.setFilterEPC(RFID_PREFIX + filterEPC);
    }

    public boolean startReading() {
        this.scanInProgress = true;
        return uhfReader.startReading();
    }

    public void stopReading() {
        this.scanInProgress = false;
        this.filterEPC = null;
        uhfReader.StopReading();
    }

    public boolean isReading() {
        return this.scanInProgress;
    }

    @Override
    public void run() {
        List<RFIDTag> tagList;
        if (uhfReader != null && scanInProgress) {
            try {
                tagList = uhfReader.inventoryRealTime();

                if (tagList != null && !tagList.isEmpty()) {
                    RFIDTag tag = tagList.get(0);
                    Message msg = new Message();
                    msg.what = 10;
                    Bundle b = new Bundle();
                    b.putInt("rssi", tag.getRssi());
                    b.putString("epc", tag.getEpc());
                    msg.setData(b);
                    mScanHandler.sendMessage(msg);
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        if (!scanInProgress) {
            uhfReader.StopReading();
            mScanHandler.sendEmptyMessage(1980);
            mScanHandler.removeCallbacks(this);
        }
        mScanHandler.postDelayed(this, 0);
    }
}
