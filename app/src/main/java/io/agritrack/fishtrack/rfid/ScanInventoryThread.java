package io.agritrack.fishtrack.rfid;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.pda.serialport.Tools;

public class ScanInventoryThread extends Thread {
    private final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);

    private boolean scanInProgress;
    private UhfReader uhfReader;
    private MutableLiveData<Set<String>> scanResult;
    private final Set<String> epcValues;
    private String RFID_FILTER;

    public ScanInventoryThread() {
        this.epcValues = new TreeSet<>();
    }

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
    }

    public void setScanInProgress(Boolean val) {
        this.scanInProgress = val;
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
                        Stream<TagModel> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (TagToString.apply(f)).indexOf(this.RFID_FILTER) == 11);
                        for (TagModel tag : filteredStream.collect(Collectors.toList())) {
                            if (tag != null) {
                                final String epcStr = TagToString.apply(tag);
                                if (epcStr.length() <= 12) {
                                    continue;
                                }
                                epcValues.add(epcStr.substring(11));
                            }
                        }

                        if (scanResult != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    scanResult.setValue(epcValues);
                                }
                            });
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
