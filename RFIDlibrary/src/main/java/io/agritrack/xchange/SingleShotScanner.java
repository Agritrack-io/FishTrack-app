package io.agritrack.xchange;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.pda.serialport.Tools;


public class SingleShotScanner implements Runnable {
    protected final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);

    private UhfReader uhfReader;
    private final Handler mScanHandler;
    private String RFID_FILTER = null;


    public SingleShotScanner(Handler handler) {
        super();
        uhfReader = UhfReader.getInstance();

        if (uhfReader != null) {
            uhfReader.setWorkArea(3);
            uhfReader.setOutputPower(24);
        } else {
            Toast.makeText(null, "Failed to initialize UHF Reader", Toast.LENGTH_LONG);
        }

        mScanHandler = handler;
    }

    public void LowEnergy() {
        if(uhfReader!=null) {
            uhfReader.setOutputPower(23);
        }
    }

    public void HighEnergy() {
        if(uhfReader!=null) {
            uhfReader.setOutputPower(24);
        }
    }

    public boolean startReading() {
        if (uhfReader == null) {
            uhfReader = UhfReader.getInstance();
        }
        return true;
    }

    public void stopReading() {
        if (uhfReader != null) {
            uhfReader.unSelectEPC();
            uhfReader.close();
        }
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
    }

    @Override
    public void run() {
        int idx = 0;
        while (true) {
            if (uhfReader != null) {
                List<TagModel> inventory = uhfReader.inventoryRealTime();
                final List<RFIDTag> tagList = inventory.stream().map(x->new RFIDTag(TagToString.apply(x), x.getmRssi())).collect(Collectors.toList());
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<RFIDTag> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || f.getEpc().indexOf(this.RFID_FILTER) == 11);
                    Optional<RFIDTag> tag = filteredStream.sorted((y, x) -> Integer.compare(x.getRssi(), y.getRssi())).findFirst();

                    if (tag.isPresent()) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle b = new Bundle();

                        String tagStr = tag.get().getEpc();
                        b.putString("epc", tagStr);

                        msg.setData(b);
                        mScanHandler.sendMessage(msg);
                        mScanHandler.removeCallbacks(this);
                        break;
                    }
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
