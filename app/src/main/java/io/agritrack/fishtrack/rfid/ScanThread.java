package io.agritrack.fishtrack.rfid;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;

import cn.pda.serialport.Tools;

public class ScanThread extends Thread {
    private boolean scanInProgress;
    private UhfReader uhfReader;
    private TextView rfidTag;

    public ScanThread() {}

    public ScanThread(boolean scanning, UhfReader reader, TextView epcTextView) {
        this.scanInProgress = scanning;
        this.uhfReader = reader;
        this.rfidTag = epcTextView;
    }

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
    }

    public void setRfidTag(TextView rfidTag) {
        this.rfidTag = rfidTag;
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
                    tagList = uhfReader.inventoryRealTime();
                    if (tagList != null && !tagList.isEmpty()) {
                        for (TagModel tag : tagList) {
                            if (tag != null) {
                                final String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        rfidTag.setText(epcStr);
                                    }
                                });
                            }
                        }
                    }
                } catch (NullPointerException ignored) {
                }
            } else {
                break;
            }
        }
    }

}
