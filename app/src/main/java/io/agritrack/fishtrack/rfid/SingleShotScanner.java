package io.agritrack.fishtrack.rfid;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;
import java.util.concurrent.Callable;

import cn.pda.serialport.Tools;

public class SingleShotScanner implements Callable {
    private UhfReader uhfReader;

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
        this.uhfReader.setOutputPower(20);
    }

    @Override
    public Object call() throws Exception {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<TagModel> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    TagModel tag = tagList.stream().sorted((y, x) -> Byte.compare(x.getmRssi(), y.getmRssi())).findFirst().get();
                    return Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
                }
            }
            // to avoid possible endless loop.
            if (idx > 10)
                return null;
        }
    }
}
