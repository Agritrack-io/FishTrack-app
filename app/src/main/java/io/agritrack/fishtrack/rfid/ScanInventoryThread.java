package io.agritrack.fishtrack.rfid;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.pda.serialport.Tools;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;

public class ScanInventoryThread extends Thread {
    private boolean scanInProgress;
    private UhfReader uhfReader;
    private TextView rfidTag;
    private TemplateRecyclerAdapter adapter;
    private MutableLiveData<Set<String>> scanResult;
    private Set<String> epcValues;

    public ScanInventoryThread() {
        this.epcValues = new HashSet<>();
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

    public void setAdapter(TemplateRecyclerAdapter adapter) {
        this.adapter = adapter;
    }

    public void setScanResult(MutableLiveData<Set<String>> scanResult) {
        this.scanResult = scanResult;
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
                                epcValues.add(epcStr);

                                if(this.adapter!=null){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            adapter.addItem(epcStr);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } else if(this.rfidTag!=null){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            rfidTag.setText(epcStr);
                                        }
                                    });
                                }
                            }
                        }
                        if(scanResult!=null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    scanResult.setValue(epcValues);
                                }
                            });
                        }
                    }
                } catch (NullPointerException ignored) { }
            } else {
                break;
            }
        }
    }

}
