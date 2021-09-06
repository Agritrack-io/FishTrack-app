package io.agritrack.fishtrack.rfid;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import cn.pda.serialport.Tools;

public class SingleShotScanner implements Callable {
    private final Function<TagModel, String> TagToString = t -> Tools.Bytes2HexString(t.getmEpcBytes(), t.getmEpcBytes().length);
    private UhfReader uhfReader;
    private String RFID_FILTER = null;

    public void setUhfReader(UhfReader uhfReader) {
        this.uhfReader = uhfReader;
        this.uhfReader.setOutputPower(20);
    }

    public void setFilter(String rfidFilter) {
        this.RFID_FILTER = rfidFilter;
    }

    @Override
    public Object call() throws Exception {
        int idx = 0;
        while (true) {
            idx++;
            if (uhfReader != null) {
                final List<TagModel> tagList = uhfReader.inventoryRealTime();
                if (tagList != null && !tagList.isEmpty()) {
                    Stream<TagModel> filteredStream = tagList.stream().filter(f -> this.RFID_FILTER == null || (TagToString.apply(f)).indexOf(this.RFID_FILTER) == 11);
                    TagModel tag = filteredStream.sorted((y, x) -> Byte.compare(x.getmRssi(), y.getmRssi())).findFirst().get();
                    if (tag != null) {
                        String tagStr = TagToString.apply(tag);
                        return (tagStr.length() > 15) ? tagStr.substring(15) : "N/A";
                    } else {
                        return null;
                    }
                }
            }
            // to avoid possible endless loop.
            if (idx > 10)
                return null;
        }
    }
}
