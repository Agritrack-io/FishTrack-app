package io.agritrack.fishtrack.rfid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;

import cn.pda.serialport.Tools;

public class ScanFilterThread extends Thread {
    public static final String RFID_PREFIX = "BE0019A0000";

    private boolean scanInProgress;
    private UhfReader uhfReader;
    private Handler handler;
    private String filterEPC;

    public ScanFilterThread() {
    }

    public ScanFilterThread(UhfReader uhfReader, Handler handler) {
        this.uhfReader = uhfReader;
        this.handler = handler;
    }

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setFilterEPC(String filterTag) {
        this.filterEPC = filterTag;
    }

    public void setScanInProgress(Boolean val) {
        this.scanInProgress = val;
    }

    @Override
    public void run() {
        List<TagModel> tagList;
        while (scanInProgress) {
            if (uhfReader != null) {
                try {
                    uhfReader.setOutputPower(23);
                    tagList = uhfReader.inventorySingle(RFID_PREFIX + filterEPC);

                    if (tagList != null && !tagList.isEmpty()) {
                        TagModel tag = tagList.get(0);
                        final String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
                        final byte rssi = tag.getmRssi();
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();
                        b.putInt("rssi", rssi);
                        b.putString("epc", epcStr);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            } else {
                break;
            }
        }
    }
}
